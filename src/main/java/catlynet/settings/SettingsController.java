/*
 * SettingsController.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.settings;

import catlynet.main.CatlyNet;
import catlynet.view.EdgeView;
import catlynet.view.NodeView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import jloda.fx.util.ProgramProperties;

public class SettingsController {
	@FXML
	private TitledPane titledPane;

	@FXML
	private TextField iterationsTextField;

	@FXML
	private CheckBox darkModeCheckBox;

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
	private void initialize() {
		titledPane.getStylesheets().add(getClass().getResource("floating-titled-pane.css").toExternalForm());
		infoTextArea.setText(ProgramProperties.getProgramVersion() + "\n" + CatlyNet.getOptions().getAuthors() + ". License GPL3");
	}

	public TextField getIterationsTextField() {
		return iterationsTextField;
	}

	public CheckBox getDarkModeCheckBox() {
		return darkModeCheckBox;
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
}
