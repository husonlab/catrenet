/*
 *  SettingsController.java Copyright (C) 2024 Daniel H. Huson
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

import catrenet.settings.displaylabels.ListViewUtils;
import catrenet.view.EdgeView;
import catrenet.view.NodeView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import jloda.fx.icons.MaterialIcons;
import jloda.fx.util.ProgramProperties;
import jloda.util.Pair;

public class SettingsController {
	@FXML
	private TitledPane titledPane;

	@FXML
	private TextField iterationsTextField;

	@FXML
	private TextField maxSizeNetworkTextField;

	@FXML
	private ChoiceBox<EdgeView.EdgeStyle> reactionEdgeStyleCBox;

	@FXML
	private ChoiceBox<EdgeView.EdgeStyle> catalystEdgeStyleCBox;

	@FXML
	private ChoiceBox<EdgeView.EdgeStyle> inhibitionEdgeStyleCBox;

	@FXML
	private ChoiceBox<NodeView.NodeStyle> reactionNodeStyleCBox;

	@FXML
	private ChoiceBox<NodeView.NodeStyle> foodNodeStyleCBox;

	@FXML
	private ColorPicker reactionEdgesColorCBox;

	@FXML
	private ColorPicker catlystEdgesColorCBox;

	@FXML
	private ColorPicker inhibitorEdgesColorCBox;

	@FXML
	private ColorPicker reactionNodesColorCBox;

	@FXML
	private ChoiceBox<NodeView.NodeStyle> moleculeNodeStyleCBox;

	@FXML
	private ColorPicker moleculeNodesColorCBox;

	@FXML
	private ChoiceBox<Integer> moleculeNodesSizeCBox;

	@FXML
	private CheckBox useColorsInAnimationRadioButton;

	@FXML
	private CheckBox moveLabelsInAnimationCheckBox;

	@FXML
	private ColorPicker foodNodesColorCBox;

	@FXML
	private ChoiceBox<Integer> reactionEdgesLineWidthCBox;

	@FXML
	private ChoiceBox<Integer> catalystEdgesLineWidthCBox;

	@FXML
	private ChoiceBox<Integer> inhibitionEdgesLineWidthCBox;

	@FXML
	private ChoiceBox<Integer> reactionNodesSizeCBox;

	@FXML
	private ChoiceBox<Integer> foodNodesSizeCBox;

	@FXML
	private RadioButton singleArrowRadioButton;

	@FXML
	private RadioButton doubleArrowRadioButton;

	@FXML
	private RadioButton fullNotationRadioButton;

	@FXML
	private RadioButton sparseNotationRadioButton;

	@FXML
	private RadioButton tabbedFormatRadioButton;

	@FXML
	private CheckBox wrapTextCheckBox;

	@FXML
	private TextArea infoTextArea;

	@FXML
	private Button clearDisplayLabelsButton;

	@FXML
	private Button importDisplayLabelsButton;

	@FXML
	private Button exportDisplayLabelsButton;

	@FXML
	private ListView<Pair<String, String>> displayLabelListView;

	@FXML
	private CheckBox activeDisplayLabelsCheckBox;

	@FXML
	private Button addDisplayLabelButton;

	@FXML
	private void initialize() {
		infoTextArea.setText(ProgramProperties.getProgramVersion() + "\nDaniel H. Huson, Joana C. Xavier and  Mike A. Steel. License GPL3");
		MaterialIcons.setIcon(clearDisplayLabelsButton, MaterialIcons.delete);
		MaterialIcons.setIcon(importDisplayLabelsButton, MaterialIcons.arrow_downward);
		MaterialIcons.setIcon(exportDisplayLabelsButton, MaterialIcons.arrow_upward);
		MaterialIcons.setIcon(addDisplayLabelButton, MaterialIcons.add);

		ListViewUtils.setupEditing(displayLabelListView, true);
	}

	public TextField getIterationsTextField() {
		return iterationsTextField;
	}

	public TextField getMaxSizeNetworkTextField() {
		return maxSizeNetworkTextField;
	}

	public ChoiceBox<EdgeView.EdgeStyle> getReactionEdgeStyleCBox() {
		return reactionEdgeStyleCBox;
	}

	public ChoiceBox<EdgeView.EdgeStyle> getCatalystEdgeStyleCBox() {
		return catalystEdgeStyleCBox;
	}

	public ChoiceBox<EdgeView.EdgeStyle> getInhibitionEdgeStyleCBox() {
		return inhibitionEdgeStyleCBox;
	}

	public ChoiceBox<NodeView.NodeStyle> getReactionNodeStyleCBox() {
		return reactionNodeStyleCBox;
	}

	public ChoiceBox<NodeView.NodeStyle> getFoodNodeStyleCBox() {
		return foodNodeStyleCBox;
	}

	public ColorPicker getReactionEdgesColorCBox() {
		return reactionEdgesColorCBox;
	}

	public ColorPicker getCatlystEdgesColorCBox() {
		return catlystEdgesColorCBox;
	}

	public ColorPicker getInhibitorEdgesColorCBox() {
		return inhibitorEdgesColorCBox;
	}

	public ColorPicker getReactionNodesColorCBox() {
		return reactionNodesColorCBox;
	}

	public ChoiceBox<NodeView.NodeStyle> getMoleculeNodeStyleCBox() {
		return moleculeNodeStyleCBox;
	}

	public ColorPicker getMoleculeNodesColorCBox() {
		return moleculeNodesColorCBox;
	}

	public ChoiceBox<Integer> getMoleculeNodesSizeCBox() {
		return moleculeNodesSizeCBox;
	}

	public ColorPicker getFoodNodesColorCBox() {
		return foodNodesColorCBox;
	}

	public ChoiceBox<Integer> getReactionEdgesLineWidthCBox() {
		return reactionEdgesLineWidthCBox;
	}

	public ChoiceBox<Integer> getCatalystEdgesLineWidthCBox() {
		return catalystEdgesLineWidthCBox;
	}

	public ChoiceBox<Integer> getInhibitionEdgesLineWidthCBox() {
		return inhibitionEdgesLineWidthCBox;
	}

	public ChoiceBox<Integer> getReactionNodesSizeCBox() {
		return reactionNodesSizeCBox;
	}

	public ChoiceBox<Integer> getFoodNodesSizeCBox() {
		return foodNodesSizeCBox;
	}

	public RadioButton getSingleArrowRadioButton() {
		return singleArrowRadioButton;
	}

	public RadioButton getDoubleArrowRadioButton() {
		return doubleArrowRadioButton;
	}

	public RadioButton getFullNotationRadioButton() {
		return fullNotationRadioButton;
	}

	public RadioButton getSparseNotationRadioButton() {
		return sparseNotationRadioButton;
	}

	public RadioButton getTabbedFormatRadioButton() {
		return tabbedFormatRadioButton;
	}

	public CheckBox getWrapTextCheckBox() {
		return wrapTextCheckBox;
	}

	public TitledPane getTitledPane() {
		return titledPane;
	}

	public CheckBox getUseColorsInAnimationRadioButton() {
		return useColorsInAnimationRadioButton;
	}

	public CheckBox getMoveLabelsInAnimationCheckBox() {
		return moveLabelsInAnimationCheckBox;
	}

	public Button getClearDisplayLabelsButton() {
		return clearDisplayLabelsButton;
	}

	public Button getImportDisplayLabelsButton() {
		return importDisplayLabelsButton;
	}

	public Button getExportDisplayLabelsButton() {
		return exportDisplayLabelsButton;
	}

	public ListView<Pair<String, String>> getDisplayLabelListView() {
		return displayLabelListView;
	}

	public CheckBox getActiveDisplayLabelsCheckBox() {
		return activeDisplayLabelsCheckBox;
	}

	public Button getAddDisplayLabelButton() {
		return addDisplayLabelButton;
	}
}
