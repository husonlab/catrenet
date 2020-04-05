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
import jloda.fx.control.ItemSelectionModel;
import jloda.graph.*;

import java.util.ArrayList;
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

        controller.getSelectAllMenuItem().setOnAction(e -> {
            if (inputHasFocus.get()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().selectAll();
            } else {
                select(window.getInputReactionSystem(), view, true, true, false, false, false, false);
            }
        });
        controller.getSelectAllMenuItem().disableProperty().bind(inputHasFocus.not().and(visualizationHasFocus.not()));

        controller.getSelectNoneMenuItem().setOnAction(e -> {
            if (inputHasFocus.get()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().selectRange(0, 0);
            } else {
                view.getNodeSelection().clearSelection();
                view.getEdgeSelection().clearSelection();
            }
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(inputHasFocus.not().and(visualizationHasFocus.not()));

        controller.getSelectNoneContextMenuItem().setOnAction(controller.getSelectNoneMenuItem().getOnAction());
        controller.getSelectNoneContextMenuItem().disableProperty().bind(controller.getSelectNoneMenuItem().disableProperty());

        controller.getSelectInvertedMenuItem().setOnAction(c -> {
            view.getReactionGraph().nodeStream().forEach(v -> view.getNodeSelection().toggleSelection(v));
            view.getReactionGraph().edgeStream().forEach(e -> view.getEdgeSelection().toggleSelection(e));
        });
        controller.getSelectInvertedMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectNodesMenuItem().setOnAction(e -> select(window.getInputReactionSystem(), view, true, false, false, false, false, false));
        controller.getSelectNodesMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectEdgesMenuItem().setOnAction(e -> select(window.getInputReactionSystem(), view, false, true, false, false, false, false));
        controller.getSelectEdgesMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectFoodMenuItem().setOnAction(e -> select(window.getInputReactionSystem(), view, false, false, true, false, false, false));
        controller.getSelectFoodMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectMoleculesMenuItem().setOnAction(e -> select(window.getInputReactionSystem(), view, false, false, false, false, true, false));
        controller.getSelectMoleculesMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectReactionsMenuItem().setOnAction(e -> select(window.getInputReactionSystem(), view, false, false, false, true, false, false));
        controller.getSelectReactionsMenuItem().disableProperty().bind(visualizationHasFocus.not());

        controller.getSelectConnectedComponentMenuItem().setOnAction(c -> {
            final Graph reactionGraph = view.getReactionGraph();
            final NodeSet nodes = new NodeSet(reactionGraph);
            new ArrayList<>(view.getNodeSelection().getSelectedItems()).forEach(v -> reactionGraph.visitConnectedComponent(v, nodes));
            view.getNodeSelection().selectItems(nodes);
            final EdgeSet edges = new EdgeSet(reactionGraph);
            for (Node p : nodes) {
                for (Edge f : p.adjacentEdges()) {
                    if (nodes.contains(f.getOpposite(p)))
                        edges.add(f);
                }
            }
            view.getEdgeSelection().selectItems(edges);
        });
        controller.getSelectConnectedComponentMenuItem().disableProperty().bind(visualizationHasFocus.not().or(view.getNodeSelection().emptyProperty()));

        controller.getSelectConnectedComponentContextMenuItem().setOnAction(controller.getSelectConnectedComponentMenuItem().getOnAction());
        controller.getSelectConnectedComponentContextMenuItem().disableProperty().bind(controller.getSelectConnectedComponentMenuItem().disableProperty());

        controller.getSelectMaxCAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.maxCAF)));
        controller.getSelectMaxCAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.maxCAF).sizeProperty().isEqualTo(0)));

        controller.getSelectCAFContextMenuItem().setOnAction(controller.getSelectMaxCAFMenuItem().getOnAction());
        controller.getSelectCAFContextMenuItem().disableProperty().bind(controller.getSelectMaxCAFMenuItem().disableProperty());

        controller.getSelectMaxRAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.maxRAF)));
        controller.getSelectMaxRAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.maxRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectRAFContextMenuItem().setOnAction(controller.getSelectMaxRAFMenuItem().getOnAction());
        controller.getSelectRAFContextMenuItem().disableProperty().bind(controller.getSelectMaxRAFMenuItem().disableProperty());

        controller.getSelectMaxPseudoRAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.maxPseudoRAF)));
        controller.getSelectMaxPseudoRAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.maxPseudoRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectPseudoRAFContextMenuItem().setOnAction(controller.getSelectMaxPseudoRAFMenuItem().getOnAction());
        controller.getSelectPseudoRAFContextMenuItem().disableProperty().bind(controller.getSelectMaxPseudoRAFMenuItem().disableProperty());

        controller.getSelectMinIrrRAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.minIrrRAF)));
        controller.getSelectMinIrrRAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.minIrrRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectMinIrrRAFContextMenuItem().setOnAction(controller.getSelectMinIrrRAFMenuItem().getOnAction());
        controller.getSelectMinIrrRAFContextMenuItem().disableProperty().bind(controller.getSelectMinIrrRAFMenuItem().disableProperty());

        controller.getSelectQuotientRAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.QuotientRAF)));
        controller.getSelectQuotientRAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.QuotientRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectQuotientRAFContextMenuItem().setOnAction(controller.getSelectQuotientRAFMenuItem().getOnAction());
        controller.getSelectQuotientRAFContextMenuItem().disableProperty().bind(controller.getSelectQuotientRAFMenuItem().disableProperty());


        controller.getSelectMuCAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.muCAF)));
        controller.getSelectMuCAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.muCAF).sizeProperty().isEqualTo(0)));

        controller.getSelectMuCAFContextMenuItem().setOnAction(controller.getSelectMuCAFMenuItem().getOnAction());
        controller.getSelectMuCAFContextMenuItem().disableProperty().bind(controller.getSelectMuCAFMenuItem().disableProperty());

        controller.getSelectURAFMenuItem().setOnAction(e -> selectForAlgorithm(view, window.getReactionSystem(ReactionSystem.Type.uRAF)));
        controller.getSelectURAFMenuItem().disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(ReactionSystem.Type.uRAF).sizeProperty().isEqualTo(0)));

        controller.getSelectURAFContextMenuItem().setOnAction(controller.getSelectURAFMenuItem().getOnAction());
        controller.getSelectURAFContextMenuItem().disableProperty().bind(controller.getSelectURAFMenuItem().disableProperty());
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
                    || (food & v.getInfo() instanceof MoleculeType && reactionSystem.getFoods().contains(v.getInfo()))
                    || (molecules && v.getInfo() instanceof MoleculeType))
                view.getNodeSelection().select(v);
            else if (deselectNonMatched)
                view.getNodeSelection().clearSelection(v);
        }
        if (edges) {
            view.getEdgeSelection().selectItems(view.getReactionGraph().getEdgesAsSet());
        } else if (deselectNonMatched)
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

        final ItemSelectionModel<Node> nodeSelection = view.getNodeSelection();
        final ItemSelectionModel<Edge> edgeSelection = view.getEdgeSelection();

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
                    if (!nodeSelection.isSelected(w)) {
                        allNeighborsSelected = false;
                        break;
                    }
                }
                if (allNeighborsSelected)
                    nodeSelection.select(v);
            }
        }

        view.getReactionGraph().edgeStream().filter(e -> nodeSelection.isSelected(e.getSource()) && nodeSelection.isSelected(e.getTarget())).forEach(edgeSelection::select);
    }
}
