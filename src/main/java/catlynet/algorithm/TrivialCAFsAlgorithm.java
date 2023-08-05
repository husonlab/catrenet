/*
 * TrivialCAFsAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;

import java.util.stream.Collectors;

/**
 * determines all trivial RAFs, i.e. irreducible one-element CAFs
 * Daniel Huson, 4.2020
 */
public class TrivialCAFsAlgorithm extends AlgorithmBase {
    public static final String Name = "trivial CAFs";

    @Override
    public String getDescription() {
        return "computes all reactions that can run using only the food set";
    }

    @Override
    public String getName() {
        return Name;
    }

    /**
     * compute all reactions that can run using only the food set
     *
     * @param input - unexpanded catalytic reaction system
     */

    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        final ReactionSystem result = new ReactionSystem(Name);

        result.getReactions().addAll(
                input.getReactions().parallelStream().filter(r ->
                        ((r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both)) &&
                                r.isCatalyzedAndUninhibitedAndHasAllReactants(input.getFoods(), Reaction.Direction.forward)
                                || ((r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both)) &&
                                r.isCatalyzedAndUninhibitedAndHasAllReactants(input.getFoods(), Reaction.Direction.reverse)).collect(Collectors.toList()));
        result.getFoods().setAll(result.computeMentionedFoods(input.getFoods()));
        return result;
    }
}
