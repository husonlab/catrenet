/*
 * ExportListController.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.dialog.exportlist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;

public class ExportListController {

    @FXML
    private TextArea reactionsTextArea;

    @FXML
    private RadioButton keepRadioButton;

    @FXML
    private RadioButton removeRadioButton;

    @FXML
    private Label messageLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button exportButton;


    public TextArea getReactionsTextArea() {
        return reactionsTextArea;
    }

    public RadioButton getKeepRadioButton() {
        return keepRadioButton;
    }

    public RadioButton getRemoveRadioButton() {
        return removeRadioButton;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getExportButton() {
        return exportButton;
    }
}
