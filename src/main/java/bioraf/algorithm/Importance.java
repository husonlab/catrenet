/*
 * Importance.java Copyright (C) 2024 Daniel H. Huson
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

package bioraf.algorithm;

import bioraf.model.MoleculeType;
import bioraf.model.Reaction;
import bioraf.model.ReactionSystem;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.util.ArrayList;

/**
 * computes importance of food items and reactions
 * Daniel Huson, 7.2019
 */
public class Importance implements IDescribed {

    public String getDescription() {
        return "computes the percent difference between model size and model size without given food item [HS23]";
    }
    /**
     * computes food importance
     *
     * @return list of food, importance pairs, in order of decreasing importance (percent difference between model size and model size without given food item)
     */
    public static ArrayList<Pair<MoleculeType, Float>> computeFoodImportance(ReactionSystem inputSystem, ReactionSystem originalResult, AlgorithmBase algorithm, ProgressListener progress) throws CanceledException {
        final var result = new ArrayList<Pair<MoleculeType, Float>>();

        progress.setTasks(StringUtils.fromCamelCase(Basic.getShortName(algorithm.getClass())), "importance");
        progress.setMaximum(inputSystem.getFoods().size());
        progress.setMaximum(10000000);
        progress.setProgress(0);
        final var increment = 5000000 / inputSystem.getFoods().size();

        for (var food : inputSystem.getFoods()) {
            final var replicateInput = inputSystem.shallowCopy();
            replicateInput.setName("Food importance");
            replicateInput.getFoods().remove(food);
            final var replicateOutput = algorithm.apply(replicateInput, new ProgressSilent());

            final var importance = 100f * (originalResult.size() - replicateOutput.size()) / (float) originalResult.size();
            if (importance > 0)
                result.add(new Pair<>(food, importance));
            progress.setProgress(progress.getProgress() + increment);
        }
        result.sort((a, b) -> -Float.compare(a.getSecond(), b.getSecond()));
        return result;
    }

    /**
     * computes reaction importance
     *
     * @return list of reaction, importance pairs, in order of decreasing importance (difference between model size and model size without given reaction)
     */
    public static ArrayList<Pair<Reaction, Float>> computeReactionImportance(ReactionSystem inputSystem, ReactionSystem originalResult, AlgorithmBase algorithm, ProgressListener progress) throws CanceledException {
        final var result = new ArrayList<Pair<Reaction, Float>>();

        if (originalResult.size() == 1) {
            result.add(new Pair<>(originalResult.getReactions().get(0), 100f));
        } else if (originalResult.size() > 1) {
            progress.setTasks(StringUtils.fromCamelCase(Basic.getShortName(algorithm.getClass())), "importance");
            progress.setMaximum(inputSystem.getFoods().size());
            progress.setMaximum(10000000);
            progress.setProgress(5000000);
            final var increment = 5000000 / inputSystem.getReactions().size();

            final var sizeToCompareAgainst = originalResult.size() - 1;

            for (var reaction : inputSystem.getReactions()) {
                final var replicateInput = inputSystem.shallowCopy();
                replicateInput.setName("Reaction importance");
                replicateInput.getReactions().remove(reaction);
                final var replicateOutput = algorithm.apply(replicateInput, new ProgressSilent());
                if (replicateOutput.size() < sizeToCompareAgainst) {
                    final var importance = 100f * (sizeToCompareAgainst - replicateOutput.size()) / sizeToCompareAgainst;
                    if (importance > 0)
                        result.add(new Pair<>(reaction, importance));
                }
                result.sort((a, b) -> -Float.compare(a.getSecond(), b.getSecond()));
                progress.setProgress(progress.getProgress() + increment);
            }
        }
        return result;
    }

    /**
     * pretty print food importance
     *
     * @return food importance string
     */
    public static String toStringFoodImportance(ArrayList<Pair<MoleculeType, Float>> foodImportance) {
        final var buf = new StringBuilder();

        buf.append("Food importance: ");
        var first = true;
        for (var pair : foodImportance) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(String.format("%s %.0f%%", pair.getFirst().getName(), pair.getSecond()));
        }
        return buf.toString();
    }

    /**
     * pretty print reaction importance
     *
     * @return food importance string
     */
    public static String toStringReactionImportance(ArrayList<Pair<Reaction, Float>> reactionImportance) {
        final var buf = new StringBuilder();

        buf.append("Reaction importance: ");
        var first = true;
        for (var pair : reactionImportance) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(String.format("%s %.0f%%", pair.getFirst().getName(), pair.getSecond()));
        }
        return buf.toString();
    }
}
