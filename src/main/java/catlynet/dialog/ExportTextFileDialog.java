/*
 * ExportTextFileDialog.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.dialog;

import catlynet.io.CRSFileFilter;
import catlynet.window.MainWindow;
import javafx.stage.FileChooser;
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.NotificationManager;
import jloda.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * show the export text file dialog
 * Daniel Huson, 8.2023
 */
public class ExportTextFileDialog {
	public static void apply(MainWindow window, String name, String text) {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export File - " + ProgramProperties.getProgramVersion());

		File currentFile = new File(FileUtils.replaceFileSuffix(window.getDocument().getFileName(), "-" + name + ".txt"));

		fileChooser.getExtensionFilters().addAll(CRSFileFilter.getInstance(), TextFileFilter.getInstance());

		if (!currentFile.isDirectory()) {
			fileChooser.setInitialDirectory(currentFile.getParentFile());
			fileChooser.setInitialFileName(currentFile.getName());
		} else {
			final File tmp = new File(ProgramProperties.get("ExportFileDir", ProgramProperties.get("SaveFileDir", "")));
			if (tmp.isDirectory()) {
				fileChooser.setInitialDirectory(tmp);
			}
		}

		final File selectedFile = fileChooser.showSaveDialog(window.getStage());

		if (selectedFile != null) {
			try {
				try (Writer w = FileUtils.getOutputWriterPossiblyZIPorGZIP(selectedFile.getPath())) {
					w.write(text);
				}
				ProgramProperties.put("ExportFileDir", selectedFile.getParent());
				NotificationManager.showInformation("Exported to file: " + selectedFile);
			} catch (IOException ex) {
				NotificationManager.showError("Export failed: " + ex);
			}
		}
	}
}
