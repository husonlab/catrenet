/*
 * IModelAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.model.Model;
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import jloda.util.Basic;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * computes a new reaction model from an old one
 * Daniel Huson, 7.2019
 */
public abstract class ModelAlgorithmBase {
    /**
     * run the algorithm
     *
     * @param input
     * @param output
     */
    abstract public void apply(Model input, Model output);

    /**
     * extend a food set by add all reaction products (disregarding reactants, catalysts and inhibitors)
     *
     * @param foodSet
     * @param reactions
     * @return extended food set
     */
    protected Set<MoleculeType> extendFood(Collection<MoleculeType> foodSet, Collection<Reaction> reactions) {
        final Set<MoleculeType> extendedFoodSet = new TreeSet<>(foodSet);
        while (true) {
            int size = extendedFoodSet.size();
            for (Reaction reaction : reactions) {
                extendedFoodSet.addAll(reaction.getProducts());
            }
            if (extendedFoodSet.size() == size)
                break;
        }
        return extendedFoodSet;
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

    /**
     * filter food so as to only return food mentioned in the reactions
     *
     * @param foodSet
     * @param reactions
     * @return filtered food
     */
    protected Set<MoleculeType> filterFood(Collection<MoleculeType> foodSet, Collection<Reaction> reactions) {
        final Set<MoleculeType> filteredFood = new TreeSet<>();

        for (Reaction reaction : reactions) {
            filteredFood.addAll(Basic.intersection(foodSet, reaction.getReactants()));
            filteredFood.addAll(Basic.intersection(foodSet, reaction.getCatalysts()));
            filteredFood.addAll(Basic.intersection(foodSet, reaction.getProducts()));
        }
        return filteredFood;
    }
}
