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

import catlynet.algorithm.IModelAlgorithm;
import catlynet.algorithm.Importance;
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
    public static void apply(MainWindow window, IModelAlgorithm algorithm, TextArea textArea) {
        final MainWindowController controller = window.getController();

        final AService<Model> service = new AService<>(() -> {
            final Model result = new Model();
            algorithm.apply(window.getModel(), result);
            return result;
        }, controller.getStatusFlowPane());

        service.setOnSucceeded((c) -> {
            final Model result = service.getValue();

            if (result.getReactions().size() > 0) {
                final String headLine;
                if (result.getNumberOfTwoWayReactions() > 0) {
                    headLine = result.getName() + " has " + result.size() + " reactions (" + result.getNumberOfTwoWayReactions() + " two-way and " + result.getNumberOfOneWayReactions() + " one-way)";
                } else {
                    headLine = result.getName() + " has " + result.size() + " reactions";
                }

                final String infoLine1 = Importance.toStringFoodImportance(Importance.computeFoodImportance(result, algorithm));
                final String infoLine2 = Importance.toStringReactionImportance(result, Importance.computeReactionImportance(result, algorithm));


                NotificationManager.showInformation(headLine);

                textArea.setText("# " + headLine + ":\n# " + infoLine1 + "\n# " + infoLine2 + "\n\n" + ModelIO.toString(result, true, true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));

                window.getLogStream().println("\n" + headLine);
                window.getLogStream().println(infoLine1);
                window.getLogStream().println(infoLine2);
            } else
                textArea.setText("# No " + result.getName() + "\n");
        });
        service.start();
    }
}
