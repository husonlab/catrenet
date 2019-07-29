/*
 * AlgorithmBase.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * computes a new reaction model from an old one
 * Daniel Huson, 7.2019
 */
public abstract class AlgorithmBase {
    /**
     * run the algorithm
     *
     * @param input
     * @return output
     */
    abstract public ReactionSystem apply(ReactionSystem input);

    /**
     * add molecules mentioned as products to the given list of existing molecules
     *
     * @param molecules existing molecules
     * @param reactions
     * @return extended food set
     */
    protected Set<MoleculeType> addAllMentionedProducts(Collection<MoleculeType> molecules, Collection<Reaction> reactions) {
        final Set<MoleculeType> products = new TreeSet<>(molecules);
        for (Reaction reaction : reactions) {
            products.addAll(reaction.getProducts());
            }
        return products;
    }

    /**
     * filter reactions to only keep those that can be run given the current food
     *
     * @param food
     * @param reactions
     * @return filtered reactions
     */
    protected Set<Reaction> filterReactions(Collection<MoleculeType> food, Collection<Reaction> reactions) {
        final Set<Reaction> filteredReactions = new TreeSet<>();
        for (Reaction reaction : reactions) {
            if (food.containsAll(reaction.getReactants()) && Basic.intersects(food, reaction.getCatalysts()))
                filteredReactions.add(reaction);
        }
        return filteredReactions;
    }
}
