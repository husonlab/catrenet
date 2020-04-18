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
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.MenuItem;
import jloda.fx.control.ItemSelectionModel;
import jloda.graph.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * setup bindings for selection menu items
 * Daniel Huson, 7.2019
 */
public class SelectionBindings {
    private static final ObservableSet<String> previousSelection = FXCollections.observableSet(new TreeSet<>());
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

        window.getDocument().getDefinedSystems().addListener((InvalidationListener) c -> controller.getSelectReactionSystemMenu().getItems()
                .setAll(window.getDocument().getDefinedSystems().stream().map(window::getReactionSystem).map(r -> {
                    final MenuItem menuItem = new MenuItem(r.getName());
                    menuItem.setOnAction(z -> selectForAlgorithm(view, window.getReactionSystem(r.getName())));
                    menuItem.disableProperty().bind(visualizationHasFocus.not().or(window.getReactionSystem(r.getName()).sizeProperty().isEqualTo(0)));
                    return menuItem;
                }).collect(Collectors.toList())));

        final List<MenuItem> additionalContextMenuItems = new ArrayList<>(controller.getVisualizationContextMenu().getItems());

        window.getDocument().getDefinedSystems().addListener((InvalidationListener) c -> {
                    controller.getVisualizationContextMenu().getItems()
                            .setAll(window.getDocument().getDefinedSystems().stream().map(window::getReactionSystem).map(r -> {
                                final MenuItem menuItem = new MenuItem("Select " + r.getName());
                                menuItem.setOnAction(z -> selectForAlgorithm(view, window.getReactionSystem(r.getName())));
                                menuItem.disableProperty().bind(window.getReactionSystem(r.getName()).sizeProperty().isEqualTo(0));
                                return menuItem;
                            }).collect(Collectors.toList()));
                    controller.getVisualizationContextMenu().getItems().addAll(additionalContextMenuItems);
                }
        );

        window.getStage().focusedProperty().addListener((c, o, n) -> {
            if (!n) {
                previousSelection.clear();
                previousSelection.addAll(window.getReactionGraphView().getNodeSelection().getSelectedItems().stream().map(v -> window.getReactionGraphView().getLabel(v).getText())
                        .filter(text -> text.length() > 0 && !text.equals("&")).collect(Collectors.toSet()));
            }
        });

        controller.getSelectFromPreviousWindowMenuItem().setOnAction(c -> {
            if (previousSelection.size() > 0) {
                for (Node v : view.getReactionGraph().nodes()) {
                    final String text = view.getLabel(v).getText();
                    if (previousSelection.contains(text))
                        view.getNodeSelection().select(v);

                }
            }
        });
        controller.getSelectFromPreviousWindowMenuItem().disableProperty().bind(visualizationHasFocus.not().or(Bindings.isEmpty(previousSelection)));
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
        // don't select inactive copies:
        if (view.isUseMultiCopyFoodNodes()) {
            for (Node v : view.getReactionGraph().nodes()) {
                if (v.getInfo() instanceof MoleculeType) {
                    final Optional<Boolean> hasSelectedNeighbor = v.adjacentNodeStream(true).map(nodeSelection::isSelected).findAny();
                    if (hasSelectedNeighbor.isEmpty() || !hasSelectedNeighbor.get())
                        nodeSelection.clearSelection(v);
                }
            }
        }


        view.getReactionGraph().edgeStream().filter(e -> nodeSelection.isSelected(e.getSource()) && nodeSelection.isSelected(e.getTarget())).forEach(edgeSelection::select);
    }
}
