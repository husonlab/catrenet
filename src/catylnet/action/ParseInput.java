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

package catylnet.action;

import catylnet.window.MainWindow;
import catylnet.window.MainWindowController;
import javafx.scene.control.ComboBox;
import jloda.fx.util.BasicFX;
import jloda.fx.util.NotificationManager;
import jloda.util.Basic;
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
            window.getModel().read(new StringReader(controller.getCrsTextArea().getText()));
            final ComboBox<String> foodCBox = controller.getFoodSetComboBox();
            if (foodCBox.getSelectionModel().getSelectedItem() != null)
                window.getModel().read(new StringReader("Food: " + foodCBox.getSelectionModel().getSelectedItem()));
            final String foodString = Basic.toString(window.getModel().getFoods(), " ");
            foodCBox.getSelectionModel().select(foodString);
            if (!foodCBox.getItems().contains(foodString))
                foodCBox.getItems().add(0, foodString);

            controller.getCrsTextArea().setText(Basic.toString(window.getModel().getReactions(), "\n"));
            return true;
        } catch (IOException ex) {
            if (ex instanceof IOExceptionWithLineNumber) {
                NotificationManager.showError("Error in line: " + ((IOExceptionWithLineNumber) ex).getLineNumber() + ": " + ex.getMessage());
                BasicFX.gotoAndSelectLine(window.getController().getCrsTextArea(), ((IOExceptionWithLineNumber) ex).getLineNumber(), -1);
            } else
                NotificationManager.showError("Error: " + ex.getMessage());
        }
        return false;
    }
}
