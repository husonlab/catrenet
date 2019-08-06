/*
 * MUCAFAlgorithm.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package catlynet.algorithm;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import jloda.util.Basic;

import java.util.*;

/**
 * computes a maximally uninhibited CAF (MU CAF)
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MUCAFAlgorithm extends AlgorithmBase {
    /**
     * computes a MU CAF
     *
     * @param input - unexpanded catalytic reaction system
     * @return MU CAF or empty set
     */
    public ReactionSystem apply(ReactionSystem input) {
        final ReactionSystem result = new ReactionSystem();
        result.setName("MU CAF");

        final ReactionSystem expanded = input.getExpandedSystem();
        final Set<Reaction> inputReactions = new TreeSet<>(expanded.getReactions());
        final Set<MoleculeType> inputFood = new TreeSet<>(expanded.getFoods());

        final ArrayList<Set<MoleculeType>> molecules = new ArrayList<>();
        final ArrayList<Set<Reaction>> reactions = new ArrayList<>();

        molecules.add(0, inputFood);
        reactions.add(0, new HashSet<>());

        int i = 0;
        while (true) {
            i++;

            Reaction anUninhibitedReaction = null;
            for (Reaction reaction : Basic.difference(inputReactions, reactions.get(i - 1))) {
                if (molecules.get(i - 1).containsAll(reaction.getProducts()) && molecules.get(i - 1).containsAll(reaction.getCatalysts())
                        && !Basic.intersects(molecules.get(i - 1), reaction.getInhibitors())) {
                    anUninhibitedReaction = reaction;
                    break;
                }
            }
            if (anUninhibitedReaction != null) {
                molecules.add(i, addAllMentionedProducts(molecules.get(i - 1), reactions.get(i - 1)));
                reactions.add(i, Basic.union(reactions.get(i - 1), Collections.singleton(anUninhibitedReaction)));
            } else
                break;
        }

        if (reactions.get(i - 1).size() > 0) {
            result.getReactions().setAll(reactions.get(i - 1));
            result.getFoods().setAll(input.getFoods());
        }

        return result;
    }
}
