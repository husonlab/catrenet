/*
 * VFormatWindow.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.vformat;

import catlynet.main.Version;
import catlynet.view.EdgeView;
import catlynet.view.NodeView;
import catlynet.window.MainWindow;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.window.MainWindowManager;
import jloda.util.ProgramProperties;

import java.util.Arrays;
import java.util.List;

public class VFormatWindow {
    public static final String title = "Node and Edge Format";

    private final Stage stage;

    /**
     * construct the format dialog for the given window
     *
     * @param mainWindow
     */
    public VFormatWindow(MainWindow mainWindow) {
        final ExtendedFXMLLoader<VFormatWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        Parent root = extendedFXMLLoader.getRoot();
        VFormatWindowController controller = extendedFXMLLoader.getController();

        stage = new Stage();
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.setX(mainWindow.getStage().getX() + 150);
        stage.setY(mainWindow.getStage().getY() + 150);

        stage.setTitle(title + " - " + Version.NAME);
        stage.show();

        // ensures that window can't be resized too small:
        Platform.runLater(() -> {
            stage.setMinWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
        });

        MainWindowManager.getInstance().addAuxiliaryWindow(mainWindow, stage);

        {
            final NodeView nodeView = NodeView.createNullNodeView();

            controller.getReactionNodeStyleCBox().getItems().addAll(NodeView.NodeStyle.values());
            controller.getReactionNodeStyleCBox().setValue(nodeView.getReactionNodeShape());

            controller.getFoodNodeStyleCBox().getItems().addAll(NodeView.NodeStyle.values());
            controller.getFoodNodeStyleCBox().setValue(nodeView.getFoodNodeShape());

            controller.getMoleculeNodeStyleCBox().getItems().addAll(NodeView.NodeStyle.values());
            controller.getMoleculeNodeStyleCBox().setValue(nodeView.getMoleculeNodeShape());

            controller.getReactionNodesColorCBox().setValue(nodeView.getReactionNodeFillColor());
            controller.getFoodNodesColorCBox().setValue(nodeView.getFoodNodeFillColor());
            controller.getMoleculeNodesColorCBox().setValue(nodeView.getMoleculeNodeFillColor());

            final List<Integer> values = Arrays.asList(1, 2, 4, 6, 8, 10, 12, 16, 20);

            controller.getReactionNodesSizeCBox().getItems().addAll(values);
            controller.getReactionNodesSizeCBox().setValue(nodeView.getReactionNodeSize());

            controller.getFoodNodesSizeCBox().getItems().addAll(values);
            controller.getFoodNodesSizeCBox().setValue(nodeView.getFoodNodeSize());

            controller.getMoleculeNodesSizeCBox().getItems().addAll(values);
            controller.getMoleculeNodesSizeCBox().setValue(nodeView.getMoleculeNodeSize());
        }

        {
            final EdgeView edgeView = EdgeView.createNullEdgeView();

            controller.getReactionEdgeStyleCBox().getItems().addAll(EdgeView.EdgeStyle.values());
            controller.getReactionEdgeStyleCBox().setValue(edgeView.getReactionEdgeStyle());

            controller.getCatalystEdgeStyleCBox().getItems().addAll(EdgeView.EdgeStyle.values());
            controller.getCatalystEdgeStyleCBox().setValue(edgeView.getCatalystEdgeStyle());

            controller.getInhibitionEdgeStyleCBox().getItems().addAll(EdgeView.EdgeStyle.values());
            controller.getInhibitionEdgeStyleCBox().setValue(edgeView.getInhibitionEdgeStyle());

            controller.getReactionEdgesColorCBox().setValue(edgeView.getReactionColor());
            controller.getCatlystEdgesColorCBox().setValue(edgeView.getCatalystColor());
            controller.getInhibitorEdgesColorCBox().setValue(edgeView.getInhibitionColor());

            final List<Integer> values = Arrays.asList(1, 2, 4, 6, 8, 10, 12, 16, 20);

            controller.getReactionEdgesLineWidthCBox().getItems().addAll(values);
            controller.getReactionEdgesLineWidthCBox().setValue(edgeView.getReactionEdgeWidth());

            controller.getCatalystEdgesLineWidthCBox().getItems().addAll(values);
            controller.getCatalystEdgesLineWidthCBox().setValue(edgeView.getCatalystEdgeWidth());

            controller.getInhibitionEdgesLineWidthCBox().getItems().addAll(values);
            controller.getInhibitionEdgesLineWidthCBox().setValue(edgeView.getInhibitionEdgeWidth());
        }

        controller.getCancelButton().setOnAction(e -> {
            stage.hide();
        });

        controller.getApplyButton().setOnAction(e -> {
            stage.hide();

            ProgramProperties.put("reactionNodeStyle", controller.getReactionNodeStyleCBox().getValue().name());
            ProgramProperties.put("moleculeNodeStyle", controller.getMoleculeNodeStyleCBox().getValue().name());
            ProgramProperties.put("foodNodeStyle", controller.getFoodNodeStyleCBox().getValue().name());

            ProgramProperties.put("reactionNodeFillColor", controller.getReactionNodesColorCBox().getValue());
            ProgramProperties.put("moleculeNodeFillColor", controller.getMoleculeNodesColorCBox().getValue());
            ProgramProperties.put("foodNodeFillColor", controller.getFoodNodesColorCBox().getValue());

            ProgramProperties.put("reactionNodeSize", controller.getReactionNodesSizeCBox().getValue());
            ProgramProperties.put("moleculeNodeSize", controller.getMoleculeNodesSizeCBox().getValue());
            ProgramProperties.put("foodNodeSize", controller.getFoodNodesSizeCBox().getValue());

            ProgramProperties.put("reactionEdgeStyle", controller.getReactionEdgeStyleCBox().getValue().name());
            ProgramProperties.put("catalystEdgeStyle", controller.getCatalystEdgeStyleCBox().getValue().name());
            ProgramProperties.put("inhibitionEdgeStyle", controller.getInhibitionEdgeStyleCBox().getValue().name());

            ProgramProperties.put("reactionColor", controller.getReactionEdgesColorCBox().getValue());
            ProgramProperties.put("catalystColor", controller.getCatlystEdgesColorCBox().getValue());
            ProgramProperties.put("inhibitionColor", controller.getInhibitorEdgesColorCBox().getValue());

            ProgramProperties.put("reactionEdgeWidth", controller.getReactionEdgesLineWidthCBox().getValue());
            ProgramProperties.put("catalystEdgeWidth", controller.getCatalystEdgesLineWidthCBox().getValue());
            ProgramProperties.put("inhibitionEdgeWidth", controller.getInhibitionEdgesLineWidthCBox().getValue());
        });
    }

    public Stage getStage() {
        return stage;
    }
}
