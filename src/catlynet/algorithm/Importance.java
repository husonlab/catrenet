/*
 * Importance.java Copyright (C) 2019. Daniel H. Huson
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
import jloda.util.Pair;

import java.util.ArrayList;

/**
 * computes importance of food items and reactions
 */
public class Importance {
    /**
     * computes food importance
     *
     * @param model
     * @param algorithm
     * @return list of food, immportance pairs, in order of decreasing importance (percent difference between model size and model size without given food item)
     */
    public static ArrayList<Pair<MoleculeType, Float>> computeFoodImportance(Model model, ModelAlgorithmBase algorithm) {
        final ArrayList<Pair<MoleculeType, Float>> result = new ArrayList<>();
        final Model expandedModel = model.getExpandedModel();

        for (MoleculeType food : model.getFoods()) {
            final Model expandedInputModel = expandedModel.shallowCopy();
            expandedInputModel.setName("Food importance");
            expandedInputModel.getFoods().remove(food);
            final Model outputModel = new Model();
            algorithm.apply(expandedInputModel, outputModel);
            final float importance = 100f * (expandedInputModel.size() - outputModel.size()) / (float) expandedInputModel.size();
            if (importance > 0)
                result.add(new Pair<>(food, importance));
        }
        result.sort((a, b) -> -Float.compare(a.getSecond(), b.getSecond()));
        return result;
    }

    /**
     * computes reaction importance
     *
     * @param model
     * @param algorithm
     * @return list of reaction, immportance pairs, in order of decreasing importance (difference between model size and model size without given reaction)
     */
    public static ArrayList<Pair<Reaction, Float>> computeReactionImportance(Model model, ModelAlgorithmBase algorithm) {
        final ArrayList<Pair<Reaction, Float>> result = new ArrayList<>();

        if (model.getReactions().size() > 1) {
            for (Reaction reaction : model.getReactions()) {
                final Model inputModel = model.shallowCopy();
                inputModel.getReactions().remove(reaction); // need to first remove reaction, then expand
                final Model expandedInputModel = inputModel.getExpandedModel();
                expandedInputModel.setName("Reaction importance");
                expandedInputModel.getReactions().remove(reaction);
                final Model outputModel = new Model();
                algorithm.apply(expandedInputModel, outputModel);

                // System.err.println(reaction.getName()+" "+expandedInputModel.size()+" -> "+outputModel.size());

                final float importance = 100f * (expandedInputModel.size() - outputModel.size()) / (float) expandedInputModel.size();
                if (importance > 0)
                    result.add(new Pair<>(reaction, importance));
            }
            result.sort((a, b) -> -Float.compare(a.getSecond(), b.getSecond()));
        }

        return result;
    }

    /**
     * pretty print food importance
     *
     * @param foodImportance
     * @return food importance string
     */
    public static String toStringFoodImportance(ArrayList<Pair<MoleculeType, Float>> foodImportance) {
        final StringBuilder buf = new StringBuilder();

        buf.append("Food importance: ");
        boolean first = true;
        for (Pair<MoleculeType, Float> pair : foodImportance) {
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
     * @param reactionImportance
     * @return food importance string
     */
    public static String toStringReactionImportance(ArrayList<Pair<Reaction, Float>> reactionImportance) {
        final StringBuilder buf = new StringBuilder();

        buf.append("Reaction importance: ");
        boolean first = true;
        for (Pair<Reaction, Float> pair : reactionImportance) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(String.format("%s %.0f%%", pair.getFirst().getName(), pair.getSecond()));
        }
        return buf.toString();
    }
}
