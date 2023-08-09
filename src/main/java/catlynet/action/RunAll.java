/*
 * RunAll.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.algorithm.*;
import catlynet.model.ReactionSystem;
import catlynet.tab.TabManager;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.value.ChangeListener;

import java.text.SimpleDateFormat;

/**
 * run all algorithms
 * Daniel Huson, 2.2020
 */
public class RunAll {
    public static void apply(MainWindow window, ChangeListener<Boolean> runningListener) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        window.getLogStream().println("\nRun +++++ " + simpleDateFormat.format(System.currentTimeMillis()) + " +++++:");

        final MainWindowController controller = window.getController();
        final TabManager tabManager = window.getTabManager();

		controller.getParsedReactionsTextArea().clear();
        tabManager.clearAll();

        if (VerifyInput.verify(window)) {
            final ReactionSystem inputReactions = window.getInputReactionSystem();

            RunAlgorithm.apply(window, inputReactions, new MaxCAFAlgorithm(), runningListener, true);
            RunAlgorithm.apply(window, inputReactions, new MaxRAFAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new StrictlyAutocatalyticMaxRAFAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new MaxPseudoRAFAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new TrivialCAFsAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new TrivialRAFsAlgorithm(), runningListener, false);

            if (inputReactions.isInhibitorsPresent()) {
                RunAlgorithm.apply(window, inputReactions, new MuCAFAlgorithm(), runningListener, false);
                RunAlgorithm.apply(window, inputReactions, new URAFAlgorithm(), runningListener, false);
            }

            window.getDocument().setReactionDependencyNetwork(null);
            if (window.getInputReactionSystem().getReactions().size() <= 100) {
                ComputeReactionDependencies.run(window);
            } else
                System.err.println("Too many reactions, not automatically computing reaction dependency network");
            window.getDocument().setMoleculeDependencyNetwork(null);
            ComputeMoleculeDependencies.run(window);
        }
    }
}
