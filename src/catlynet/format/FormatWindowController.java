/*
 * FormatWindowController.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.format;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;

import java.net.URL;
import java.util.ResourceBundle;

public class FormatWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private RadioButton fullFormatRadioButton;

    @FXML
    private RadioButton spareFormatRadioButton;

    @FXML
    private RadioButton tabbedFormatRadioButton;

    @FXML
    private RadioButton arrowsUseEqualsRadioButton;

    @FXML
    private RadioButton arrowsUseMinusRadioButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    void initialize() {
        assert fullFormatRadioButton != null : "fx:id=\"fullFormatRadioButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";
        assert spareFormatRadioButton != null : "fx:id=\"spareFormatRadioButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";
        assert tabbedFormatRadioButton != null : "fx:id=\"tabbedFormatRadioButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";
        assert arrowsUseEqualsRadioButton != null : "fx:id=\"arrowsUseEqualsRadioButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";
        assert arrowsUseMinusRadioButton != null : "fx:id=\"arrowsUseMinusRadioButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";
        assert applyButton != null : "fx:id=\"applyButton\" was not injected: check your FXML file 'FormatWindow.fxml'.";

    }

    public RadioButton getFullFormatRadioButton() {
        return fullFormatRadioButton;
    }

    public RadioButton getSpareFormatRadioButton() {
        return spareFormatRadioButton;
    }

    public RadioButton getTabbedFormatRadioButton() {
        return tabbedFormatRadioButton;
    }

    public RadioButton getArrowsUseEqualsRadioButton() {
        return arrowsUseEqualsRadioButton;
    }

    public RadioButton getArrowsUseMinusRadioButton() {
        return arrowsUseMinusRadioButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }
}
