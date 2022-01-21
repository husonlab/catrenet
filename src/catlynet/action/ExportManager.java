/*
 * ExportManager.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.action;

import catlynet.io.CRSFileFilter;
import catlynet.io.FileOpener;
import catlynet.io.Save;
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.view.ReactionGraphView;
import catlynet.window.MainWindow;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.NotificationManager;
import jloda.util.CollectionUtils;
import jloda.util.FileUtils;
import jloda.util.ProgramProperties;
import jloda.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
        window.getController().getExportMenu().getItems().stream()
                .filter(m -> m != window.getController().getExportSelectedNodesMenuItem() && m != window.getController().getExportListOfReactionsMenuItem() && !(m instanceof SeparatorMenuItem))
                .forEach(m -> m.setDisable(true));
    }

    /**
     * add or replace an export menu item
     *
     * @param reactions
     */
    public void addOrReplace(ReactionSystem reactions) {
        final String reactionName = reactions.getName();
        final Menu exportMenu = window.getController().getExportMenu();
        final MenuItem exportMenuItem;

        final Optional<MenuItem> existing = exportMenu.getItems().stream().filter(m -> !(m instanceof SeparatorMenuItem) && m.getText().equals(reactionName + "...")).findAny();
        if (reactions.size() > 0) {
            if (existing.isPresent())
                exportMenuItem = existing.get();
            else {
                exportMenuItem = new MenuItem(reactionName + "...");
                exportMenu.getItems().add(exportMenuItem);
                //exportMenu.getItems().setAll(exportMenu.getItems().stream().sorted(Comparator.comparing(MenuItem::getText)).collect(Collectors.toList()));
            }
			exportMenuItem.setOnAction(c -> exportDialog(window, StringUtils.toCamelCase(reactionName), reactions));
			exportMenuItem.setDisable(reactions.size() == 0);
        } else {
            existing.ifPresent(menuItem -> exportMenu.getItems().remove(menuItem));
        }
    }

    /**
     * export a system of reactions
     *
     * @param window
     * @param reactionName
     * @param reactions
     */
    public static void exportDialog(MainWindow window, String reactionName, ReactionSystem reactions) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File - " + ProgramProperties.getProgramVersion());

		File currentFile = new File(FileUtils.replaceFileSuffix(window.getDocument().getFileName(), "-" + reactionName + ".crs"));

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

                if (selectedFile.exists()) {
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Open saved file - " + ProgramProperties.getProgramName());
                    alert.setHeaderText("Successfully exported the '" + reactionName + "' reaction system to file '" + selectedFile.getName() + "'.");
                    alert.setContentText("Do you want to open the file in a new window?");

                    final Optional<ButtonType> answer = alert.showAndWait();
                    if (answer.isPresent() && answer.get() == ButtonType.OK) {
                        (new FileOpener()).accept(selectedFile.getPath());
                    }
                    alert.close();
                }
            } catch (IOException ex) {
                NotificationManager.showError("Export failed: " + ex);
            }
        }
    }

    public static void exportNodes(MainWindow window) {
        final ReactionGraphView graphView = window.getReactionGraphView();

        final Set<MoleculeType> food = new HashSet<>();

        final ReactionSystem output = new ReactionSystem();
        graphView.getNodeSelection().getSelectedItems().forEach(v -> {
            if (v.getInfo() instanceof Reaction) {
                final Reaction r = (Reaction) v.getInfo();
                food.addAll(r.getReactants());
				food.addAll(r.getProducts());
				r.getCatalystConjunctions().forEach(c -> food.addAll(MoleculeType.valuesOf(StringUtils.split(c.getName(), '&'))));
				food.addAll(r.getInhibitions());
                output.getReactions().add(r);
            } else if (v.getInfo() instanceof MoleculeType) {
                food.add((MoleculeType) v.getInfo());
            }
        });

        output.getFoods().addAll(CollectionUtils.intersection(window.getInputReactionSystem().getFoods(), food));

        if (output.getReactions().size() > 0) {
            ExportManager.exportDialog(window, "selected", output);
        }
    }
}
