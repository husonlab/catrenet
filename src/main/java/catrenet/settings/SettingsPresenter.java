/*
 *  SettingsPresenter.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.settings;

import catrenet.io.ModelIO;
import catrenet.view.EdgeView;
import catrenet.view.NodeView;
import catrenet.window.MainWindow;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ClipboardUtils;
import jloda.fx.util.ProgramProperties;
import jloda.fx.util.RunAfterAWhile;
import jloda.util.NumberUtils;
import jloda.util.Pair;
import jloda.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SettingsPresenter {
	public SettingsPresenter(MainWindow mainWindow, SettingsController controller) {
		{
			var nodeView = NodeView.createNullNodeView();

			controller.getReactionNodeStyleCBox().getItems().addAll(NodeView.NodeStyle.values());
			ProgramProperties.track("reactionNodeStyle", controller.getReactionNodeStyleCBox().valueProperty(), NodeView.NodeStyle::valueOf, nodeView.getReactionNodeShape());
			controller.getFoodNodeStyleCBox().getItems().addAll(NodeView.NodeStyle.values());
			ProgramProperties.track("foodNodeStyle", controller.getFoodNodeStyleCBox().valueProperty(), NodeView.NodeStyle::valueOf, nodeView.getFoodNodeShape());
			controller.getMoleculeNodeStyleCBox().getItems().addAll(NodeView.NodeStyle.values());
			ProgramProperties.track("moleculeNodeStyle", controller.getMoleculeNodeStyleCBox().valueProperty(), NodeView.NodeStyle::valueOf, nodeView.getMoleculeNodeShape());

			ProgramProperties.track("reactionNodeColor", controller.getReactionNodesColorCBox().valueProperty(), nodeView.getReactionNodeFillColor());
			ProgramProperties.track("foodNodeColor", controller.getFoodNodesColorCBox().valueProperty(), nodeView.getFoodNodeFillColor());
			ProgramProperties.track("moleculeNodeColor", controller.getMoleculeNodesColorCBox().valueProperty(), nodeView.getMoleculeNodeFillColor());

			var values = Arrays.asList(1, 2, 4, 6, 8, 10, 12, 16, 20);

			controller.getReactionNodesSizeCBox().getItems().addAll(values);
			ProgramProperties.track("reactionNodeSize", controller.getReactionNodesSizeCBox().valueProperty(), Integer::parseInt, nodeView.getReactionNodeSize());

			controller.getFoodNodesSizeCBox().getItems().addAll(values);
			ProgramProperties.track("foodNodeSize", controller.getFoodNodesSizeCBox().valueProperty(), Integer::parseInt, nodeView.getFoodNodeSize());

			controller.getMoleculeNodesSizeCBox().getItems().addAll(values);
			ProgramProperties.track("moleculeNodeSize", controller.getMoleculeNodesSizeCBox().valueProperty(), Integer::parseInt, nodeView.getMoleculeNodeSize());
		}

		{
			var edgeView = EdgeView.createNullEdgeView();

			controller.getReactionEdgeStyleCBox().getItems().addAll(EdgeView.EdgeStyle.values());
			ProgramProperties.track("reactionEdgeStyle", controller.getReactionEdgeStyleCBox().valueProperty(), EdgeView.EdgeStyle::valueOf, edgeView.getReactionEdgeStyle());

			controller.getCatalystEdgeStyleCBox().getItems().addAll(EdgeView.EdgeStyle.values());
			ProgramProperties.track("catalystEdgeStyle", controller.getCatalystEdgeStyleCBox().valueProperty(), EdgeView.EdgeStyle::valueOf, edgeView.getCatalystEdgeStyle());

			controller.getInhibitionEdgeStyleCBox().getItems().addAll(EdgeView.EdgeStyle.values());
			ProgramProperties.track("inhibitorEdgeStyle", controller.getInhibitionEdgeStyleCBox().valueProperty(), EdgeView.EdgeStyle::valueOf, edgeView.getInhibitionEdgeStyle());

			controller.getReactionEdgesColorCBox().setValue(edgeView.getReactionColor());

			ProgramProperties.track("reactionEdgeColor", controller.getReactionEdgesColorCBox().valueProperty(), edgeView.getReactionColor());
			ProgramProperties.track("catalystEdgeColor", controller.getCatlystEdgesColorCBox().valueProperty(), edgeView.getCatalystColor());
			ProgramProperties.track("inhibitorEdgeColor", controller.getInhibitorEdgesColorCBox().valueProperty(), edgeView.getInhibitionColor());

			var values = Arrays.asList(1, 2, 4, 6, 8, 10, 12, 16, 20);

			controller.getReactionEdgesLineWidthCBox().getItems().addAll(values);
			ProgramProperties.track("reactionEdgeWidth", controller.getReactionEdgesLineWidthCBox().valueProperty(), Integer::parseInt, edgeView.getReactionEdgeWidth());

			controller.getCatalystEdgesLineWidthCBox().getItems().addAll(values);
			ProgramProperties.track("catalystEdgeWidth", controller.getCatalystEdgesLineWidthCBox().valueProperty(), Integer::parseInt, edgeView.getCatalystEdgeWidth());

			controller.getInhibitionEdgesLineWidthCBox().getItems().addAll(values);
			ProgramProperties.track("inhibitorEdgeWidth", controller.getInhibitionEdgesLineWidthCBox().valueProperty(), Integer::parseInt, edgeView.getInhibitionEdgeWidth());
		}

		{
			var formatToggleGroup = new ToggleGroup();
			formatToggleGroup.getToggles().addAll(controller.getFullNotationRadioButton(), controller.getSparseNotationRadioButton(), controller.getTabbedFormatRadioButton());

			var arrowToggleGroup = new ToggleGroup();
			arrowToggleGroup.getToggles().addAll(controller.getDoubleArrowRadioButton(), controller.getSingleArrowRadioButton());

			formatToggleGroup.selectedToggleProperty().addListener((c, o, n) -> {
				if (n != null) {
					ProgramProperties.put("ReactionNotation", ((RadioButton) n).getText());
					apply(mainWindow, formatToggleGroup, arrowToggleGroup);
				}
			});

			for (var toggle : formatToggleGroup.getToggles()) {
				if (ProgramProperties.get("ReactionNotation", controller.getSparseNotationRadioButton().getText()).equals(((RadioButton) toggle).getText())) {
					formatToggleGroup.selectToggle(toggle);
					break;
				}
			}

			arrowToggleGroup.selectedToggleProperty().addListener((c, o, n) -> {
				if (n != null) {
					ProgramProperties.put("ArrowNotation", ((RadioButton) n).getText());
					apply(mainWindow, formatToggleGroup, arrowToggleGroup);
				}
			});

			for (var toggle : arrowToggleGroup.getToggles()) {
				if (ProgramProperties.get("ArrowNotation", controller.getDoubleArrowRadioButton().getText()).equals(((RadioButton) toggle).getText())) {
					arrowToggleGroup.selectToggle(toggle);
					break;
				}
			}
		}

		controller.getWrapTextCheckBox().selectedProperty().addListener((v, o, n) -> {
			for (var textArea : BasicFX.getAllRecursively(mainWindow.getStage().getScene().getRoot(), TextArea.class)) {
				textArea.setWrapText(n);
			}
		});

		controller.getIterationsTextField().setText(String.valueOf(mainWindow.getReactionGraphView().getEmbeddingIterations()));
		controller.getIterationsTextField().textProperty().addListener((v, o, n) -> {
			if (NumberUtils.isInteger(n)) {
				var value = NumberUtils.parseInt(n);
				if (value > 0)
					mainWindow.getReactionGraphView().setEmbeddingIterations(value);
			}
		});

		controller.getMaxSizeNetworkTextField().setText(String.valueOf(mainWindow.getMaxGraphDisplaySize()));
		controller.getMaxSizeNetworkTextField().textProperty().addListener((v, o, n) -> {
			if (NumberUtils.isInteger(n)) {
				var value = NumberUtils.parseInt(n);
				if (value > 0)
					mainWindow.maxGraphDisplaySizeProperty().set(NumberUtils.parseInt(n));
			}
		});

		controller.getUseColorsInAnimationRadioButton().selectedProperty().bindBidirectional(mainWindow.getController().getUseColorsMenuItem().selectedProperty());
		controller.getMoveLabelsInAnimationCheckBox().selectedProperty().bindBidirectional(mainWindow.getController().getMoveLabelsMenuItem().selectedProperty());

		{
			controller.getActiveDisplayLabelsCheckBox().selectedProperty().bindBidirectional(mainWindow.getDocument().useDisplayLabelsProperty());
			controller.getDisplayLabelListView().getItems().addListener((ListChangeListener<? super Pair<String, String>>) e -> {
				while (e.next()) {
					for (var item : e.getAddedSubList()) {
						mainWindow.getDocument().getDisplayLabelsMap().put(item.getFirst(), item.getSecond());
					}
					for (var item : e.getRemoved()) {
						mainWindow.getDocument().getDisplayLabelsMap().remove(item.getFirst());
					}
				}
			});

			controller.getDisplayLabelListView().getItems().addAll(ProgramProperties.get("DisplayLabels", new ArrayList<>()));
			controller.getDisplayLabelListView().getItems().addListener((InvalidationListener) e ->
					RunAfterAWhile.applyInFXThread(controller.getDisplayLabelListView(), () -> ProgramProperties.put("DisplayLabels", controller.getDisplayLabelListView().getItems())));


			controller.getClearDisplayLabelsButton().setOnAction(e -> controller.getDisplayLabelListView().getItems().clear());
			controller.getClearDisplayLabelsButton().disableProperty().bind(Bindings.isEmpty(controller.getDisplayLabelListView().getItems()));

			controller.getImportDisplayLabelsButton().setOnAction(e -> {
				var string = ClipboardUtils.getTextFilesContentOrString();
				if (string != null && !string.isBlank()) {
					var oldSize = controller.getDisplayLabelListView().getItems().size();
					var newKeys = new HashSet<String>();
					for (var line : StringUtils.toList(string)) {
						var tokens = StringUtils.split(line, '\t');
						if (tokens.length == 2) {
							if (!newKeys.contains(tokens[0])) {
								controller.getDisplayLabelListView().getItems().add(new Pair<>(tokens[0], tokens[1]));
								newKeys.add(tokens[0]);
							}
						}
					}
					var toDelete = new ArrayList<Pair<String, String>>();
					for (var i = 0; i < oldSize; i++) {
						var item = controller.getDisplayLabelListView().getItems().get(i);
						if (newKeys.contains(item.getFirst()))
							toDelete.add(item);
					}
					controller.getDisplayLabelListView().getItems().removeAll(toDelete);
				}
			});

			controller.getExportDisplayLabelsButton().setOnAction(e -> {
				var buf = new StringBuilder();
				for (var item : controller.getDisplayLabelListView().getItems()) {
					buf.append("%s\t%s\n".formatted(item.getFirst(), item.getSecond()));
				}
				ClipboardUtils.putString(buf.toString());

			});
			controller.getExportDisplayLabelsButton().disableProperty().bind(Bindings.isEmpty(controller.getDisplayLabelListView().getItems()));

			controller.getAddDisplayLabelButton().setOnAction(e -> {
				var item = new Pair<>("Key", "Value");
				controller.getDisplayLabelListView().getItems().add(item);
				controller.getDisplayLabelListView().scrollTo(controller.getDisplayLabelListView().getItems().indexOf(item));
				controller.getDisplayLabelListView().getSelectionModel().select(item);
			});

			controller.getImportDisplayLabelsButton().disableProperty().bind(ClipboardUtils.hasStringProperty().not().and(ClipboardUtils.hasFilesProperty().not()));

		}
	}

	private void apply(MainWindow mainWindow, ToggleGroup formatToggleGroup, ToggleGroup arrowToggleGroup) {
		final var reactionNotation = (formatToggleGroup.getSelectedToggle() != null ? ReactionNotation.valueOfIgnoreCase(((RadioButton) formatToggleGroup.getSelectedToggle()).getText()) : null);
		final var arrowNotation = (arrowToggleGroup.getSelectedToggle() != null ? ArrowNotation.valueOfLabel(((RadioButton) arrowToggleGroup.getSelectedToggle()).getText()) : null);

		final var changed = (reactionNotation != null && reactionNotation != mainWindow.getDocument().getReactionNotation())
							|| (arrowNotation != null && arrowNotation != mainWindow.getDocument().getArrowNotation());
		if (changed) {
			if (reactionNotation != null)
				mainWindow.getDocument().setReactionNotation(reactionNotation);
			if (arrowNotation != null)
				mainWindow.getDocument().setArrowNotation(arrowNotation);
			// rewrite input tab:
			mainWindow.getController().getInputTextArea().setText(ModelIO.toString(mainWindow.getInputReactionSystem(), false,
					mainWindow.getDocument().getReactionNotation(), mainWindow.getDocument().getArrowNotation()));
			var foodString = ModelIO.getFoodString(mainWindow.getInputReactionSystem(), mainWindow.getDocument().getReactionNotation());

			mainWindow.getController().getInputFoodTextArea().setText(foodString);
			mainWindow.getTabManager().clearAll();
		}
	}
}
