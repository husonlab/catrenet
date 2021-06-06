/*
 * ReactionGraphView.java Copyright (C) 2019. Daniel H. Huson
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
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindowController;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.util.AService;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.SelectionEffectBlue;
import jloda.fx.window.NotificationManager;
import jloda.graph.*;
import jloda.graph.algorithms.ConnectedComponents;
import jloda.graph.algorithms.FruchtermanReingoldLayout;
import jloda.graph.fmm.MultiComponents;
import jloda.util.APoint2D;
import jloda.util.Basic;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * maintains the visualization of a model
 * Daniel Huson, 7.2019
 */
public class ReactionGraphView {
    private final static ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font("Helvetica", 12));

    public enum Type {fullGraph, dependencyGraph, reactantDependencyGraph}

    private final ObjectProperty<Type> graphType = new SimpleObjectProperty<>();

    private final Graph reactionGraph = new Graph();
    private final NodeSet foodNodes = new NodeSet(reactionGraph);
    private final NodeArray<NodeView> node2view = new NodeArray<>(reactionGraph);
    private final EdgeArray<EdgeView> edge2view = new EdgeArray<>(reactionGraph);

    private final ItemSelectionModel<Node> nodeSelection = new ItemSelectionModel<>();
    private final ItemSelectionModel<Edge> edgeSelection = new ItemSelectionModel<>();

    private final ObjectProperty<Color> inhibitionEdgeColor = new SimpleObjectProperty<>(Color.LIGHTGREY);

    private final BooleanProperty suppressCatalystEdges = new SimpleBooleanProperty(false);
    private final BooleanProperty useMultiCopyFoodNodes = new SimpleBooleanProperty(false);

    private final IntegerProperty embeddingIterations = new SimpleIntegerProperty(1000);

    private final MainWindowController controller;

    private final BooleanProperty empty = new SimpleBooleanProperty(true);

    private final StringProperty nodeLabelStyle = new SimpleStringProperty("");

    static class AndNode {
    }

    private final ReactionSystem reactionSystem;

    private final Group world;

    private final MoleculeFlowAnimation moleculeFlowAnimation;

    private final PrintStream logStream;

    /**
     * construct a graph view for the given system
     *
     * @param reactionSystem
     */
    public ReactionGraphView(ReactionSystem reactionSystem, MainWindowController controller, PrintStream logStream) {
        this.reactionSystem = reactionSystem;
        this.controller = controller;
        this.world = new Group();
        this.logStream = logStream;

        nodeSelection.getSelectedItems().addListener((ListChangeListener<Node>) (e) -> {
            while (e.next()) {
                for (Node v : e.getAddedSubList()) {
                    final NodeView nv = node2view.get(v);
                    if (nv != null) {
                        nv.getLabel().setEffect(SelectionEffectBlue.getInstance());
                        nv.getShape().setEffect(SelectionEffectBlue.getInstance());
                    }
                }
                for (Node v : e.getRemoved()) {
                    final NodeView nv = node2view.get(v);
                    if (nv != null) {
                        nv.getLabel().setEffect(null);
                        nv.getShape().setEffect(null);
                    }
                }
            }
        });

        edgeSelection.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            while (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    final Group group = edge2view.get(e);
                    if (group != null) {
                        for (javafx.scene.Node node : group.getChildren())
                            node.setEffect(SelectionEffectBlue.getInstance());
                    }
                }
                for (Edge e : c.getRemoved()) {
                    final Group group = edge2view.get(e);
                    if (group != null) {
                        for (javafx.scene.Node node : group.getChildren())
                            node.setEffect(null);
                    }
                }
            }
        });

        inhibitionEdgeColor.addListener((c, o, n) -> {
            for (Edge e : reactionGraph.edges()) {
                if (e.getInfo() == EdgeType.Inhibitor) {
                    for (javafx.scene.Node node : edge2view.get(e).getChildren()) {
                        if (node instanceof Path || node instanceof Polyline)
                            ((Shape) node).setStroke(n);
                    }
                }
            }
        });

        nodeLabelStyle.addListener((c, o, n) -> {

            for (Node v : reactionGraph.nodes()) {
                node2view.get(v).getLabel().setStyle(n);
            }
        });

        moleculeFlowAnimation = new MoleculeFlowAnimation(reactionGraph, foodNodes, edge2view, world);

        moleculeFlowAnimation.animateInhibitionsProperty().addListener((c, o, n) -> inhibitionEdgeColor.set(n ? Color.BLACK : Color.LIGHTGREY));

        graphType.addListener(c -> update());
        suppressCatalystEdges.addListener(c -> update());
        useMultiCopyFoodNodes.addListener(c -> update());
    }

    /**
     * apply the visualization
     */
    public void update() {
        clear();

        //System.err.println("Updating graph");
        final Map<MoleculeType, Node> molecule2node = new HashMap<>();

        if (getGraphType() == Type.dependencyGraph)
            SetupDependencyGraph.apply(reactionGraph, reactionSystem, true);
        else if (getGraphType() == Type.reactantDependencyGraph)
            SetupDependencyGraph.apply(reactionGraph, reactionSystem, false);
        else
            SetupFullGraph.apply(reactionGraph, reactionSystem, foodNodes, molecule2node, isSuppressCatalystEdges(), isUseMultiCopyFoodNodes());

        final int numberOfConnectedComponts = ConnectedComponents.count(reactionGraph);
        if (numberOfConnectedComponts > 1)
            logStream.printf("Reaction graph has %d connected components%n", numberOfConnectedComponts);

        final AService<NodeArray<APoint2D<?>>> service = new AService<>(controller.getStatusFlowPane());

        final NodeArray<APoint2D<?>> result = new NodeArray<>(reactionGraph);

        service.setCallable(() -> {
            var maxWidth = Math.max(600, controller.getVisualizationScrollPane().getViewportBounds().getWidth() - 100);
            if (true)
                MultiComponents.apply(null, 0.9 * maxWidth, maxWidth, 80, 80, reactionGraph, e -> 1d, (v, p) -> result.put(v, new APoint2D<>(p.getX(), p.getY(), v)));
            else {
                final FruchtermanReingoldLayout layout = new FruchtermanReingoldLayout(reactionGraph);
                layout.apply(getEmbeddingIterations(), result, service.getProgressListener(), ProgramExecutorService.getNumberOfCoresToUse());
            }
            return result;
        });

        /*
         service.setCallable(() -> {
            if(reactionGraph.isSimple())
                FastMultiLayerMethodLayout.apply(null,reactionGraph,e->1d,(v,p)->result.put(v,new APoint2D<>(p.getX(),p.getY(),v)));
            else {
                var simpleGraph=new Graph();
                var src2tar=reactionGraph.extract(new HashSet<>(reactionGraph.getNodesAsList()),subsetSimpleEdges(reactionGraph),simpleGraph);
                var tar2src=new NodeArray<Node>(simpleGraph);
                for(var v:src2tar.keys()) {
                    tar2src.put(src2tar.get(v),v);
                }
                FastMultiLayerMethodLayout.apply(null,simpleGraph,e->1d,(v,p)->result.put(tar2src.get(v),new APoint2D<>(p.getX(),p.getY(),tar2src.get(v))));
            }
            return result;
        });
         */

        service.setOnRunning(e -> service.getProgressListener().setTasks("Graph layout", ""));
        service.setOnFailed(e -> NotificationManager.showError("Graph layout failed: " + service.getException().getMessage()));
        service.setOnCancelled(e -> {
                    NotificationManager.showWarning("Graph layout CANCELED");
                    if (!result.isEmpty()) // use what ever has been produced
                        world.getChildren().setAll(setupGraphView(reactionSystem, reactionGraph, node2view, edge2view, service.getValue()));
                }
        );
        service.setOnSucceeded((e) -> {
            world.getChildren().setAll(setupGraphView(reactionSystem, reactionGraph, node2view, edge2view, service.getValue()));
            empty.set(reactionGraph.getNumberOfNodes() == 0);
        });
        service.start();
    }

    public void clear() {
        moleculeFlowAnimation.setPlaying(false);
        empty.set(true);
        foodNodes.clear();
        nodeSelection.clearSelection();
        edgeSelection.clearSelection();
        reactionGraph.clear();
        world.getChildren().clear();
    }

    public Group getWorld() {
        return world;
    }

    public Graph getReactionGraph() {
        return reactionGraph;
    }

    public ItemSelectionModel<Node> getNodeSelection() {
        return nodeSelection;
    }

    public ItemSelectionModel<Edge> getEdgeSelection() {
        return edgeSelection;
    }

    /**
     * set up the graph view
     *
     * @param graph
     * @param coordinates
     * @return graph view
     */
    private Collection<? extends javafx.scene.Node> setupGraphView(ReactionSystem reactionSystem, Graph graph, NodeArray<NodeView> node2view, EdgeArray<EdgeView> edge2view, NodeArray<APoint2D<?>> coordinates) {
        final Group spacers = new Group();
        final Group nodes = new Group();
        final Group edges = new Group();
        final Group labels = new Group();

        graph.nodeStream().forEach(v -> {
            final NodeView nv = new NodeView(this, reactionSystem.getFoods(), v, coordinates.get(v).getX(), coordinates.get(v).getY());
            nv.getLabel().setStyle(getNodeLabelStyle());
            node2view.put(v, nv);
            spacers.getChildren().add(nv.getSpacer());
            nodes.getChildren().add(nv.getShape());
            labels.getChildren().add(nv.getLabel());
        });

        for (Edge edge : graph.edges()) {
            final EdgeType edgeType = (EdgeType) edge.getInfo();

            final Shape sourceShape = node2view.get(edge.getSource()).getShape();
            final Shape targetShape = node2view.get(edge.getTarget()).getShape();

            final EdgeView edgeView = new EdgeView(this, edge, sourceShape.translateXProperty(), sourceShape.translateYProperty(), targetShape.translateXProperty(), targetShape.translateYProperty(), edgeType);

            edges.getChildren().add(edgeView);
            edge2view.put(edge, edgeView);
        }

        return Arrays.asList(spacers, edges, nodes, labels);
    }

    /**
     * setup mouse interaction
     *
     * @param mouseTarget
     * @param nodeToMove
     */
    public void setupMouseInteraction(javafx.scene.Node mouseTarget, javafx.scene.Node nodeToMove, Node v, Edge e) {
        mouseTarget.setCursor(Cursor.CROSSHAIR);

        final double[] mouseDown = new double[2];
        final boolean[] moved = {false};

        mouseTarget.setOnMousePressed(c -> {
            mouseDown[0] = c.getSceneX();
            mouseDown[1] = c.getSceneY();
            moved[0] = false;
            c.consume();
        });

        mouseTarget.setOnMouseDragged(c -> {
            final double mouseX = c.getSceneX();
            final double mouseY = c.getSceneY();

            if (v != null && !nodeToMove.translateXProperty().isBound()) {
                for (Node w : nodeSelection.getSelectedItems()) {
                    node2view.get(w).translate(mouseX - mouseDown[0], mouseY - mouseDown[1]);
                }
            } else {

                if (nodeToMove.translateXProperty().isBound())
                    nodeToMove.setLayoutX(nodeToMove.getLayoutX() + (mouseX - mouseDown[0]));
                else
                    nodeToMove.setTranslateX(nodeToMove.getTranslateX() + (mouseX - mouseDown[0]));
                if (nodeToMove.translateYProperty().isBound())
                    nodeToMove.setLayoutY(nodeToMove.getLayoutY() + (mouseY - mouseDown[1]));

                else
                    nodeToMove.setTranslateY(nodeToMove.getTranslateY() + (mouseY - mouseDown[1]));
            }

            mouseDown[0] = c.getSceneX();
            mouseDown[1] = c.getSceneY();
            moved[0] = true;

            c.consume();
        });

        mouseTarget.setOnMouseReleased(c -> {
            if (!moved[0] && (v != null || e != null)) {
                if (!c.isShiftDown()) {
                    nodeSelection.clearSelection();
                    edgeSelection.clearSelection();
                    if (v != null) {
                        nodeSelection.select(v);
                        if (isUseMultiCopyFoodNodes() && foodNodes.contains(v)) {
                            for (Node w : foodNodes) {
                                if (v.getInfo() == w.getInfo())
                                    nodeSelection.select(w);
                            }
                        }
                    }
                    if (e != null)
                        edgeSelection.select(e);
                } else {
                    if (v != null) {
                        if (nodeSelection.isSelected(v))
                            nodeSelection.clearSelection(v);
                        else
                            nodeSelection.select(v);
                    }
                    if (e != null) {
                        if (edgeSelection.isSelected(e))
                            edgeSelection.clearSelection(e);
                        else
                            edgeSelection.select(e);
                    }

                }
            }
        });

        mouseTarget.setOnMouseClicked(c -> {
            if (c.getClickCount() == 2) {
                if (v != null) {
                    nodeSelection.selectItems(Basic.asList(v.adjacentNodes()));
                    edgeSelection.selectItems(Basic.asList(v.adjacentEdges()));
                }
            } else if (c.getClickCount() == 3) {
                final NodeSet nodes = new NodeSet(reactionGraph);
                ConnectedComponents.collect(v, nodes);
                nodeSelection.selectItems(nodes);
                final EdgeSet edges = new EdgeSet(reactionGraph);
                for (Node p : nodes) {
                    for (Edge f : p.adjacentEdges()) {
                        if (nodes.contains(f.getOpposite(p)))
                            edges.add(f);
                    }
                }
                edgeSelection.selectItems(edges);
            }
        });
    }

    public MoleculeFlowAnimation getMoleculeFlowAnimation() {
        return moleculeFlowAnimation;
    }

    public Color getInhibitionEdgeColor() {
        return inhibitionEdgeColor.get();
    }

    public ObjectProperty<Color> inhibitionEdgeColorProperty() {
        return inhibitionEdgeColor;
    }

    public void setInhibitionEdgeColor(Color inhibitionEdgeColor) {
        this.inhibitionEdgeColor.set(inhibitionEdgeColor);
    }

    /**
     * find a path in a group
     *
     * @param group
     * @return path, if found
     */
    static Path getPath(Group group) {
        for (javafx.scene.Node child : group.getChildren()) {
            if (child instanceof Path)
                return (Path) child;
        }
        return null;
    }

    public static Font getFont() {
        return font.get();
    }

    public static ObjectProperty<Font> fontProperty() {
        return font;
    }

    public static void setFont(Font font) {
        ReactionGraphView.font.set(font);
    }

    public Shape getShape(Node v) {
        return node2view.get(v).getShape();
    }

    public Label getLabel(Node v) {
        return node2view.get(v).getLabel();
    }

    public String getNodeLabelStyle() {
        return nodeLabelStyle.get();
    }

    public StringProperty nodeLabelStyleProperty() {
        return nodeLabelStyle;
    }

    public void setNodeLabelStyle(String nodeLabelStyle) {
        this.nodeLabelStyle.set(nodeLabelStyle);
    }

    public Rectangle2D getBBox() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Node v : reactionGraph.nodes()) {
            final Shape shape = node2view.get(v).getShape();
            minX = Math.min(minX, shape.getTranslateX());
            minY = Math.min(minY, shape.getTranslateY());
            maxX = Math.max(maxX, shape.getTranslateY());
            maxY = Math.max(maxY, shape.getTranslateY());
        }
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean isEmpty() {
        return empty.get();
    }

    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    public boolean isSuppressCatalystEdges() {
        return suppressCatalystEdges.get();
    }

    public BooleanProperty suppressCatalystEdgesProperty() {
        return suppressCatalystEdges;
    }

    public void setSuppressCatalystEdges(boolean suppressCatalystEdges) {
        this.suppressCatalystEdges.set(suppressCatalystEdges);
    }

    public boolean isUseMultiCopyFoodNodes() {
        return useMultiCopyFoodNodes.get();
    }

    public BooleanProperty useMultiCopyFoodNodesProperty() {
        return useMultiCopyFoodNodes;
    }

    public void setUseMultiCopyFoodNodes(boolean useMultiCopyFoodNodes) {
        this.useMultiCopyFoodNodes.set(useMultiCopyFoodNodes);
    }

    public Collection<String> getSelectedLabels() {
        return getNodeSelection().getSelectedItems().stream().map(v -> getLabel(v).getText()).filter(s -> s.length() > 0).collect(Collectors.toList());
    }

    public int getEmbeddingIterations() {
        return embeddingIterations.get();
    }

    public IntegerProperty embeddingIterationsProperty() {
        return embeddingIterations;
    }

    public void setEmbeddingIterations(int embeddingIterations) {
        this.embeddingIterations.set(embeddingIterations);
    }

    public Type getGraphType() {
        return graphType.get();
    }

    public ObjectProperty<Type> graphTypeProperty() {
        return graphType;
    }

    public void setGraphType(Type graphType) {
        this.graphType.set(graphType);
    }
}
