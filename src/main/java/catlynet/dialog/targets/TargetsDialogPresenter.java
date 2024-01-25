/*
 * TargetsDialogPresenter.java Copyright (C) 2024 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import jloda.fx.util.AutoCompleteComboBox;

import java.util.ArrayList;
import java.util.Collection;

public class TargetsDialogPresenter {
	public TargetsDialogPresenter(Stage stage, Collection<String> possibleTargets, TargetsDialogController controller, Collection<String> selected) {
		controller.getSelectComboBox().getItems().addAll(possibleTargets.stream().sorted().toList());
		Platform.runLater(() -> AutoCompleteComboBox.install(controller.getSelectComboBox()));

		controller.getSelectComboBox().setDisable(possibleTargets.isEmpty());
		controller.getAddButton().disableProperty().bind(
				Bindings.createBooleanBinding(() -> !possibleTargets.contains(controller.getSelectComboBox().getValue())
													|| controller.getListView().getItems().contains(controller.getSelectComboBox().getValue()), controller.getSelectComboBox().valueProperty()));
		controller.getAddButton().setOnAction(e -> {
			var target = controller.getSelectComboBox().getValue();
			if (possibleTargets.contains(target) && !controller.getListView().getItems().contains(target)) {
				controller.getListView().getItems().add(target);
				Platform.runLater(() -> controller.getSelectComboBox().setValue(""));
			}
		});

		controller.getDeleteButton().setOnAction(e -> {
			var toRemove = new ArrayList<>(controller.getListView().getSelectionModel().getSelectedItems());
			Platform.runLater(() -> controller.getListView().getItems().removeAll(toRemove));
		});
		controller.getDeleteButton().disableProperty().bind(Bindings.isEmpty(controller.getListView().getSelectionModel().getSelectedItems()));

		controller.getApplyButton().setOnAction(e -> {
			selected.addAll(controller.getListView().getItems());
			stage.hide();
		});
		controller.getApplyButton().disableProperty().bind(Bindings.isEmpty(controller.getListView().getItems()));
		controller.getCancelButton().setOnAction(e -> {
			selected.clear();
			stage.hide();
		});
	}
}
