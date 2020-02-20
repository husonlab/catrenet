/*
 * ExportManager.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.action;

import catlynet.io.CRSFileFilter;
import catlynet.io.Save;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The export menu manager
 * Daniel Huson, 2.2020
 */
public class ExportManager {
    private final MainWindow window;

    /**
     * constructor
     */
    public ExportManager(MainWindow window) {
        this.window = window;
    }


    public void clear() {
        window.getController().getExportMenu().getItems().forEach(m -> m.setDisable(true));
    }

    /**
     * add or replace an export menu item
     *
     * @param reactions
     */
    public void addOrReplace(ReactionSystem reactions) {
        final String reactionName = Basic.toCamelCase(reactions.getName());
        final Menu exportMenu = window.getController().getExportMenu();
        final MenuItem exportMenuItem;

        final Optional<MenuItem> existing = exportMenu.getItems().stream().filter(m -> m.getText().equals(reactionName)).findAny();
        if (existing.isPresent())
            exportMenuItem = existing.get();
        else {
            exportMenuItem = new MenuItem(reactionName);
            exportMenu.getItems().add(exportMenuItem);
            exportMenu.getItems().setAll(exportMenu.getItems().stream().sorted(Comparator.comparing(MenuItem::getText)).collect(Collectors.toList()));
        }
        exportMenuItem.setOnAction(c -> exportDialog(window, reactionName, reactions));
        exportMenuItem.setDisable(reactions.size() == 0);
    }

    /**
     * export a system of reactions
     *
     * @param window
     * @param reactionName
     * @param reactions
     */
    public void exportDialog(MainWindow window, String reactionName, ReactionSystem reactions) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File - " + ProgramProperties.getProgramVersion());

        File currentFile = new File(Basic.replaceFileSuffix(window.getDocument().getFileName(), "-" + reactionName + ".crs"));

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
                Save.apply(selectedFile, window, reactions);
                ProgramProperties.put("ExportFileDir", selectedFile.getParent());
                NotificationManager.showInformation("Exported to file: " + selectedFile);
                RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());
            } catch (IOException ex) {
                NotificationManager.showError("Export failed: " + ex);
            }
        }
    }
}
