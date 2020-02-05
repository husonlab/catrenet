/*
 * MuCAFAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.util.CanceledException;
import jloda.util.ProgressListener;

import java.util.*;

/**
 * computes a maximally uninhibited CAF (MU CAF)
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MuCAFAlgorithm extends AlgorithmBase {
    /**
     * computes a MU CAF
     *
     * @param input - unexpanded catalytic reaction system
     * @return MU CAF or empty set
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        final ReactionSystem result = new ReactionSystem();
        result.setName("MU CAF");

        final ReactionSystem expanded = input.computeExpandedSystem();
        final ArrayList<Reaction> inputReactions = Basic.randomize(expanded.getReactions(), new Random());
        //final ArrayList<Reaction> inputReactions = new ArrayList<>(expanded.getReactions());

        final Set<MoleculeType> inputFood = new TreeSet<>(expanded.getFoods());

        final ArrayList<Set<MoleculeType>> molecules = new ArrayList<>();
        final ArrayList<Set<Reaction>> reactions = new ArrayList<>();
        final ArrayList<Set<MoleculeType>> inhibitions = new ArrayList<>();

        molecules.add(0, inputFood);
        reactions.add(0, new HashSet<>());
        inhibitions.add(0, new HashSet<>());

        progress.setMaximum(100);
        int i = 0;
        while (true) {
            i++;

            Reaction anUninhibitedReaction = null;
            for (Reaction reaction : Basic.difference(inputReactions, reactions.get(i - 1))) {
                if (!Basic.intersects(reaction.getProducts(), reaction.getInhibitions())
                        && !Basic.intersects(inhibitions.get(i - 1), reaction.getProducts())
                        && molecules.get(i - 1).containsAll(reaction.getReactants())
                        && Basic.intersects(molecules.get(i - 1), reaction.getCatalysts())
                        && !Basic.intersects(molecules.get(i - 1), reaction.getInhibitions())) {
                    anUninhibitedReaction = reaction;
                    break;
                }
            }

            if (anUninhibitedReaction != null) {
                reactions.add(i, Basic.union(reactions.get(i - 1), Collections.singleton(anUninhibitedReaction)));
                molecules.add(i, addAllMentionedProducts(molecules.get(i - 1), reactions.get(i)));
                inhibitions.add(i, Basic.union(inhibitions.get(i - 1), anUninhibitedReaction.getInhibitions()));
            } else
                break;
            progress.setProgress(reactions.size());
        }

        if (reactions.get(i - 1).size() > 0) {
            result.getReactions().setAll(reactions.get(i - 1));
            result.getFoods().setAll(input.getFoods());
        }

        return result;
    }
}
