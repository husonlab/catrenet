/*
 * TargetsDialogController.java Copyright (C) 2024 Daniel H. Huson
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

package catlynet.dialog.targets;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.IntegerStringConverter;
import jloda.fx.icons.MaterialIcons;

public class TargetsDialogController {

	@FXML
	private Button addButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button applyButton;

	@FXML
	private Button cancelButton;

	@FXML
	private ListView<String> listView;

	@FXML
	private ComboBox<String> selectComboBox;

	@FXML
	private TextField ordersTextField;

	@FXML
	private void initialize() {
		MaterialIcons.setIcon(addButton, "add");
		MaterialIcons.setIcon(deleteButton, "delete");

		ordersTextField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
	}

	public Button getAddButton() {
		return addButton;
	}

	public Button getDeleteButton() {
		return deleteButton;
	}

	public Button getApplyButton() {
		return applyButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public ListView<String> getListView() {
		return listView;
	}

	public ComboBox<String> getSelectComboBox() {
		return selectComboBox;
	}

	public TextField getOrdersTextField() {
		return ordersTextField;
	}
}
