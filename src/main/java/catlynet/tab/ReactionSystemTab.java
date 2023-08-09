/*
 * ReactionSystemTab.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.tab;

import catlynet.dialog.ExportReactionsFileDialog;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import javafx.scene.control.MenuItem;

/**
 * reaction system tab
 * Daniel Huson, 8.2023
 */
public class ReactionSystemTab extends TextTab {
	private final ReactionSystem reactionSystem;

	public ReactionSystemTab(MainWindow mainWindow, ReactionSystem reactionSystem) {
		super(mainWindow, reactionSystem.getName());
		this.reactionSystem = reactionSystem;

		var label = getName() + "...";
		{
			var menuItem = mainWindow.getController().getExportMenu().getItems().stream().filter(item -> label.equals(item.getText())).findAny();
			menuItem.ifPresentOrElse(item -> item.setOnAction(e -> exportToFile()), () -> {
				var newItem = new MenuItem(label);
				newItem.setOnAction(e -> exportToFile());
				mainWindow.getController().getExportMenu().getItems().add(newItem);
			});
		}

		setOnClosed(e -> {
			var menuItem = mainWindow.getController().getExportMenu().getItems().stream().filter(item -> label.equals(item.getText())).findAny();
			menuItem.ifPresent(item -> mainWindow.getController().getExportMenu().getItems().remove(item));
		});
	}

	public ReactionSystem getReactionSystem() {
		return reactionSystem;
	}

	public void exportToFile() {
		ExportReactionsFileDialog.apply(getMainWindow(), reactionSystem);
	}
}
