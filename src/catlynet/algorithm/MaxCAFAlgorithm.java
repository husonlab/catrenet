/*
 * MaxCAFAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.util.CanceledException;
import jloda.util.ProgressListener;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * computes a maximal "constructively autocatalytic F-generated reaction network" (CAF)
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MaxCAFAlgorithm extends AlgorithmBase {
    /**
     * computes a CAF
     *
     * @param input
     * @return result
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        final ReactionSystem result = new ReactionSystem();
        result.setName("Max CAF");

        final ReactionSystem expanded = input.computeExpandedSystem();
        final Set<Reaction> inputReactions = new TreeSet<>(expanded.getReactions());
        final Set<MoleculeType> inputFood = new TreeSet<>(expanded.getFoods());

        final ArrayList<Set<Reaction>> reactions = new ArrayList<>();
        final ArrayList<Set<MoleculeType>> molecules = new ArrayList<>();

        molecules.add(0, inputFood);
        reactions.add(0, filterReactions(inputFood, inputReactions));

        progress.setMaximum(100);
        progress.setProgress(0);

        int i = 0;
        do {
            i++;
            molecules.add(i, addAllMentionedProducts(molecules.get(i - 1), reactions.get(i - 1)));
            reactions.add(i, filterReactions(molecules.get(i), inputReactions));
            progress.setProgress(Math.min(100, reactions.size()));
        } while (reactions.get(i).size() > reactions.get(i - 1).size());

        if (reactions.get(i).size() > 0) {
            result.getReactions().setAll(reactions.get(i));
            result.getFoods().setAll(input.getFoods());
        }

        return result;
    }
}
