/*
 * RunAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.action;

import catlynet.algorithm.AlgorithmBase;
import catlynet.algorithm.Importance;
import catlynet.algorithm.MuCAFAlgorithm;
import catlynet.io.ModelIO;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
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
    public static void apply(MainWindow window, final ReactionSystem inputReactions, AlgorithmBase algorithm, ChangeListener<Boolean> runningListener, boolean updateWorkingInputTextArea) {
        final MainWindowController controller = window.getController();


        if (updateWorkingInputTextArea) {
            controller.getParsedReactionsTextArea().setText(String.format("# Input has %,d reactions (%,d two-way and %,d one-way) on %,d food items\n\n%s",
                    inputReactions.size(), inputReactions.getNumberOfTwoWayReactions(), inputReactions.getNumberOfOneWayReactions(), inputReactions.getFoodSize(),
                    ModelIO.toString(window.getInputReactionSystem().sorted(), true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation())));

            WarnAboutMissingMoleculesOrUnusedFood.run(window);
        }

        var result = window.getReactionSystem(algorithm.getName());
        var service = new AService<Triplet<ReactionSystem, String, String>>(controller.getStatusFlowPane());
        service.setCallable(() -> {
            final ReactionSystem outputReactions = algorithm.apply(inputReactions, service.getProgressListener());

            final String infoLine1;
            final String infoLine2;
            if (algorithm instanceof MuCAFAlgorithm || !window.getController().getComputeImportanceCheckMenuItem().isSelected()) {
                infoLine1 = null;
                infoLine2 = null;
            } else {
                infoLine1 = Importance.toStringFoodImportance(Importance.computeFoodImportance(inputReactions, outputReactions, algorithm, service.getProgressListener()));
                infoLine2 = Importance.toStringReactionImportance(Importance.computeReactionImportance(inputReactions, outputReactions, algorithm, service.getProgressListener()));
            }

            return new Triplet<>(outputReactions, infoLine1, infoLine2);
        });

        service.runningProperty().addListener(runningListener);

        service.setOnRunning(c -> service.getProgressListener().setTasks(StringUtils.fromCamelCase(Basic.getShortName(algorithm.getClass())), ""));

        service.setOnSucceeded(c -> {
            var triplet = service.getValue();

            result.shallowCopy(triplet.getFirst());

            window.getExportManager().addOrReplace(result);

            if (result.size() > 0) {
                var headLine = result.getName() + " has " + result.size() + " reactions"
                               + (result.getNumberOfTwoWayReactions() > 0 ? " (" + result.getNumberOfTwoWayReactions() + " two-way and " + result.getNumberOfOneWayReactions() + " one-way)" : "")
                               + " on " + result.getFoods().size() + " food items";

                var infoLine1 = triplet.getSecond();
                var infoLine2 = triplet.getThird();

                NotificationManager.showInformation(headLine);

                var textArea = window.getTabManager().getTextArea(algorithm.getName());
                window.getTabManager().getTab(algorithm.getName()).disableProperty().bind(result.sizeProperty().isEqualTo(0));
                controller.getOutputTabPane().getSelectionModel().select(window.getTabManager().getTab(algorithm.getName()));

                if (infoLine1 != null && infoLine2 != null) {
                    //final String text="# " + headLine + ":\n# " + infoLine1 + "\n# " + infoLine2 + "\n\n" + ModelIO.toString(result, false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation());
                    var text = "# " + headLine + ":\n# " + infoLine1 + "\n# " + infoLine2 + "\n\n" + StringUtils.toString(result.getReactionNames(), "\n");
                    textArea.setText(text);
                    window.getLogStream().println("\n\n" + headLine);
                    window.getLogStream().println(infoLine1);
                    window.getLogStream().println(infoLine2);
                } else {
                    //final String text= "# " + headLine + "\n\n" + ModelIO.toString(result, false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation());
                    var text = "# " + headLine + "\n\n" + StringUtils.toString(result.getReactionNames(), "\n");
                    textArea.setText(text);
                    window.getLogStream().println("\n" + headLine);
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