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
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.shapes.CircleShape;
import jloda.fx.shapes.SquareShape;
import jloda.fx.util.AService;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.SelectionEffectBlue;
import jloda.graph.*;
import jloda.util.APoint2D;
import jloda.util.Basic;
import jloda.util.Pair;

import java.util.*;

/**
 * maintains the visualization of a model
 * Daniel Huson, 7.2019
 */
public class ReactionGraphView {
    private final Graph reactionGraph = new Graph();
    private final NodeSet foodNodes = new NodeSet(reactionGraph);
    private final NodeArray<Pair<javafx.scene.Node, javafx.scene.Node>> node2group = new NodeArray<>(reactionGraph);
    private final EdgeArray<Group> edge2group = new EdgeArray<>(reactionGraph);

    private final AMultipleSelectionModel<Node> nodeSelection = new AMultipleSelectionModel<>();
    private final AMultipleSelectionModel<Edge> edgeSelection = new AMultipleSelectionModel<>();

    private final ObjectProperty<Color> inhibitionEdgeColor = new SimpleObjectProperty<>(Color.LIGHTGREY);


    public class AndNode {
    }

    private final ReactionSystem reactionSystem;

    private final Group world;

    private final MoleculeFlowAnimation moleculeFlowAnimation;

    /**
     * construct a graph view for the given system
     *
     * @param reactionSystem
     */
    public ReactionGraphView(ReactionSystem reactionSystem) {
        this.reactionSystem = reactionSystem;
        this.world = new Group();

        nodeSelection.getSelectedItems().addListener((ListChangeListener<Node>) (e) -> {
            while (e.next()) {
                for (Node v : e.getAddedSubList()) {
                    final Pair<javafx.scene.Node, javafx.scene.Node> pair = node2group.get(v);
                    if (pair != null) {
                        for (Object obj : pair) {
                            ((javafx.scene.Node) obj).setEffect(SelectionEffectBlue.getInstance());

                        }
                    }
                }
                for (Node v : e.getRemoved()) {
                    final Pair<javafx.scene.Node, javafx.scene.Node> pair = node2group.get(v);
                    if (pair != null) {
                        for (Object obj : pair) {
                            ((javafx.scene.Node) obj).setEffect(null);
                        }
                    }
                }
            }
        });

        edgeSelection.getSelectedItems().addListener((ListChangeListener<Edge>) (e) -> {
            while (e.next()) {
                for (Edge edge : e.getAddedSubList()) {
                    final Group group = edge2group.get(edge);
                    if (group != null) {
                        for (javafx.scene.Node node : group.getChildren())
                            node.setEffect(SelectionEffectBlue.getInstance());
                    }
                }
                for (Edge edge : e.getRemoved()) {
                    final Group group = edge2group.get(edge);
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
                    for (javafx.scene.Node node : edge2group.get(e).getChildren()) {
                        if (node instanceof Path || node instanceof Polyline)
                            ((Shape) node).setStroke(n);
                    }
                }
            }
        });

        moleculeFlowAnimation = new MoleculeFlowAnimation(reactionGraph, foodNodes, edge2group, world);

        moleculeFlowAnimation.animateInhibitionsProperty().addListener((c, o, n) -> inhibitionEdgeColor.set(n ? Color.BLACK : Color.LIGHTGREY));
    }

