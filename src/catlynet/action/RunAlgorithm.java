/*
 * RunAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.action;

import catlynet.algorithm.Importance;
import catlynet.algorithm.ModelAlgorithmBase;
import catlynet.io.ModelIO;
import catlynet.model.Model;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.scene.control.TextArea;
import jloda.fx.util.AService;
import jloda.fx.util.NotificationManager;

/**
 * runs an algorithm
 * Daniel Huson, 7.2019
 */
public class RunAlgorithm {
    /**
     * run an algorithm, return the resulting model and write to text area
     *
     * @param window
     * @param algorithm
     * @param result
     * @param textArea
     */
    public static void apply(MainWindow window, final Model inputModel, ModelAlgorithmBase algorithm, final Model result, TextArea textArea) {
        final MainWindowController controller = window.getController();
        result.clear();

        final AService<Model> service = new AService<>(() -> {
            final Model outputModel = new Model();
            algorithm.apply(inputModel, outputModel);
            return outputModel;
        }, controller.getStatusFlowPane());

        service.setOnSucceeded((c) -> {
            result.shallowCopy(service.getValue());

            if (result.size() > 0) {
                final String headLine = result.getName() + " has " + result.size() + " reactions"
                        + (result.getNumberOfTwoWayReactions() > 0 ? " (" + result.getNumberOfTwoWayReactions() + " two-way and " + result.getNumberOfOneWayReactions() + " one-way)":"");

                final String infoLine1 = Importance.toStringFoodImportance(Importance.computeFoodImportance(result, algorithm));
                final String infoLine2 = Importance.toStringReactionImportance(Importance.computeReactionImportance(result, algorithm));

                NotificationManager.showInformation(headLine);

                textArea.setText("# " + headLine + ":\n# " + infoLine1 + "\n# " + infoLine2 + "\n\n" + ModelIO.toString(result, false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));

                window.getLogStream().println("\n" + headLine);
                window.getLogStream().println(infoLine1);
                window.getLogStream().println(infoLine2);
            } else {
                window.getLogStream().println("\nNo " + result.getName());
                textArea.setText("# No " + result.getName() + "\n");
            }
        });
        service.start();
    }
}
