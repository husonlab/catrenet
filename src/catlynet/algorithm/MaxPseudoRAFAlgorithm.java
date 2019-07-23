/*
 * MaxPseudoRAFAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import static catlynet.algorithm.MaxRAFAlgorithm.*;

/**
 * computes a pseudo-RAF
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MaxPseudoRAFAlgorithm implements IModelAlgorithm {
    /**
     * computes a pseudo-RAF
     *
     * @param input
     * @param result
     */
    public void apply(Model input, Model result) {
        result.clear();
        result.setName("Max Pseudo-RAF");

        final Set<Reaction> inputReactions = new TreeSet<>(input.getReactions());
        final Set<MoleculeType> inputFood = new TreeSet<>(input.getFoods());
        final Set<MoleculeType> mentionedFood = filterFood(inputFood, inputReactions);

        final Set<MoleculeType> startingFood = extendFood(mentionedFood, inputReactions, false, false);

        if (inputReactions.size() > 0) {
            final ArrayList<Set<Reaction>> reactions = new ArrayList<>();
            final ArrayList<Set<MoleculeType>> foods = new ArrayList<>();

            reactions.add(inputReactions);
            foods.add(startingFood);

            int i = 0;

            do {
                final Set<MoleculeType> extendedFood = extendFood(inputFood, reactions.get(i), false, false);
                final Set<Reaction> filteredReactions = filterReactions(extendedFood, reactions.get(i));

                reactions.add(filteredReactions);
                foods.add(extendedFood);


                // System.err.println("i=" + i + ":" + Basic.toString(reactions.get(i), ", ") + " Food: " + Basic.toString(foods.get(i), " "));

                i++;
            }
            while (reactions.get(i).size() > reactions.get(i - 1).size());

            //System.err.println("Final:" + Basic.toString(reactions.get(i - 1), ", ") + " Food: " + Basic.toString(foods.get(i - 1), " "));

            if (reactions.get(i).size() > 0) {
                result.getReactions().setAll(reactions.get(i));
                result.getFoods().setAll(foods.get(i));
            }
        }
    }
}
