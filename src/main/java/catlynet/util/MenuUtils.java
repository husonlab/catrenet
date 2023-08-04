/*
 * MenuUtils.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.util;

import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

/**
 * some utils for copying menus
 * Daniel Huson, 8.2023
 */
public class MenuUtils {
	/**
	 * copy all menu items from a menu and return a menu button with the new items, setting up listeners for the appropriate properties
	 *
	 * @param name       name to use for menu button
	 * @param sourceMenu the menu to be copied
	 * @return menu button with copied items
	 */
	public static MenuButton createMenuButton(String name, Menu sourceMenu) {
		var menuButton = new MenuButton(name);
		menuButton.getItems().addAll(copy(sourceMenu.getItems()));
		return menuButton;
	}

	/**
	 * copy all menu items from a menu and return a menu button with the new items, setting up listeners for the appropriate properties
	 *
	 * @param name  name to use for menu button
	 * @param items the items to be copied
	 * @return menu button with copied items
	 */
	public static MenuButton createMenuButton(String name, MenuItem... items) {
		var menuButton = new MenuButton(name);
		menuButton.getItems().addAll(copy(List.of(items)));
		return menuButton;
	}

	/**
	 * copies a list of menu items, setting up listeners for the appropriate properties
	 *
	 * @param items items to copy
	 * @return copies of items
	 */
	public static List<MenuItem> copy(List<MenuItem> items) {
		var result = new ArrayList<MenuItem>();
		for (var sourceItem : items) {
			MenuItem targetItem;
			if (sourceItem instanceof SeparatorMenuItem) {
				targetItem = new SeparatorMenuItem();
			} else if (sourceItem instanceof CheckMenuItem sourceCheckMenuItem) {
				targetItem = new CheckMenuItem(sourceCheckMenuItem.getText());
				((CheckMenuItem) targetItem).selectedProperty().bindBidirectional(sourceCheckMenuItem.selectedProperty());
			} else if (sourceItem instanceof RadioMenuItem sourceRadioMenuItem) {
				targetItem = new RadioMenuItem(sourceRadioMenuItem.getText());
				((RadioMenuItem) targetItem).selectedProperty().bindBidirectional(sourceRadioMenuItem.selectedProperty());
			} else if (sourceItem instanceof Menu sourceMenu) {
				var subMenu = new Menu(sourceMenu.getText());
				subMenu.getItems().addAll(copy(sourceMenu.getItems()));
				targetItem = subMenu;
			} else {
				targetItem = new MenuItem(sourceItem.getText());
			}
			targetItem.setOnAction(e -> {
				var action = sourceItem.getOnAction();
				if (action != null)
					action.handle(e);
			});
			targetItem.disableProperty().bindBidirectional(sourceItem.disableProperty());
			result.add(targetItem);

		}
		return result;
	}
}
