/*
 * QuotientRAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.util.progress.ProgressSilent;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * compute the quotient RAF
 * Daniel Huson, 4.2020
 */
public class QuotientRAFAlgorithm extends AlgorithmBase {
    public static final String Name = "Quotient RAF";

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "computes the Max RAF minus all the reactions from the Max CAF and adds all produces of the Max CAF to the food set [SXH20]";
    }

    /**
     * compute the quotient max RAF
     *
	 */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress.setTasks("Compute quotient RAF", "");
        progress.setMaximum(3);
        progress.setProgress(0);

        final ReactionSystem maxRAF = new MaxRAFAlgorithm().apply(input, new ProgressSilent());
        progress.setProgress(1);

        final ReactionSystem maxCAF = new MaxCAFAlgorithm().apply(maxRAF, new ProgressSilent());
        progress.setProgress(2);

        final ReactionSystem result = maxRAF.shallowCopy();
        result.setName(Name);

        result.getReactions().removeAll(maxCAF.getReactions());

        final Set<MoleculeType> products = new TreeSet<>();
        products.addAll(maxCAF.getReactions().stream().filter(r -> r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both).map(Reaction::getProducts).flatMap(Collection::stream).collect(Collectors.toList()));
        products.addAll(maxCAF.getReactions().stream().filter(r -> r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both).map(Reaction::getReactants).flatMap(Collection::stream).collect(Collectors.toList()));
        products.addAll(result.getFoods()); // these two lines ensures alphabetical order

        result.getFoods().setAll(products);
        progress.setProgress(3);
        return result;
    }
}
