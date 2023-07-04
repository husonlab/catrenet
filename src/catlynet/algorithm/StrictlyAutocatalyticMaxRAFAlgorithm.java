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
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.TreeSet;

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

    /**
     * computes the partially autocatalytic Max RAF.
     * Ignore all inhibitions.
     * First remove any food items mentioned as a catalyst
     *
     * @returns result, empty, it none exists
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        if (true) { // filter, then compute MaxRAF
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
                        var newReaction = new Reaction(reaction);
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
        } else { // For sanity checks: compute maxRAF, then filter, then compute maxRAF again....
            var maxRAF = (new MaxRAFAlgorithm()).apply(input, progress);
            final var inputFood = new TreeSet<>(input.getFoods());
            final var filteredReactions = new TreeSet<Reaction>();
            for (var reaction : maxRAF.getReactions()) {
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
                        var newReaction = new Reaction(reaction);
                        filteredReactions.add(reaction);
                    }
                }
            }
            var filteredSystem = new ReactionSystem(getName() + "MaxRAF-Filter");
            filteredSystem.getFoods().addAll(inputFood);
            filteredSystem.getReactions().addAll(filteredReactions);
            if (false)
                return filteredSystem;
            else {
                var maxRAF2 = (new MaxRAFAlgorithm()).apply(filteredSystem, progress);
                maxRAF2.setName(getName() + "MaxRAF-Filter-MaxRAF");
                return maxRAF2;

            }
        }
    }
}
