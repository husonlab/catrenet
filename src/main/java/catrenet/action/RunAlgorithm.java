/*
 * RunAlgorithm.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.action;

import catrenet.algorithm.AlgorithmBase;
import catrenet.algorithm.Importance;
import catrenet.algorithm.MuCAFAlgorithm;
import catrenet.io.ModelIO;
import catrenet.model.ReactionSystem;
import catrenet.window.MainWindow;
import catrenet.window.MainWindowController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.StringUtils;
import jloda.util.Triplet;

/**
 * runs an algorithm
 * Daniel Huson, 7.2019
 */
public class RunAlgorithm {
    /**
     * run an algorithm, return the resulting model and write to text area
     *
	 */
    public static void apply(MainWindow window, final ReactionSystem inputReactions, AlgorithmBase algorithm, ChangeListener<Boolean> runningListener, boolean updateParsedInputTab) {
        final MainWindowController controller = window.getController();

        if (updateParsedInputTab) {
            controller.getParsedReactionsTextArea().setText(String.format("# Input has %,d reactions (%,d two-way and %,d one-way) on %,d food items\n\n%s",
                    inputReactions.size(), inputReactions.getNumberOfTwoWayReactions(), inputReactions.getNumberOfOneWayReactions(), inputReactions.getFoodSize(),
                    ModelIO.toString(window.getInputReactionSystem().sorted(), true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation())));

            WarnAboutMissingMoleculesOrUnusedFood.run(window);
        }

        var result = window.getReactionSystem(algorithm.getName());
		var service = new AService<Triplet<ReactionSystem, String, String>>(controller.getBottomFlowPane());
        service.setCallable(() -> {
            final ReactionSystem outputReactions = algorithm.apply(inputReactions, service.getProgressListener());

            if (controller.getComputeImportanceCheckMenuItem().isSelected() && !(algorithm instanceof MuCAFAlgorithm)) {
                var infoLine1 = Importance.toStringFoodImportance(Importance.computeFoodImportance(inputReactions, outputReactions, algorithm, service.getProgressListener()));
                var infoLine2 = Importance.toStringReactionImportance(Importance.computeReactionImportance(inputReactions, outputReactions, algorithm, service.getProgressListener()));
                return new Triplet<>(outputReactions, infoLine1, infoLine2);

            } else {
                return new Triplet<>(outputReactions, null, null);
            }
        });

        service.runningProperty().addListener(runningListener);

        service.setOnRunning(c -> service.getProgressListener().setTasks(StringUtils.fromCamelCase(Basic.getShortName(algorithm.getClass())), ""));

        service.setOnSucceeded(c -> {
            var triplet = service.getValue();

            result.shallowCopy(triplet.getFirst());

            if (result.size() > 0) {
                var infoLine1 = triplet.getSecond();
                var infoLine2 = triplet.getThird();

                NotificationManager.showInformation(result.getHeaderLine());

                var textTab = window.getTabManager().getTextTab(result.getName(), result);

                controller.getOutputTabPane().getSelectionModel().select(textTab);

                window.getLogStream().println("\n\n" + result.getHeaderLine());
                if (infoLine1 != null && infoLine2 != null) {
                    window.getLogStream().println(infoLine1);
                    window.getLogStream().println(infoLine2);
                }
            } else {
                window.getLogStream().println("\n\nNo " + result.getName());
                NotificationManager.showInformation("No " + result.getName());
                window.getTabManager().clear(algorithm.getName());
                Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
            }
        });
        service.start();
    }
}
