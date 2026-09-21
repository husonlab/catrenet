/*
 *  RunMaxURAFs.java Copyright (C) 2026 Daniel H. Huson
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

package catrenet.action;

import catrenet.algorithm.MaxURAFsAlgorithm;
import catrenet.io.ModelIO;
import catrenet.model.ReactionSystem;
import catrenet.window.MainWindow;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jloda.fx.util.AService;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.NotificationManager;

import java.util.Collection;

/**
 * computes all maximal uRAFs and lists them in a text tab
 * Daniel Huson, 9.2026
 */
public class RunMaxURAFs {
    /**
     * warn (and require confirmation) above this many inhibiting elements, since the algorithm
     * examines 2^k subsets of the k inhibiting elements
     */
    public static final int INHIBITING_ELEMENTS_WARNING_THRESHOLD = 12;

    /**
     * compute all maximal uRAFs and list them in a text tab
     */
    public static void apply(MainWindow window, ChangeListener<Boolean> runningListener) {
        if (!VerifyInput.verify(window))
            return;

        final var input = window.getInputReactionSystem();

        if (!input.isInhibitorsPresent()) {
            NotificationManager.showWarning("Won't compute uRAFs, no inhibitions present");
            return;
        }

        final var k = MaxURAFsAlgorithm.inhibitingElements(input).size();
        if (k > INHIBITING_ELEMENTS_WARNING_THRESHOLD) {
            final var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Compute all maximal uRAFs");
            alert.setHeaderText("Large number of inhibiting elements");
            alert.setContentText(String.format("There are %d inhibiting elements, so up to 2^%d = %,d reaction subsets must be examined.%nThis may take a long time. Proceed?",
                    k, k, (1L << Math.min(k, 62))));
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().addAll(ProgramProperties.getProgramIconsFX());
            final var choice = alert.showAndWait();
            if (choice.isEmpty() || choice.get() != ButtonType.OK)
                return;
        }

        final var service = new AService<Collection<ReactionSystem>>(window.getController().getBottomFlowPane());
        service.setCallable(() -> new MaxURAFsAlgorithm().apply(input, service.getProgressListener()));
        service.runningProperty().addListener(runningListener);
        service.setOnSucceeded(c -> {
            final var results = service.getValue();

            final var headLine = results.isEmpty() ? "No uRAF found"
                    : String.format("Found %d maximal uRAF%s", results.size(), results.size() == 1 ? "" : "s");
            NotificationManager.showInformation(headLine);
            window.getLogStream().println("\n" + headLine);

            final var notation = window.getDocument().getReactionNotation();
            final var arrowNotation = window.getDocument().getArrowNotation();

            final var buf = new StringBuilder();
            buf.append("# ").append(headLine).append("\n\n");
            if (!results.isEmpty()) {
                buf.append("Food: ").append(ModelIO.getFoodString(input, notation)).append("\n\n");
                var count = 0;
                for (var uRAF : results) {
                    buf.append(String.format("# Maximal U RAF %d has %d reactions\n\n", ++count, uRAF.size()));
                    buf.append(ModelIO.toString(uRAF, false, notation, arrowNotation));
                    buf.append("\n\n");
                }
            }

            final var textTab = window.getTabManager().getTextTab(MaxURAFsAlgorithm.Name, null);
            textTab.getTextArea().setText(buf.toString());
            window.getController().getOutputTabPane().getSelectionModel().select(textTab);
        });
        service.start();
    }
}
