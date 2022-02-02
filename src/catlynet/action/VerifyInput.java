/*
 * VerifyInput.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.io.ModelIO;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import jloda.fx.util.BasicFX;
import jloda.fx.window.NotificationManager;
import jloda.util.IOExceptionWithLineNumber;

import java.io.IOException;
import java.io.StringReader;

/**
 * verifies the current input
 * Daniel Huson, 7.2019
 */
public class VerifyInput {
    /**
     * verfies the correctness the current input food set and reactions
     *
     * @return true, if successful
     */
    public static boolean verify(MainWindow window) {
        final MainWindowController controller = window.getController();

        try {
            final ReactionSystem reactionSystem = window.getDocument().getInputReactionSystem();
            reactionSystem.clear();

            ModelIO.read(reactionSystem, new StringReader(controller.getInputTextArea().getText()), window.getDocument().getReactionNotation());

            ModelIO.read(reactionSystem, new StringReader("Food: " + controller.getInputFoodTextArea().getText()), window.getDocument().getReactionNotation());

            // final String foodString = ModelIO.getFoodString(reactionSystem, window.getDocument().getReactionNotation());

            //controller.getInputTextArea().setText(ModelIO.toString(model, false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));

            reactionSystem.updateIsInhibitorsPresent();
            if (!window.getDocument().isWarnedAboutInhibitions() && reactionSystem.isInhibitorsPresent()) {
                final String message = "Input catalytic reaction system contains inhibitions. These are ignored in the computation of maxCAF, maxRAF and maxPseudoRAF";
                window.getLogStream().println(message);
                NotificationManager.showInformation(message);
                window.getDocument().setWarnedAboutInhibitions(true);
            }

            return true;
        } catch (IOException ex) {
            if (ex instanceof IOExceptionWithLineNumber) {
                NotificationManager.showError("Error in line: " + ((IOExceptionWithLineNumber) ex).getLineNumber() + ": " + ex.getMessage());
                BasicFX.gotoAndSelectLine(window.getController().getInputTextArea(), ((IOExceptionWithLineNumber) ex).getLineNumber(), -1);
            } else
                NotificationManager.showError("Error: " + ex.getMessage());
        }
        return false;
    }
}