    /**
     * update the visualization
     */
    public void update() {
        //System.err.println("Updating graph");
        clear();

        final Map<MoleculeType, Node> molecule2node = new HashMap<>();

        for (Reaction reaction : reactionSystem.getReactions()) {
            final Node reactionNode = reactionGraph.newNode(reaction);

            for (Collection<MoleculeType> collection : Arrays.asList(reaction.getReactants(), reaction.getProducts(), reaction.getCatalysts(), reaction.getInhibitors())) {
                for (MoleculeType molecule : collection) {
                    if (molecule2node.get(molecule) == null) {

                        if (molecule.getName().contains("&")) {
                            molecule2node.put(molecule, reactionGraph.newNode(new AndNode()));

                            for (MoleculeType catalyst : MoleculeType.valueOf(Basic.trimAll(Basic.split(molecule.getName(), '&')))) {
                                if (molecule2node.get(catalyst) == null) {
                                    molecule2node.put(catalyst, reactionGraph.newNode(catalyst));
                                }
                            }
                        } else {
                            final Node v = reactionGraph.newNode(molecule);
                            molecule2node.put(molecule, v);
                            if (reactionSystem.getFoods().contains(molecule))
                                foodNodes.add(v);
                        }
                    }
                }
            }
            for (MoleculeType molecule : reaction.getReactants()) {
                reactionGraph.newEdge(molecule2node.get(molecule), reactionNode, reaction.getDirection() == Reaction.Direction.both ? EdgeType.ReactantReversible : EdgeType.Reactant);
            }
            for (MoleculeType molecule : reaction.getProducts()) {
                reactionGraph.newEdge(reactionNode, molecule2node.get(molecule), reaction.getDirection() == Reaction.Direction.both ? EdgeType.ProductReversible : EdgeType.Product);
            }
            for (MoleculeType molecule : reaction.getCatalysts()) {
                if (molecule.getName().contains("&")) {
                    for (MoleculeType catalyst : MoleculeType.valueOf(Basic.trimAll(Basic.split(molecule.getName(), '&')))) {
                        reactionGraph.newEdge(molecule2node.get(catalyst), molecule2node.get(molecule), EdgeType.Catalyst);
                    }
                }
                reactionGraph.newEdge(molecule2node.get(molecule), reactionNode, EdgeType.Catalyst);
            }
            for (MoleculeType molecule : reaction.getInhibitors()) {
                reactionGraph.newEdge(molecule2node.get(molecule), reactionNode, EdgeType.Inhibitor);
            }
        }
        for (Node v : reactionGraph.nodes()) {
            if (v.getInfo() instanceof AndNode) {
                for (Edge e : v.inEdges()) {
                    if (e.getSource().getDegree() == 1)
                        foodNodes.add(e.getSource());
                }
            }
        }

        nodeSelection.setItems(reactionGraph.getNodesAsSet());
        edgeSelection.setItems(reactionGraph.getEdgesAsSet());

        AService<NodeArray<APoint2D>> service = new AService<>();
        service.setCallable((() -> {
            final FruchtermanReingoldLayout layout = new FruchtermanReingoldLayout(reactionGraph);

            return layout.apply(10000);
        }));

        service.setOnSucceeded((e) -> {
            world.getChildren().addAll(setupGraphView(reactionSystem, reactionGraph, node2group, edge2group, service.getValue()));
        });
        service.start();
    }

    public void clear() {
        foodNodes.clear();
        nodeSelection.clearSelection();
        nodeSelection.setItems();
        edgeSelection.clearSelection();
        edgeSelection.setItems();
        reactionGraph.clear();
        world.getChildren().clear();
    }

    public Group getWorld() {
        return world;
    }

    public Graph getReactionGraph() {
        return reactionGraph;
    }

    public AMultipleSelectionModel<Node> getNodeSelection() {
        return nodeSelection;
    }

    public AMultipleSelectionModel<Edge> getEdgeSelection() {
        return edgeSelection;
    }

