/*
 * ExportList.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.dialog.exportlist;

import catlynet.action.ExportManager;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * export a provided list of reactions
 * Daniel Huson, 4.2020
 */
public class ExportList {
    private final Stage stage;

    /**
     * constructor
     *
     * @param window
     */
    public ExportList(MainWindow window) {
        final ExtendedFXMLLoader<ExportListController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        final ExportListController controller = extendedFXMLLoader.getController();
        final Parent root = extendedFXMLLoader.getRoot();

        final Scene scene = new Scene(root);
        stage = new Stage();
        stage.setTitle("Export Selected Reactions - " + ProgramProperties.getProgramName());
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setX(window.getStage().getX() + 100);
        stage.setY(window.getStage().getY() + 100);
        stage.setMinWidth(((Pane) root).getPrefWidth());
        stage.setMinHeight(((Pane) root).getPrefHeight());

        final ObservableSet<String> reactionNames = FXCollections.observableSet(new TreeSet<>());

        final ReactionSystem inputReactions = window.getInputReactionSystem();

        controller.getReactionsTextArea().textProperty().addListener((InvalidationListener) c -> {
			final Set<String> set = new HashSet<>(StringUtils.getLinesFromString(controller.getReactionsTextArea().getText()));
			final int total = window.getInputReactionSystem().size();
            reactionNames.clear();
            reactionNames.addAll(Basic.intersection(set, inputReactions.getReactionNames()));
            final int found = reactionNames.size();
            final int unknown = set.size() - found;
            controller.getMessageLabel().setText(unknown == 0 ? String.format("%,d of %,d", found, total) : String.format("%,d of %,d (%d unknown)", found, total, unknown));
        });

        final ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(controller.getKeepRadioButton(), controller.getRemoveRadioButton());
        toggleGroup.selectToggle(controller.getKeepRadioButton());

        controller.getCancelButton().setOnAction(c -> stage.close());
        controller.getExportButton().setOnAction(c -> {
            final ReactionSystem result = new ReactionSystem("extracted");
            result.getReactions().setAll(inputReactions.getReactions().filtered(r -> reactionNames.contains(r.getName()) == controller.getKeepRadioButton().isSelected()));
            result.getFoods().setAll(result.computeMentionedFoods(inputReactions.getFoods()));
            ExportManager.exportDialog(window, result.getName(), result);
        });
        controller.getExportButton().disableProperty().bind(Bindings.isEmpty(reactionNames));
    }

    public Stage getStage() {
        return stage;
    }
}
