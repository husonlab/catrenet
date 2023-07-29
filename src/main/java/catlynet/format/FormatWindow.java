/*
 * FormatWindow.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.format;

import catlynet.io.ModelIO;
import catlynet.main.Version;
import catlynet.window.MainWindow;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.MainWindowManager;
import jloda.util.FileUtils;

public class FormatWindow {
    public static final String title = "Node and Edge Format";

    private final Stage stage;

    /**
     * construct the format dialog for the given window
     *
	 */
    public FormatWindow(MainWindow mainWindow) {
        final ExtendedFXMLLoader<FormatWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        Parent root = extendedFXMLLoader.getRoot();
        FormatWindowController controller = extendedFXMLLoader.getController();

        final ToggleGroup formatToggleGroup = new ToggleGroup();
        formatToggleGroup.getToggles().addAll(controller.getFullFormatRadioButton(), controller.getSpareFormatRadioButton(), controller.getTabbedFormatRadioButton());

        formatToggleGroup.selectedToggleProperty().addListener((c, o, n) -> {
            if (n != null) {
                ProgramProperties.put("ReactionNotation", ((RadioButton) n).getText());
            }
        });

        for (Toggle toggle : formatToggleGroup.getToggles()) {
            if (ProgramProperties.get("ReactionNotation", controller.getSpareFormatRadioButton().getText()).equals(((RadioButton) toggle).getText())) {
                formatToggleGroup.selectToggle(toggle);
                break;
            }
        }

        final ToggleGroup arrowToggleGroup = new ToggleGroup();
        arrowToggleGroup.getToggles().addAll(controller.getArrowsUseEqualsRadioButton(), controller.getArrowsUseMinusRadioButton());

        arrowToggleGroup.selectedToggleProperty().addListener((c, o, n) -> {
            if (n != null) {
                ProgramProperties.put("ArrowNotation", ((RadioButton) n).getText());
            }
        });


        for (Toggle toggle : arrowToggleGroup.getToggles()) {
            if (ProgramProperties.get("ArrowNotation", controller.getArrowsUseEqualsRadioButton().getText()).equals(((RadioButton) toggle).getText())) {
                arrowToggleGroup.selectToggle(toggle);
                break;
            }
        }

        stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.sizeToScene();
        stage.setX(mainWindow.getStage().getX() + 150);
        stage.setY(mainWindow.getStage().getY() + 150);

		stage.setTitle(title + " - " + FileUtils.getFileNameWithoutPath(mainWindow.getDocument().getFileName()) + " - " + Version.NAME);
		stage.show();

        // ensures that window can't be resized too small:
        Platform.runLater(() -> {
            stage.setMinWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
        });

        MainWindowManager.getInstance().addAuxiliaryWindow(mainWindow, stage);

        controller.getCancelButton().setOnAction((e) -> {
            stage.hide();
            MainWindowManager.getInstance().removeAuxiliaryWindow(mainWindow, stage);

        });

        controller.getApplyButton().setOnAction((e) -> {
            final ReactionNotation reactionNotation = ReactionNotation.valueOfIgnoreCase(((RadioButton) formatToggleGroup.getSelectedToggle()).getText());
            final ArrowNotation arrowNotation = ArrowNotation.valueOfLabel(((RadioButton) arrowToggleGroup.getSelectedToggle()).getText());

            final boolean changed = (reactionNotation != null && reactionNotation != mainWindow.getDocument().getReactionNotation())
                    || (arrowNotation != null && arrowNotation != mainWindow.getDocument().getArrowNotation());
            if (changed) {
                if (reactionNotation != null)
                    mainWindow.getDocument().setReactionNotation(reactionNotation);
                if (arrowNotation != null)
                    mainWindow.getDocument().setArrowNotation(arrowNotation);
                // rewrite input tab:
                mainWindow.getController().getInputTextArea().setText(ModelIO.toString(mainWindow.getInputReactionSystem(), false,
                        mainWindow.getDocument().getReactionNotation(), mainWindow.getDocument().getArrowNotation()));
                final String foodString = ModelIO.getFoodString(mainWindow.getInputReactionSystem(), mainWindow.getDocument().getReactionNotation());

                mainWindow.getController().getInputFoodTextArea().setText(foodString);

                mainWindow.getTabManager().clearAll();
            }
            stage.hide();
            MainWindowManager.getInstance().removeAuxiliaryWindow(mainWindow, stage);
        });
    }

    public Stage getStage() {
        return stage;
    }
}
