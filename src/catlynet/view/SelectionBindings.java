/*
 * SelectionBindings.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.view;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.graph.Edge;
import jloda.graph.Node;

import java.util.Set;

/**
 * setup bindings for selection menu items
 * Daniel Huson, 7.2019
 */
public class SelectionBindings {
    /**
     * setup
     *
     * @param window
     * @param controller
     */
    public static void setup(MainWindow window, MainWindowController controller) {
        final ReactionGraphView view = window.getReactionGraphView();

        final BooleanProperty inputHasFocus = new SimpleBooleanProperty();
        inputHasFocus.bind(controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().textProperty().isNotEmpty()));

        final BooleanProperty visualizationHasFocus = new SimpleBooleanProperty();
        visualizationHasFocus.bind(inputHasFocus.not().and(controller.getVisualizationTab().selectedProperty()));

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            if (inputHasFocus.get()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().selectAll();
            } else {
                select(window.getInputReactionSystem(), view, true, true, false, false, false, false);
            }
        });
        controller.getSelectAllMenuItem().disableProperty().bind(inputHasFocus.not().and(visualizationHasFocus.not()));

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            if (inputHasFocus.get()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().selectRange(0, 0);
            } else {
                select(window.getInputReactionSystem(), view, false, false, false, false, false, true);
            }
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(inputHasFocus.not().and(visualizationHasFocus.not()));

        controller.getSelectNoneContextMenuItem().setOnAction(controller.getSelectNodesMenuItem().getOnAction());
        controller.getSelectNoneContextMenuItem().disableProperty().bind(controller.getSelectNoneMenuItem().disableProperty());

        controller.getSelectInvertedMenuItem().setOnAction((e) -> {
            view.getNodeSelection().invertSelection();
            view.getEdgeSelection().invertSelection();
        });
        controller.getSelectInvertedMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectNodesMenuItem().setOnAction((e) -> {
            select(window.getInputReactionSystem(), view, true, false, false, false, false, false);

        });
        controller.getSelectNodesMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectEdgesMenuItem().setOnAction((e) -> {
            select(window.getInputReactionSystem(), view, false, true, false, false, false, false);

        });
        controller.getSelectEdgesMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectFoodMenuItem().setOnAction((e) -> {
            select(window.getInputReactionSystem(), view, false, false, true, false, false, false);

        });
        controller.getSelectFoodMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectMoleculesMenuItem().setOnAction((e) -> {
            select(window.getInputReactionSystem(), view, false, false, false, false, true, false);

        });
        controller.getSelectMoleculesMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectReactionsMenuItem().setOnAction((e) -> {
            select(window.getInputReactionSystem(), view, false, false, false, true, false, false);

        });
        controller.getSelectReactionsMenuItem().disableProperty().bind(visualizationHasFocus.not());


        controller.getSelectMaxCAFMenuItem().setOnAction((e) -> {
            selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.maxCAF));
        });
        controller.getSelectMaxCAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.maxCAF).sizeProperty().isEqualTo(0)));

        controller.getSelectCAFContextMenuItem().setOnAction(controller.getSelectMaxCAFMenuItem().getOnAction());
        controller.getSelectCAFContextMenuItem().disableProperty().bind(controller.getSelectMaxCAFMenuItem().disableProperty());

        controller.getSelectMuCAFMenuItem().setOnAction((e) -> {
            selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.muCAF));
        });
        controller.getSelectMuCAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.muCAF).sizeProperty().isEqualTo(0)));

        controller.getSelectMuCAFMenuItem().setOnAction(controller.getSelectMuCAFMenuItem().getOnAction());
        controller.getSelectMuCAFContextMenuItem().disableProperty().bind(controller.getSelectMuCAFMenuItem().disableProperty());

        controller.getSelectMaxRAFMenuItem().setOnAction((e) -> {
            selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.maxRAF));
        });
        controller.getSelectMaxRAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.maxRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectRAFContextMenuItem().setOnAction(controller.getSelectMaxRAFMenuItem().getOnAction());
        controller.getSelectRAFContextMenuItem().disableProperty().bind(controller.getSelectMaxRAFMenuItem().disableProperty());

        controller.getSelectMaxPseudoRAFMenuItem().setOnAction((e) -> {
            selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.maxPseudoRAF));

        });
        controller.getSelectMaxPseudoRAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.maxPseudoRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectPseudoRAFContextMenuItem().setOnAction(controller.getSelectMaxPseudoRAFMenuItem().getOnAction());
        controller.getSelectPseudoRAFContextMenuItem().disableProperty().bind(controller.getSelectMaxPseudoRAFMenuItem().disableProperty());
    }

    /**
     * select a given subset of nodes and/or edges
     *
     * @param reactionSystem
     * @param view
     * @param nodes
     * @param edges
     * @param food
     * @param reactions
     * @param molecules
     * @param deselectNonMatched
     */
    private static void select(ReactionSystem reactionSystem, ReactionGraphView view, boolean nodes, boolean edges, boolean food, boolean reactions, boolean molecules, boolean deselectNonMatched) {
        for (Node v : view.getReactionGraph().nodes()) {
            if (nodes || (reactions && v.getInfo() instanceof Reaction)
                    || (food & v.getInfo() instanceof MoleculeType && reactionSystem.getFoods().contains((MoleculeType) v.getInfo()))
                    || (molecules && v.getInfo() instanceof MoleculeType))
                view.getNodeSelection().select(v);
            else if (deselectNonMatched)
                view.getNodeSelection().clearSelection(v);
        }
        if (edges)
            view.getEdgeSelection().selectAll();
        else if (deselectNonMatched)
            view.getEdgeSelection().clearSelection();
    }

    /**
     * select subgraph associated with given (sub) model
     *
     * @param view
     * @param subReactionSystem
     */
    private static void selectForAlgorithm(ReactionGraphView view, ReactionSystem subReactionSystem) {
        final Set<MoleculeType> molecules = subReactionSystem.getMoleculeTypes(true, true, true, false, false);

        final AMultipleSelectionModel<Node> nodeSelection = view.getNodeSelection();
        final AMultipleSelectionModel<Edge> edgeSelection = view.getEdgeSelection();

        nodeSelection.clearSelection();
        edgeSelection.clearSelection();

        for (Node v : view.getReactionGraph().nodes()) {
            if (v.getInfo() instanceof Reaction) {
                final Reaction reaction = (Reaction) v.getInfo();
                if (subReactionSystem.getReactionNames().contains(reaction.getName()))
                    nodeSelection.select(v);
            } else if (v.getInfo() instanceof MoleculeType) {
                final MoleculeType moleculeType = (MoleculeType) v.getInfo();
                if (molecules.contains(moleculeType))
                    nodeSelection.select(v);
            }
        }
        for (Node v : view.getReactionGraph().nodes()) {
            if (v.getInfo() instanceof ReactionGraphView.AndNode) {
                boolean allNeighborsSelected = true;
                for (Node w : v.adjacentNodes()) {
                    if (!nodeSelection.getSelectedItems().contains(w)) {
                        allNeighborsSelected = false;
                        break;
                    }
                }
                if (allNeighborsSelected)
                    nodeSelection.select(v);
            }
        }

        for (Edge e : view.getReactionGraph().edges()) {
            if (nodeSelection.getSelectedItems().contains(e.getSource()) && nodeSelection.getSelectedItems().contains(e.getTarget()))
                edgeSelection.select(e);
        }
    }
}
