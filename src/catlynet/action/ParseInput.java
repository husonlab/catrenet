/*
 * ParseInput.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.io.ModelIO;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.scene.control.ComboBox;
import jloda.fx.util.BasicFX;
import jloda.fx.util.NotificationManager;
import jloda.util.IOExceptionWithLineNumber;

import java.io.IOException;
import java.io.StringReader;

/**
 * verifies the current input
 * Daniel Huson, 7.2019
 */
public class ParseInput {
    /**
     * parses the current input food set and reactions
     *
     * @param window
     * @return true, if successful
     */
    public static boolean apply(MainWindow window) {
        final MainWindowController controller = window.getController();

        window.getModel().clear();
        try {
            ModelIO.read(window.getModel(), new StringReader(controller.getInputTextArea().getText()), window.getDocument().getReactionNotation());
            final ComboBox<String> foodCBox = controller.getFoodSetComboBox();
            if (foodCBox.getSelectionModel().getSelectedItem() != null)
                ModelIO.read(window.getModel(), new StringReader("Food: " + foodCBox.getSelectionModel().getSelectedItem()), window.getDocument().getReactionNotation());
            final String foodString = ModelIO.getFoodString(window.getModel(), true, window.getDocument().getReactionNotation());
            foodCBox.getSelectionModel().select(foodString);
            if (!foodCBox.getItems().contains(foodString))
                foodCBox.getItems().add(0, foodString);

            controller.getInputTextArea().setText(ModelIO.toString(window.getModel(), false, true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
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
