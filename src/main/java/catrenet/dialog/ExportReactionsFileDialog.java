/*
 *  ExportReactionsFileDialog.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.dialog;

import catrenet.io.CRSFileFilter;
import catrenet.io.FileOpener;
import catrenet.io.Save;
import catrenet.model.ReactionSystem;
import catrenet.window.MainWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.NotificationManager;
import jloda.util.FileUtils;
import jloda.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * the export reactions file dialog
 * Daniel Huson, 8.2023
 */
public class ExportReactionsFileDialog {
	/**
	 * the export reactions file dialog
	 *
	 * @param window
	 * @param reactions
	 */
	public static void apply(MainWindow window, ReactionSystem reactions) {
		var name = StringUtils.toCamelCase(reactions.getName()).replaceAll("'", "_");
		var fileChooser = new FileChooser();
		fileChooser.setTitle("Export File - " + ProgramProperties.getProgramVersion());

		var file = new File(FileUtils.replaceFileSuffix(window.getDocument().getFileName(), "-" + name + ".crs"));

		fileChooser.getExtensionFilters().addAll(CRSFileFilter.getInstance(), TextFileFilter.getInstance());

		if (!file.isDirectory()) {
			fileChooser.setInitialDirectory(file.getParentFile());
			fileChooser.setInitialFileName(file.getName());
		} else {
			var tmp = new File(ProgramProperties.get("ExportFileDir", ProgramProperties.get("SaveFileDir", "")));
			if (tmp.isDirectory()) {
				fileChooser.setInitialDirectory(tmp);
			}
		}

		var selectedFile = fileChooser.showSaveDialog(window.getStage());

		if (selectedFile != null) {
			try {
				Save.apply(selectedFile, window, reactions);
				ProgramProperties.put("ExportFileDir", selectedFile.getParent());
				NotificationManager.showInformation("Exported to file: " + selectedFile);
				RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());

				if (selectedFile.exists()) {
					if (true) {
						(new FileOpener()).accept(selectedFile.getPath());
					} else {
						var alert = new Alert(Alert.AlertType.CONFIRMATION);
						alert.setTitle("Open saved file - " + ProgramProperties.getProgramName());
						alert.setHeaderText("Successfully exported the '" + name + "' reaction system to file '" + selectedFile.getName() + "'.");
						alert.setContentText("Do you want to open the file in a new window?");

						var answer = alert.showAndWait();
						if (answer.isPresent() && answer.get() == ButtonType.OK) {
							(new FileOpener()).accept(selectedFile.getPath());
						}
						alert.close();
					}
				}
			} catch (IOException ex) {
				NotificationManager.showError("Export failed: " + ex);
			}
		}
	}
}
