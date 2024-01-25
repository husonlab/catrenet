/*
 * TargetsDialog.java Copyright (C) 2024 Daniel H. Huson
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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class TargetsDialog {
	private final Stage stage;
	private final Collection<String> results = new ArrayList<>();

	public TargetsDialog(Stage parent, Collection<String> possibleTargets) {
		stage = new Stage();
		stage.setTitle("Select Target Molecules");
		stage.initOwner(parent);


		var fxmlLoader = new FXMLLoader();
		try (var ins = Objects.requireNonNull(TargetsDialogController.class.getResource("TargetsDialog.fxml")).openStream()) {
			fxmlLoader.load(ins);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		TargetsDialogController controller = fxmlLoader.getController();
		var presenter = new TargetsDialogPresenter(stage, possibleTargets, controller, results);

		stage.setScene(new Scene(fxmlLoader.getRoot()));
		stage.sizeToScene();
	}

	public Collection<String> show() {
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();
		return results;
	}

}
