/*
 * ExportReactionsForSelectedNodesDialog.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.dialog;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import jloda.util.CollectionUtils;
import jloda.util.StringUtils;

import java.util.HashSet;

/**
 * export dialog for nodes selected in network
 * Daniel Huson, 8.203
 */
public class ExportReactionsForSelectedNodesDialog {

    public static void apply(MainWindow window) {
        final var graphView = window.getReactionGraphView();

        final var food = new HashSet<MoleculeType>();

        final var output = new ReactionSystem("selected");
        graphView.getNodeSelection().getSelectedItems().forEach(v -> {
            if (v.getInfo() instanceof Reaction r) {
                food.addAll(r.getReactants());
				food.addAll(r.getProducts());
				r.getCatalystConjunctions().forEach(c -> food.addAll(MoleculeType.valuesOf(StringUtils.split(c.getName(), '&'))));
				food.addAll(r.getInhibitions());
                output.getReactions().add(r);
            } else if (v.getInfo() instanceof MoleculeType) {
                food.add((MoleculeType) v.getInfo());
            }
        });

        output.getFoods().addAll(CollectionUtils.intersection(window.getInputReactionSystem().getFoods(), food));

        if (!output.getReactions().isEmpty()) {
            ExportReactionsFileDialog.apply(window, output);
        }
    }

}
