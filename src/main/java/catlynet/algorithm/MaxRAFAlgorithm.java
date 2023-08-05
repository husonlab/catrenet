/*
 * MaxRAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package catlynet.algorithm;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * computes a maximal "reflexively autocatalytic F-generated reaction network" (RAF)
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MaxRAFAlgorithm extends AlgorithmBase {
    public static final String Name = "Max RAF";

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "computes the maximal RAF [HMS15] (see also [H23])";
    }

    /**
     * computes the max RAF.
     * Ignore all inhibitions.
     *
     * @returns result, empty, it none exists
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        final var result = new ReactionSystem();
        result.setName(Name);

        final var inputReactions = new TreeSet<>(input.getReactions());
        final var inputFood = new TreeSet<>(input.getFoods());

        if (inputReactions.size() > 0) {
            final var reactions = new ArrayList<Set<Reaction>>();
            final var molecules = new ArrayList<Set<MoleculeType>>();

            reactions.add(0, inputReactions);
            molecules.add(0, inputFood);

            progress.setMaximum(100);
            progress.setProgress(0);

            var i = -1;
            do {
                i++;

                molecules.add(i + 1, Utilities.computeClosure(inputFood, reactions.get(i)));
                reactions.add(i + 1, Utilities.filterReactions(molecules.get(i + 1), reactions.get(i)));
                progress.setProgress(Math.min(100, reactions.size()));
            }
            while (reactions.get(i + 1).size() < reactions.get(i).size());

            if (reactions.get(i).size() > 0) {
                result.getReactions().setAll(reactions.get(i));
                result.getFoods().setAll(result.computeMentionedFoods(input.getFoods()));
            }
        }
        return result;
    }
}
