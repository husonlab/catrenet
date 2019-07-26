/*
 * MaxRAFAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * computes a RAF
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MaxRAFAlgorithm implements IModelAlgorithm {
    /**
     * computes the RAF
     *
     * @param input
     * @param result, empty, it none exists
     */
    public void apply(Model input, Model result) {
        result.clear();
        result.setName("Max RAF");

        final Model expanded = input.getExpandedModel();
        final Set<Reaction> inputReactions = new TreeSet<>(expanded.getReactions());
        final Set<MoleculeType> inputFood = new TreeSet<>(expanded.getFoods());
        final Set<MoleculeType> mentionedFood = filterFood(inputFood, inputReactions);

        if (inputReactions.size() > 0) {
            final ArrayList<Set<Reaction>> reactions = new ArrayList<>();
            final ArrayList<Set<MoleculeType>> foods = new ArrayList<>();

            reactions.add(inputReactions);
            foods.add(mentionedFood);

            int i = 0;

            // if(!input.getName().contains("importance")) System.err.println("Running MaxRAF algorithm...");
            do {
                // if(!input.getName().contains("importance")) System.err.println("i=" + i + ":" + Basic.toString(reactions.get(i), ", ") + " Food: " + Basic.toString(foods.get(i), " "));

                final Set<MoleculeType> extendedFood = extendFood(foods.get(i), reactions.get(i), true, false);
                final Set<Reaction> filteredReactions = filterReactions(extendedFood, reactions.get(i));

                reactions.add(filteredReactions);
                foods.add(extendedFood);


                i++;
            }
            while (reactions.get(i).size() < reactions.get(i - 1).size());

            // if(!input.getName().contains("importance")) System.err.println("Final:" + Basic.toString(reactions.get(i - 1), ", ") + " Food: " + Basic.toString(foods.get(i - 1), " "));

            if (reactions.get(i).size() > 0) {
                result.getReactions().setAll(Model.compress(reactions.get(i)));
                result.getFoods().setAll(filterFood(input.getFoods(), reactions.get(i)));
            }
        }
    }

    /**
     * extend a food set by applying all reactions
     *
     * @param foodSet
     * @param reactions
     * @param requireCatalyst
     * @return extended food set
     */
    public static Set<MoleculeType> extendFood(Set<MoleculeType> foodSet, Set<Reaction> reactions, boolean requireReactants, boolean requireCatalyst) {
        final Set<MoleculeType> extendedFoodSet = new TreeSet<>(foodSet);
        while (true) {
            int size = extendedFoodSet.size();
            for (Reaction reaction : reactions) {
                if ((!requireReactants || extendedFoodSet.containsAll(reaction.getReactants())) && (!requireCatalyst || Basic.intersects(extendedFoodSet, reaction.getCatalysts()))) {
                    extendedFoodSet.addAll(reaction.getProducts());
                }
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
    public static Set<Reaction> filterReactions(Set<MoleculeType> food, Set<Reaction> reactions) {
        final Set<Reaction> filteredReactions = new TreeSet<>();
        for (Reaction reaction : reactions) {
            if (food.containsAll(reaction.getReactants()) && Basic.intersects(food, reaction.getCatalysts()))
                filteredReactions.add(reaction);
        }
        return filteredReactions;
    }

    public static Set<MoleculeType> filterFood(Collection<MoleculeType> foodSet, Collection<Reaction> reactions) {
        final Set<MoleculeType> filteredFood = new TreeSet<>();

        for (Reaction reaction : reactions) {
            filteredFood.addAll(Basic.intersection(foodSet, reaction.getReactants()));
            filteredFood.addAll(Basic.intersection(foodSet, reaction.getCatalysts()));
            filteredFood.addAll(Basic.intersection(foodSet, reaction.getProducts()));
        }
        return filteredFood;
    }
}
