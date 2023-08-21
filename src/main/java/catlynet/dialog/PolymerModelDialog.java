/*
 * PolymerModelDialog.java Copyright (C) 2023 Daniel H. Huson
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

import catlynet.algorithm.PolymerModel;
import catlynet.io.ModelIO;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jloda.fx.util.AService;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;

/**
 * show the polymer model dialog and compute an instance
 * Daniel Huson, 8.2023
 */
public class PolymerModelDialog {
	/**
	 * show the polymer model dialog and compute an instance
	 *
	 * @param mainWindow the main window
	 */
	public static void show(MainWindow mainWindow) {
		var polymerModel = new PolymerModel();
		var parameters = showParametersDialog(mainWindow.getStage());

		if (parameters != null) {
			polymerModel.setInputParameters(parameters);
			AService.run(polymerModel::apply, PolymerModelDialog::open,
					e -> NotificationManager.showError("Failed: " + e.getMessage()),
					mainWindow.getStatusPane());
		}
	}

	public static void open(ReactionSystem reactionSystem) {
		var mainWindow = (MainWindow) MainWindowManager.getInstance().createAndShowWindow(true);
		var doc = mainWindow.getDocument();
		var inputReactions = mainWindow.getInputReactionSystem();
		inputReactions.clear();
		inputReactions.setName(reactionSystem.getName());
		inputReactions.getFoods().addAll(reactionSystem.getFoods());
		mainWindow.getController().getInputFoodTextArea().setText(ModelIO.getFoodString(inputReactions, doc.getReactionNotation()));
		mainWindow.getInputReactionSystem().getReactions().addAll(reactionSystem.getReactions());
		mainWindow.getController().getInputTextArea().setText("# " + reactionSystem.getHeaderLine() + ":\n\n" + ModelIO.toString(reactionSystem, false, doc.getReactionNotation(), doc.getArrowNotation()));
		mainWindow.getController().getLogTextArea().setText("# " + reactionSystem.getHeaderLine() + "\n");
		mainWindow.getDocument().setDirty(true);
		mainWindow.getDocument().setFileName(reactionSystem.getName() + ".crs");
	}

	private static PolymerModel.Parameters showParametersDialog(Stage stage) {
		var customDialog = new Dialog<Double[]>();
		customDialog.initOwner(stage);
		customDialog.setTitle("CatlyNet - Polymer model");
		customDialog.setHeaderText("Setup model parameters");

		ButtonType confirmButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		customDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		var alphabetSizeField = new TextField();
		alphabetSizeField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		ProgramProperties.track("alphabetSizeField", alphabetSizeField.textProperty(), "2");
		if (alphabetSizeField.getText().isBlank())
			alphabetSizeField.setText("2");
		var foodMoleculeMaxLengthField = new TextField();
		foodMoleculeMaxLengthField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		ProgramProperties.track("foodMoleculeMaxLengthField", foodMoleculeMaxLengthField.textProperty(), "2");
		if (foodMoleculeMaxLengthField.getText().isBlank())
			foodMoleculeMaxLengthField.setText("2");
		var polymerMaxLengthField = new TextField();
		polymerMaxLengthField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		ProgramProperties.track("polymerMaxLengthField", polymerMaxLengthField.textProperty(), "4");
		if (polymerMaxLengthField.getText().isBlank())
			polymerMaxLengthField.setText("4");

		var meanNumberOfCatalyzedField = new TextField();
		meanNumberOfCatalyzedField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
		ProgramProperties.track("meanNumberOfCatalyzedField", meanNumberOfCatalyzedField.textProperty(), "2.0");
		if (meanNumberOfCatalyzedField.getText().isBlank())
			meanNumberOfCatalyzedField.setText("2.0");

		var replicateNumberField = new TextField();
		replicateNumberField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
		ProgramProperties.track("replicateNumberField", replicateNumberField.textProperty(), "1");
		if (replicateNumberField.getText().isBlank())
			replicateNumberField.setText("666");

		gridPane.add(new Label("Alphabet Size:"), 0, 0);
		gridPane.add(alphabetSizeField, 1, 0);
		alphabetSizeField.setTooltip(new Tooltip("The size of the alphabet."));
		gridPane.add(new Label("Food Molecules Max Length:"), 0, 1);
		gridPane.add(foodMoleculeMaxLengthField, 1, 1);
		foodMoleculeMaxLengthField.setTooltip(new Tooltip("All molecules up to this length are placed in the food set"));
		gridPane.add(new Label("Polymers Max Length:"), 0, 2);
		gridPane.add(polymerMaxLengthField, 1, 2);
		polymerMaxLengthField.setTooltip(new Tooltip("All molecules up to this length are generated (but only those that are catalyzed are kept)"));
		gridPane.add(new Label("Mean Number of Catalyzed Reactions:"), 0, 3);
		gridPane.add(meanNumberOfCatalyzedField, 1, 3);
		meanNumberOfCatalyzedField.setTooltip(new Tooltip("The mean number of reactions that any given molecule will catalyse"));
		gridPane.add(new Label("Replicate number:"), 0, 4);
		gridPane.add(replicateNumberField, 1, 4);
		replicateNumberField.setTooltip(new Tooltip("Different numbers will generated different replicates (is used as random number seed)"));

		customDialog.getDialogPane().setContent(gridPane);

		customDialog.setResultConverter(dialogButton -> {
			if (dialogButton == confirmButtonType) {
				var alphabetSize = Double.parseDouble(alphabetSizeField.getText());
				var foodMoleculeMaxLength = Double.parseDouble(foodMoleculeMaxLengthField.getText());
				var polymerMaxLength = Double.parseDouble(polymerMaxLengthField.getText());
				var meanNumberOfCatalyzed = Double.parseDouble(meanNumberOfCatalyzedField.getText());
				var randomSeed = Double.parseDouble(replicateNumberField.getText());
				return new Double[]{alphabetSize, foodMoleculeMaxLength, polymerMaxLength, meanNumberOfCatalyzed, randomSeed};
			}
			return null;
		});

		var result = customDialog.showAndWait();
		if (result.isPresent()) {
			var array = result.get();
			return new PolymerModel.Parameters((int) Math.round(array[0]), (int) Math.round(array[1]), (int) Math.round(array[2]), array[3], (int) Math.round(array[4]));
		} else return null;
	}
}