    /**
     * set up the graph view
     *
     * @param graph
     * @param coordinates
     * @return graph view
     */
    private Collection<? extends javafx.scene.Node> setupGraphView(ReactionSystem reactionSystem, Graph graph, NodeArray<Pair<javafx.scene.Node, javafx.scene.Node>> node2pair, EdgeArray<Group> edge2group, NodeArray<APoint2D> coordinates) {
        final ArrayList<javafx.scene.Node> all = new ArrayList<>();
        final ArrayList<javafx.scene.Node> labels = new ArrayList<>();

        final Font labelFont = Font.font("Arial", 12);
        final Background labelBackground = new Background(new BackgroundFill(Color.WHITE.deriveColor(1, 1, 1, 0.7), null, null));

        final Map<Node, Shape> node2shape = new HashMap<>();

        for (Node v : graph.nodes()) {
            if (v.getInfo() instanceof Reaction) {
                final Shape shape = new CircleShape(10);
                shape.setStroke(Color.BLACK);
                shape.setFill(Color.WHITE);
                shape.setStrokeWidth(2);
                shape.setTranslateX(coordinates.get(v).getX());
                shape.setTranslateY(coordinates.get(v).getY());
                setupMouseInteraction(shape, shape, v, null);
                node2shape.put(v, shape);

                final Label text = new Label(((Reaction) v.getInfo()).getName());
                text.setBackground(labelBackground);
                text.setFont(labelFont);
                text.setLayoutX(10);
                text.translateXProperty().bind(shape.translateXProperty());
                text.translateYProperty().bind(shape.translateYProperty());
                labels.add(text);
                setupMouseInteraction(text, text, v, null);

                node2pair.put(v, new Pair<>(shape, text));

            } else if (v.getInfo() instanceof MoleculeType) {
                final Shape shape = new SquareShape(10);
                shape.setStroke(Color.BLACK);
                shape.setFill(Color.WHITE);
                if (reactionSystem.getFoods().contains((MoleculeType) v.getInfo()))
                    shape.setStrokeWidth(4);
                else
                    shape.setStrokeWidth(2);
                shape.setTranslateX(coordinates.get(v).getX());
                shape.setTranslateY(coordinates.get(v).getY());
                node2shape.put(v, shape);
                setupMouseInteraction(shape, shape, v, null);


                final Label text = new Label(((MoleculeType) v.getInfo()).getName());
                text.setFont(labelFont);
                text.setBackground(labelBackground);
                text.setLayoutX(10);
                text.translateXProperty().bind(shape.translateXProperty());
                text.translateYProperty().bind(shape.translateYProperty());
                labels.add(text);

                setupMouseInteraction(text, text, v, null);

                node2pair.put(v, new Pair<>(shape, text));

            } else if (v.getInfo() instanceof AndNode) {
                final Shape shape = new CircleShape(15);
                shape.setStroke(Color.WHITE);
                shape.setFill(Color.WHITE);
                shape.setStrokeWidth(2);
                shape.setTranslateX(coordinates.get(v).getX());
                shape.setTranslateY(coordinates.get(v).getY());
                node2shape.put(v, shape);

                final Text text = new Text("&");
                text.setFont(Font.font("Arial", 12));
                text.setLayoutX(10);
                text.translateXProperty().bind(shape.translateXProperty().subtract(15));
                text.translateYProperty().bind(shape.translateYProperty().add(5));
                labels.add(text);

                setupMouseInteraction(text, shape, v, null);

                node2pair.put(v, new Pair<>(shape, text));

            } else
                System.err.println("Unsupported node type: " + v.getInfo());
        }

        for (Edge edge : graph.edges()) {
            final EdgeType edgeType = (EdgeType) edge.getInfo();

            final Shape sourceShape = node2shape.get(edge.getSource());
            final Shape targetShape = node2shape.get(edge.getTarget());

            final Group path = createPath(edge, sourceShape.translateXProperty(), sourceShape.translateYProperty(), targetShape.translateXProperty(), targetShape.translateYProperty(), edgeType);

            all.add(path);
            edge2group.put(edge, path);
        }

        all.addAll(node2shape.values());
        all.addAll(labels);
        return all;
    }

