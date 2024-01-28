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
import jloda.fx.util.ProgramProperties;
import jloda.util.IteratorUtils;
import jloda.util.NumberUtils;
import jloda.util.SetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TargetsDialogPresenter {
	private static final List<String> previousListMembers = new ArrayList<>(List.of(ProgramProperties.get("TargetPreviousListMembers", new String[0])));

	private final Collection<String> results = new ArrayList<>();
	private int randomOrders = ProgramProperties.get("TargetRandomOrders", 100);


	public TargetsDialogPresenter(Stage stage, Collection<String> possibleTargets, TargetsDialogController controller) {
		controller.getSelectComboBox().getItems().addAll(possibleTargets.stream().sorted().toList());
		Platform.runLater(() -> AutoCompleteComboBox.install(controller.getSelectComboBox()));

		controller.getListView().getItems().addAll(IteratorUtils.asList(SetUtils.intersection(previousListMembers, possibleTargets)).stream().sorted().toList());

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

		controller.getOrdersTextField().setText(String.valueOf(randomOrders));
		controller.getOrdersTextField().textProperty().addListener((v, o, n) -> {
			if (NumberUtils.isInteger(n) && NumberUtils.parseInt(n) > 0)
				randomOrders = NumberUtils.parseInt(n);
		});

		controller.getDeleteButton().setOnAction(e -> {
			var toRemove = new ArrayList<>(controller.getListView().getSelectionModel().getSelectedItems());
			Platform.runLater(() -> controller.getListView().getItems().removeAll(toRemove));
		});
		controller.getDeleteButton().disableProperty().bind(Bindings.isEmpty(controller.getListView().getSelectionModel().getSelectedItems()));

		controller.getApplyButton().setOnAction(e -> {
			results.addAll(controller.getListView().getItems());
			previousListMembers.clear();
			previousListMembers.addAll(controller.getListView().getItems());
			ProgramProperties.put("TargetRandomOrders", getRandomOrders());
			ProgramProperties.put("TargetPreviousListMembers", previousListMembers.toArray(new String[0]));
			stage.hide();
		});
		controller.getApplyButton().disableProperty().bind(Bindings.isEmpty(controller.getListView().getItems()));

		controller.getCancelButton().setOnAction(e -> {
			results.clear();
			stage.hide();
		});
	}

	public Collection<String> getResults() {
		return results;
	}

	public int getRandomOrders() {
		return randomOrders;
	}
}
