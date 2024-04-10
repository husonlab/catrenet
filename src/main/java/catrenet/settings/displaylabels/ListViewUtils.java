/*
 *  ListViewUtils.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.settings.displaylabels;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import jloda.fx.icons.MaterialIcons;
import jloda.util.Pair;

import java.util.List;

/**
 * utils for list views
 * Daniel Huson, 4.2024
 */
public class ListViewUtils {
	/**
	 * setup interactive editing of keys and values
	 *
	 * @param listView          the list view
	 * @param enforceUniqueKeys enforce unique keys
	 */
	public static void setupEditing(ListView<Pair<String, String>> listView, boolean enforceUniqueKeys) {
		listView.setCellFactory(param -> createKeyValueListCell(listView.getItems(), enforceUniqueKeys));
	}

	public static ListCell<Pair<String, String>> createKeyValueListCell(List<Pair<String, String>> items, boolean enforceUniqueKeys) {
		return new ListCell<Pair<String, String>>() {
			private final TextField keyField;
			private final TextField valueField;
			private final Button deleteButton;
			private final HBox hbox;

			{
				keyField = new TextField();
				keyField.setTooltip(new Tooltip("Label used in text"));

				Runnable saveKey = () -> {
					if (keyField.getUserData() instanceof Pair pair) {
						var item = (Pair<String, String>) pair;
						if (enforceUniqueKeys || items.stream().filter(p -> p != item).noneMatch(p -> p.getFirst().equals(keyField.getText()))) {
							var index = items.indexOf(item);
							Platform.runLater(() -> items.set(index, new Pair<>(keyField.getText(), item.getSecond())));
						}
					}
				};
				keyField.focusedProperty().addListener((v, o, n) -> {
					if (!n) {
						saveKey.run();
					}
				});
				keyField.setOnAction(e -> saveKey.run());

				valueField = new TextField();
				valueField.setTooltip(new Tooltip("Label used in drawings"));

				Runnable saveValue = () -> {
					if (valueField.getUserData() instanceof Pair pair) {
						var item = (Pair<String, String>) pair;
						if (!valueField.getText().equals(pair.getSecond())) {
							var index = items.indexOf(item);
							Platform.runLater(() -> items.set(index, new Pair<>(item.getFirst(), valueField.getText())));
						}
					}
				};
				valueField.focusedProperty().addListener((v, o, n) -> {
					if (!n)
						saveValue.run();
				});
				valueField.setOnAction(e -> saveValue.run());

				deleteButton = new Button("Delete");
				deleteButton.setTooltip(new Tooltip("Delete this item"));
				MaterialIcons.setIcon(deleteButton, MaterialIcons.clear);
				deleteButton.setOnAction(e -> {
					if (deleteButton.getUserData() instanceof Pair pair) {
						items.remove(pair);
					}
				});

				hbox = new HBox(keyField, valueField, deleteButton);
				hbox.setSpacing(5);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}

			@Override
			protected void updateItem(Pair<String, String> item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					keyField.setText(item.getFirst());
					keyField.setUserData(item);
					valueField.setText(item.getSecond());
					valueField.setUserData(item);
					deleteButton.setUserData(item);
					setGraphic(hbox);
				}
			}
		};
	}
}

