/*
 * MaxRAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.util.CollectionUtils;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.*;

/**
 * computes the partially autocatalytic Max RAF.
 * Daniel Huson, 7.2023
 * Based on notes by Mike Steel
 */
public class StrictlyAutocatalyticMaxRAFAlgorithm extends AlgorithmBase {
    public static final String Name = "Strictly Autocatalytic Max RAF";

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "computes a Max RAF that has the additional property that any contained reaction requires at least one molecule type for catalyzation that is not in the food set [HXRS23]";
    }

    /**
     * computes the partially autocatalytic Max RAF.
     * Ignore all inhibitions.
     * First remove any food items mentioned as a catalyst
     *
     * @returns result, empty, it none exists
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        if (true) {
            final var result = new ReactionSystem();
            result.setName(Name);

            final var inputReactions = new TreeSet<>(input.getReactions());
            final var inputFood = new TreeSet<>(input.getFoods());

            if (inputReactions.size() > 0) {
                final var reactions = new ArrayList<Set<Reaction>>();
                final var molecules = new ArrayList<Set<MoleculeType>>();

                reactions.add(0, inputReactions);
                molecules.add(0, inputFood);

                progress.setMaximum(100);
                progress.setProgress(0);

                var i = 0;
                do {
                    if (false) {
                        System.err.println("i=" + i + ":");
                        System.err.println("Molecules: " + StringUtils.toString(molecules.get(i), " "));
                        System.err.println("Reactions: " + StringUtils.toString(reactions.get(i), " "));
                    }

                    var previousMolecules = molecules.get(i);
                    var previousReactions = reactions.get(i);
                    var closure = Utilities.computeClosure(previousMolecules, previousReactions);
                    var closureWithoutFood = CollectionUtils.difference(closure, inputFood);

                    i++;
                    molecules.add(new HashSet<>());
                    var nextMolecules = molecules.get(i);
                    reactions.add(new HashSet<>());
                    var nextReactions = reactions.get(i);
                    for (var reaction : previousReactions) {
                        if (closure.containsAll(reaction.getReactants())) {
                            if (Arrays.stream(reaction.getCatalysts().split("[,&\\s]")).anyMatch(c -> closureWithoutFood.contains(MoleculeType.valueOf(c)))) {
                                nextReactions.add(reaction);
                                nextMolecules.addAll(inputFood);
                                nextMolecules.addAll(reaction.getProducts());
                            }

                        }
                    }
                    progress.setProgress(Math.min(100, reactions.size()));
                }
                while (reactions.get(i).size() < reactions.get(i - 1).size());

                if (reactions.get(i).size() > 0) {
                    result.getReactions().setAll(reactions.get(i));
                    result.getFoods().setAll(result.computeMentionedFoods(input.getFoods()));
                }
            }
            return result;

        } else {
            final var inputFood = new TreeSet<>(input.getFoods());
            final var filteredReactions = new TreeSet<Reaction>();
            for (var reaction : input.getReactions()) {
                if (!inputFood.containsAll(reaction.getCatalystElements())) {
                    var ok = false;
                    loop:
                    for (var cat : reaction.getCatalysts().split("[,\\s]")) {
                        cat = cat.trim();
                        for (var part : StringUtils.split(cat, '&')) {
                            if (!inputFood.contains(MoleculeType.valueOf(part))) {
                                ok = true;
                                break loop;
                            }
                        }
                    }
                    if (ok) {
                        filteredReactions.add(reaction);
                    }
                }
            }
            var filteredSystem = new ReactionSystem(input.getName());
            filteredSystem.getFoods().addAll(inputFood);
            filteredSystem.getReactions().addAll(filteredReactions);

            var maxRAF = (new MaxRAFAlgorithm()).apply(filteredSystem, progress);
            maxRAF.setName(getName());
            return maxRAF;
        }
    }
}
