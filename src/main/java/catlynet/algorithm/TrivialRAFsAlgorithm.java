/*
 * TrivialRAFsAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.util.CollectionUtils;
import jloda.util.progress.ProgressListener;

import java.util.stream.Collectors;

/**
 * determines all reactions that can run using only the input food set
 * Daniel Huson, 4.2020
 */
public class TrivialRAFsAlgorithm extends AlgorithmBase {
    public static final String Name = "trivial RAFs";


    @Override
    public String getName() {
        return Name;
    }

	@Override
	public String getDescription() {
		return "computes all reactions that can run using only the food set, where the catalyst need not be in the food set if the reaction produces it";
	}

	/**
     * compute all reactions that can run using only the food set
     *
     * @param input - unexpanded catalytic reaction system
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) {
        final ReactionSystem result = new ReactionSystem(Name);

        result.getReactions().addAll(
                input.getReactions().parallelStream()
                        .filter(r -> ((r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both) &&
									  r.isCatalyzedAndUninhibitedAndHasAllReactants(input.getFoods(), CollectionUtils.union(input.getFoods(), r.getProducts()), input.getFoods(), Reaction.Direction.forward))
									 || ((r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both) &&
										 r.isCatalyzedAndUninhibitedAndHasAllReactants(input.getFoods(), CollectionUtils.union(input.getFoods(), r.getReactants()), input.getFoods(), Reaction.Direction.reverse)))
                        .collect(Collectors.toList()));
        result.getFoods().setAll(result.computeMentionedFoods(input.getFoods()));
        return result;
    }
}
