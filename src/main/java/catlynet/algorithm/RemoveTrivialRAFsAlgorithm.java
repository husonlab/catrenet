/*
 * RemoveTrivialRAFsAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * remove all trivial RAFs
 * Daniel Huson, 4.2020
 */
public class RemoveTrivialRAFsAlgorithm extends AlgorithmBase {
    public static final String Name = "nontrivial RAFs";

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return " computes CRS that is obtained by removing all trivial RAFs";
    }

    /**
     * remove all trivial RAFs
     *
	 */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress.setTasks("Remove all trivial RAFs", "");

        final Set<MoleculeType> food = new TreeSet<>(input.getFoods());
        final ReactionSystem result = new ReactionSystem(Name);

        progress.setMaximum(input.size());
        progress.setProgress(0);

        // look at all reactions in the input (in parallel) and keep any that is not a trivial RAF
        result.getReactions().addAll(input.getReactions().parallelStream().filter(r -> {
            try {
                progress.incrementProgress();

                final Set<MoleculeType> foodAndProducts = new HashSet<>();
                if ((r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both) && food.containsAll(r.getReactants())) {
                    foodAndProducts.addAll(food);
                    foodAndProducts.addAll(r.getProducts());
                } else if ((r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both) && food.containsAll(r.getProducts())) {
                    foodAndProducts.addAll(food);
                    foodAndProducts.addAll(r.getReactants());
                } else
                    return true; // reactants not present (in either direction)

                // compute the conjunctive normal form and check that no conjunction exists for which all catalysts are in the food and products set:
                return r.getCatalystConjunctions().parallelStream().map(c -> MoleculeType.valuesOf(c.getName().split("&"))).noneMatch(foodAndProducts::containsAll);
            } catch (CanceledException ignore) {
                return false;
            }
        }).collect(Collectors.toList()));

        result.getFoods().setAll(result.computeMentionedFoods(food));

        return result;
    }
}