    /**
     * setup mouse interaction
     *
     * @param mouseTarget
     * @param nodeToMove
     */
    private void setupMouseInteraction(javafx.scene.Node mouseTarget, javafx.scene.Node nodeToMove, Node v, Edge e) {
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

            if (nodeToMove.translateXProperty().isBound())
                nodeToMove.setLayoutX(nodeToMove.getLayoutX() + (mouseX - mouseDown[0]));
            else
                nodeToMove.setTranslateX(nodeToMove.getTranslateX() + (mouseX - mouseDown[0]));
            if (nodeToMove.translateYProperty().isBound())
                nodeToMove.setLayoutY(nodeToMove.getLayoutY() + (mouseY - mouseDown[1]));

            else
                nodeToMove.setTranslateY(nodeToMove.getTranslateY() + (mouseY - mouseDown[1]));

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
                    if (v != null)
                        nodeSelection.select(v);
                    if (e != null)
                        edgeSelection.select(e);
                } else {
                    if (v != null) {
                        if (nodeSelection.getSelectedItems().contains(v))
                            nodeSelection.clearSelection(v);
                        else
                            nodeSelection.select(v);
                    }
                    if (e != null) {
                        if (edgeSelection.getSelectedItems().contains(e))
                            edgeSelection.clearSelection(e);
                        else
                            edgeSelection.select(e);
                    }

                }
            }
        });
    }

    /**
     * create a path to represent an edge
     *
     * @param aX
     * @param aY
     * @param bX
     * @param bY
     * @param edgeType
     * @return path
     */
    private Group createPath(Edge edge, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY, EdgeType edgeType) {
        final Shape arrowHead;
        switch (edgeType) {
            default:
            case Catalyst: {
                arrowHead = new Polyline(-5, -3, 5, 0, -5, 3);
                break;
            }
            case Reactant: {
                arrowHead = new Polygon(-5, -3, 5, 0, -5, 3);
                arrowHead.setFill(Color.WHITE);
                break;
            }
            case ReactantReversible: {
                arrowHead = new Polygon(-6, 0, 0, 4, 6, 0, 0, -4);
                arrowHead.setFill(Color.WHITE);
                break;
            }
            case Product: {
                arrowHead = new Polygon(-5, -3, 5, 0, -5, 3);
                arrowHead.setFill(Color.LIGHTGREY);
                break;
            }
            case ProductReversible: {
                arrowHead = new Polygon(-6, 0, 0, 4, 6, 0, 0, -4);
                arrowHead.setFill(Color.LIGHTGREY);
                break;
            }
            case Inhibitor: {
                arrowHead = new Polyline(0, -5, 0, 5);
                break;
            }
        }
        arrowHead.setStroke(Color.BLACK);

        final MoveTo moveToA = new MoveTo();

        final LineTo lineToB = new LineTo();

        final QuadCurveTo quadCurveToD = new QuadCurveTo();

        final LineTo lineToE = new LineTo();

        final CircleShape circleShape = new CircleShape(3);

        final InvalidationListener invalidationListener = (e) -> {
            final Point2D lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge));

            if (lineCenter != null) {
                circleShape.setTranslateX(lineCenter.getX());
                circleShape.setTranslateY(lineCenter.getY());
            }
        };

        aX.addListener(invalidationListener);
        aY.addListener(invalidationListener);
        bX.addListener(invalidationListener);
        bY.addListener(invalidationListener);

        {
            final Point2D lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge));
            circleShape.setTranslateX(lineCenter.getX());
            circleShape.setTranslateY(lineCenter.getY());
            circleShape.translateXProperty().addListener((c, o, n) ->
                    updatePath(aX.get(), aY.get(), bX.get(), bY.get(), new Point2D(circleShape.getTranslateX(), circleShape.getTranslateY()), moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge)));
            circleShape.translateYProperty().addListener((c, o, n) ->
                    updatePath(aX.get(), aY.get(), bX.get(), bY.get(), new Point2D(circleShape.getTranslateX(), circleShape.getTranslateY()), moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge)));
            // setupMouseInteraction(circleShape,circleShape);
            circleShape.setFill(Color.TRANSPARENT);
            circleShape.setStroke(Color.TRANSPARENT);
        }


        final Path path = new Path(moveToA, lineToB, quadCurveToD, lineToE);
        path.setStrokeWidth(2);

        setupMouseInteraction(path, circleShape, null, edge);


        if (edgeType == EdgeType.Catalyst) {
            path.getStrokeDashArray().addAll(2.0, 4.0);
        } else if (edgeType == EdgeType.Inhibitor) {
            path.getStrokeDashArray().addAll(2.0, 4.0);
            path.setStroke(getInhibitionEdgeColor());
        }

        return new Group(path, arrowHead, circleShape);
    }

    /**
     * update the path representing an edge
     *
     * @param ax
     * @param ay
     * @param ex
     * @param ey
     * @param moveToA
     * @param lineToB
     * @param quadCurveToD
     * @param lineToE
     * @param arrowHead
     * @param clockwise
     * @return center point
     */
    private static Point2D updatePath(double ax, double ay, double ex, double ey, Point2D center, MoveTo moveToA, LineTo lineToB, QuadCurveTo quadCurveToD, LineTo lineToE, EdgeType edgeType, Shape arrowHead, boolean clockwise) {
        final double straightSegmentLength = 25;
        final double liftFactor = 0.2;

        final double distance = GeometryUtilsFX.distance(ax, ay, ex, ey);

        moveToA.setX(ax);
        moveToA.setY(ay);

        lineToE.setX(ex);
        lineToE.setY(ey);


        if (distance <= 2 * straightSegmentLength) {
            lineToB.setX(ax);
            lineToB.setY(ay);

            quadCurveToD.setX(ax);
            quadCurveToD.setY(ay);
            quadCurveToD.setControlX(ax);
            quadCurveToD.setControlY(ay);

            arrowHead.setTranslateX(0.5 * (quadCurveToD.getX() + lineToE.getX()));
            arrowHead.setTranslateY(0.5 * (quadCurveToD.getY() + lineToE.getY()));
        } else {
            final double alpha = GeometryUtilsFX.computeAngle(new Point2D(ex - ax, ey - ay));

            final Point2D m = new Point2D(0.5 * (ax + ex), 0.5 * (ay + ey));
            if (center == null) {
                final Point2D c;
                if (!clockwise)
                    center = m.add(-Math.sin(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance, Math.cos(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance);
                else
                    center = m.subtract(-Math.sin(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance, Math.cos(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance);
            }

            final double beta = GeometryUtilsFX.computeAngle(center.subtract(ax, ay));

            final Point2D b = new Point2D(ax + straightSegmentLength * Math.cos(GeometryUtilsFX.deg2rad(beta)), ay + straightSegmentLength * Math.sin(GeometryUtilsFX.deg2rad(beta)));

            lineToB.setX(b.getX());
            lineToB.setY(b.getY());

            final double delta = GeometryUtilsFX.computeAngle(center.subtract(ex, ey));
            final Point2D d = new Point2D(ex + straightSegmentLength * Math.cos(GeometryUtilsFX.deg2rad(delta)), ey + straightSegmentLength * Math.sin(GeometryUtilsFX.deg2rad(delta)));

            quadCurveToD.setX(d.getX());
            quadCurveToD.setY(d.getY());
            quadCurveToD.setControlX(center.getX());
            quadCurveToD.setControlY(center.getY());

            arrowHead.setTranslateX(0.75 * d.getX() + 0.25 * lineToE.getX());
            arrowHead.setTranslateY(0.75 * d.getY() + 0.25 * lineToE.getY());
        }
        if (edgeType == EdgeType.Inhibitor) {
            lineToE.setX(arrowHead.getTranslateX());
            lineToE.setY(arrowHead.getTranslateY());
        }

        final double angle = GeometryUtilsFX.computeAngle(new Point2D(lineToE.getX() - quadCurveToD.getX(), lineToE.getY() - quadCurveToD.getY()));
        arrowHead.setRotationAxis(new Point3D(0, 0, 1));
        arrowHead.setRotate(angle);
        return center;

    }

    /**
     * is this edge the second of two edges that both connect the same two nodes?
     * (If so, will flip its bend)
     *
     * @param edge
     * @return true, if second of two edges
     */
    private static boolean isSecondOfTwoEdges(Edge edge) {
        for (Edge f : edge.getSource().adjacentEdges()) {
            if (f.getTarget() == edge.getTarget() && edge.getId() > f.getId())
                return true;
        }
        return false;
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
    public static Path getPath(Group group) {
        for (javafx.scene.Node child : group.getChildren()) {
            if (child instanceof Path)
                return (Path) child;
        }
        return null;
    }
}
