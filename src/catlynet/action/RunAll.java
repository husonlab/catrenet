/*
 * RunAll.java Copyright (C) 2020. Daniel H. Huson
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

import catlynet.algorithm.*;
import catlynet.io.ModelIO;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.value.ChangeListener;

import java.text.SimpleDateFormat;

/**
 * run all algorithms
 * Daniel Huson, 2.2020
 */
public class RunAll {
    public static void apply(MainWindow window, MainWindowController controller, ChangeListener<Boolean> runningListener) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        window.getLogStream().println("\nRun +++++ " + simpleDateFormat.format(System.currentTimeMillis()) + " +++++:");

        controller.getExpandedReactionsTextArea().clear();
        controller.getCafTextArea().clear();
        controller.getRafTextArea().clear();
        controller.getPseudoRAFTextArea().clear();

        controller.getMuCafTextArea().clear();
        controller.getuRAFTextArea().clear();

        window.getExportManager().clear();

        if (VerifyInput.verify(window)) {
            final ReactionSystem expandedReactionSystem = window.getInputReactionSystem().computeExpandedSystem();
            controller.getExpandedReactionsTextArea().setText("Expanded reactions:\n\n" + ModelIO.toString(expandedReactionSystem, true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));

            if (expandedReactionSystem.getFoods().size() < window.getInputReactionSystem().getFoods().size())
                window.getLogStream().println(String.format("Removed %d unused food items", (window.getInputReactionSystem().getFoods().size() - expandedReactionSystem.getFoods().size())));

            RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxCAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.maxCAF), controller.getCafTextArea(), runningListener);
            RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxRAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.maxRAF), controller.getRafTextArea(), runningListener);
            RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxPseudoRAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.maxPseudoRAF), controller.getPseudoRAFTextArea(), runningListener);
            RunAlgorithm.apply(window, window.getInputReactionSystem(), new MinIrrRAFHeuristic(), window.getReactionSystem(ReactionSystem.Type.minIrrRAF), controller.getIrrRAFTextArea(), runningListener);

            if (window.getInputReactionSystem().isInhibitorsPresent()) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MuCAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.muCAF), controller.getMuCafTextArea(), runningListener);
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new URAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.uRAF), controller.getuRAFTextArea(), runningListener);
            }
        }
    }
}
