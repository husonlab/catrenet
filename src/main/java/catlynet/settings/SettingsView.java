/*
 * SettingsView.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.settings;

import catlynet.window.MainWindow;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Objects;

public class SettingsView {
	private final Parent root;
	private final SettingsController controller;
	private final SettingsPresenter presenter;

	public SettingsView(MainWindow mainWindow) {
		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(SettingsView.class.getResource("SettingsView.fxml")).openStream()) {
			fxmlLoader.load(ins);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		root = fxmlLoader.getRoot();
		controller = fxmlLoader.getController();
		presenter = new SettingsPresenter(mainWindow, controller);
	}

	public SettingsController getController() {
		return controller;
	}

	public SettingsPresenter getPresenter() {
		return presenter;
	}

	public Parent getRoot() {
		return root;
	}
}
