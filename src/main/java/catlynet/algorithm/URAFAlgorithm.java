/*
 * URAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.SetUtils;
import jloda.util.progress.ProgressListener;

/**
 * computes a canonical uninhibited RAF (U RAF)
 * Daniel Huson, 9.2019
 * Based on notes by Mike Steel
 */
public class URAFAlgorithm extends AlgorithmBase {
    public static final String Name = "U RAF";

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "computes a max RAF, removes inhibited reactions, and then recomputes the max RAF (Section 6 of [HMS16])";
    }

    /**
     * computes a canonical uninhibited RAF (U RAF)
     *
     * @param input - unexpanded catalytic reaction system
     * @return U RAF or empty set
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        var result = new ReactionSystem();
        result.setName(Name);

        // 1. Compute R'= maxRAF(X, R, C, \emptyset, F) for input Q

        progress.setSubtask("MaxRAF R1");
        var R1 = new MaxRAFAlgorithm().apply(input, progress); // this algorithm ignores all inhibitions

        // 2. If R' == emptyset return nil, else let R'' be the subset of reaction r\in  R' for which r is not inhibited by the product of any reaction  in R' or by any element of the foodset.
        if (R1.size() == 0)
            return result;

        var foodSetAndProductions = Utilities.addAllMentionedProducts(R1.getFoods(), R1.getReactions());
        // final Set<MoleculeType> foodSetAndProductions=computeClosure(R1.getFoods(),R1.getReactions());

        progress.setSubtask("Setup R2");
        progress.setMaximum(R1.getReactions().size());
        var R2 = new ReactionSystem("R2");
        R2.getFoods().setAll(R1.getFoods());
        for (var reaction : R1.getReactions()) {
            if (!SetUtils.intersect(reaction.getInhibitions(), foodSetAndProductions))
                R2.getReactions().add(reaction);
            progress.incrementProgress();
        }

        // 3. If R'' = emptyset then output 'nil'

        if (R2.size() == 0)
            return result;

        // 4. Otherwise, let R'''  = maxRAF (X, R', C, \emptyset, F)

        //  5. If R''' = emptyset then output 'nil' else output R''' which is a u-RAF for Q (i.e. a RAF for Q that has no reaction inhibited by any product of R''' or by any food molecule).

        progress.setSubtask("MaxRAF R2");
        result = new MaxRAFAlgorithm().apply(R2, progress);
        result.setName(Name);
        return result;
    }
}
