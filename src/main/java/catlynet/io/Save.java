/*
 * Save.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.io;

import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import javafx.stage.FileChooser;
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.NotificationManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * save reactions and food set
 * Daniel Huson, 6.2019
 */
public class Save {
    /**
     * save file
     *
	 */
    public static void apply(File file, MainWindow window, ReactionSystem reactionSystem) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            ModelIO.write(reactionSystem, w, true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation());
        }
    }

    /**
     * show save dialog
     *
     * @return true, if saved
     */
    public static boolean showSaveDialog(MainWindow window) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File - " + ProgramProperties.getProgramVersion());

        File currentFile = new File(window.getDocument().getFileName());

        fileChooser.getExtensionFilters().addAll(CRSFileFilter.getInstance(), TextFileFilter.getInstance());

        if (!currentFile.isDirectory()) {
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        } else {
            final File tmp = new File(ProgramProperties.get("SaveFileDir", ""));
            if (tmp.isDirectory()) {
                fileChooser.setInitialDirectory(tmp);
            }
        }

        final File selectedFile = fileChooser.showSaveDialog(window.getStage());

        if (selectedFile != null) {
            try {
                apply(selectedFile, window, window.getInputReactionSystem());
                window.getDocument().setFileName(selectedFile.getPath());
                ProgramProperties.put("SaveFileDir", selectedFile.getParent());
                NotificationManager.showInformation("Saved to file: " + selectedFile);
                RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());
				window.getDocument().setDirty(false);
                return true;
            } catch (IOException ex) {
                NotificationManager.showError("Save failed: " + ex);
            }
        }
        return false;

    }
}
