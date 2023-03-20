/*
 * Utilities.java Copyright (C) 2022 Daniel H. Huson
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * some utilities
 * Daniel Huson, 4.2020
 */
public class Utilities {
    /**
     * add molecules mentioned as products to the given list of existing molecules
     *
     * @param molecules existing molecules
     * @return extended food set
     */
    public static Set<MoleculeType> addAllMentionedProducts(Collection<MoleculeType> molecules, Collection<Reaction> reactions) {
        final Set<MoleculeType> result = new TreeSet<>(molecules);
        result.addAll(reactions.stream().filter(r -> r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both).map(Reaction::getProducts).flatMap(Collection::stream).collect(Collectors.toList()));
        result.addAll(reactions.stream().filter(r -> r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both).map(Reaction::getReactants).flatMap(Collection::stream).collect(Collectors.toList()));

        return result;
    }

    /**
     * gets the closure of the set of molecules with respect to the set of reactions, ignoring catalysts and inhibitors
     *
     * @param molecules existing molecules
     * @return extended food set
     */
    public static Set<MoleculeType> computeClosure(Collection<MoleculeType> molecules, Collection<Reaction> reactions) {
        final Set<MoleculeType> allMolecules = new TreeSet<>(molecules);
        int size;
        do {
            size = allMolecules.size();
            allMolecules.addAll(reactions.stream().filter(r -> r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both).
                    filter(r -> allMolecules.containsAll(r.getReactants())).map(Reaction::getProducts).flatMap(Collection::stream).collect(Collectors.toList()));
            allMolecules.addAll(reactions.stream().filter(r -> r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both).
                    filter(r -> allMolecules.containsAll(r.getProducts())).map(Reaction::getReactants).flatMap(Collection::stream).collect(Collectors.toList()));
        }
        while (allMolecules.size() > size);
        return allMolecules;
    }

    /**
     * filter reactions to only keep those that can be run given the current food
     *
     * @return filtered reactions
     */
    public static Set<Reaction> filterReactions(Collection<MoleculeType> food, Collection<Reaction> reactions) {
        return reactions.stream().filter(r -> r.isCatalyzedAndUninhibitedAndHasAllReactants(food, r.getDirection())).collect(Collectors.toSet());
    }

    /**
     * for a given food set and set of reactions, returns all reactions that are F-generated (ignoring catalysts and inhibitors
     *
     * @param foods     the food set
     * @param reactions the input reactions
     * @return the F-generated reactions
     */
    public static List<Reaction> computeFGenerated(List<MoleculeType> foods, List<Reaction> reactions) {
        var availableFood = new HashSet<>(foods);
        var availableReactions = new ArrayList<>(reactions);
        var closure = new ArrayList<Reaction>();
        while (true) {
            var toAdd = availableReactions.stream().filter(r -> r.isHasAllReactants(availableFood, r.getDirection())).collect(Collectors.toList());
            if (toAdd.size() > 0) {
                closure.addAll(toAdd);
                for (var r : toAdd) {
                    availableFood.addAll(switch (r.getDirection()) {
                        case forward -> r.getProducts();
                        case reverse -> r.getReactants();
                        case both -> {
                            var result = new ArrayList<MoleculeType>();
                            if (availableFood.containsAll(r.getReactants()))
                                result.addAll(r.getProducts());
                            if (availableFood.containsAll(r.getProducts()))
                                result.addAll(r.getReactants());
                            yield result;
                        }
                    });
                }
                availableReactions.removeAll(toAdd);
            } else
                break;
        }
        return closure;
    }
}
