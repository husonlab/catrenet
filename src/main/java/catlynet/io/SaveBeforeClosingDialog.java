/*
 * SaveBeforeClosingDialog.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.io;

import catlynet.window.Document;
import catlynet.window.MainWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;


public class SaveBeforeClosingDialog {
    public enum Result {save, close, cancel}

    /**
     * ask whether to save before closing
     *
     * @return true if doesn't need saving or saved, false else
     */
    public static Result apply(MainWindow mainWindow) {
        final Document document = mainWindow.getDocument();
        if (!document.isDirty()) {
            return Result.close;
        } else {
            mainWindow.getStage().toFront();
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(mainWindow.getStage());

            alert.setTitle("Save File Dialog");
            alert.setHeaderText("This document has unsaved changes");
            alert.setContentText("Save changes?");


            var buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            var buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            var buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

            final var result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == buttonTypeYes) {
                    return Save.showSaveDialog(mainWindow) ? Result.save : Result.close;
                } else return result.get() == buttonTypeNo ? Result.close : Result.cancel;
            }
            return Result.cancel; // canceled
        }
    }
}
