/*
 * Visualization.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.model.Model;
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import jloda.fx.shapes.CircleShape;
import jloda.fx.shapes.SquareShape;
import jloda.fx.util.AService;
import jloda.graph.*;
import jloda.util.APoint2D;

import java.util.*;

/**
 * maintains the visualization of a model
 * Daniel Huson, 7.2019
 */
public class Visualization {
    public enum EdgeType {Reactant, Product, Catalyst, Inhibitor}

    private final Model model;

    private final Group world;

    public Visualization(Model model) {
        this.model = model;
        this.world = new Group();
    }

    /**
     * update the visualization
     */
    public void update() {
        world.getChildren().clear();

        final Graph graph = new Graph();

        final Map<MoleculeType, Node> molecule2node = new HashMap<>();
        final Map<Reaction, Node> reaction2node = new HashMap<>();

        for (Reaction reaction : model.getReactions()) {
            final Node reactionNode = graph.newNode(reaction);
            reaction2node.put(reaction, reactionNode);

            for (Collection<MoleculeType> collection : Arrays.asList(reaction.getReactants(), reaction.getProducts(), reaction.getCatalysts())) {
                for (MoleculeType molecule : collection) {
                    if (molecule2node.get(molecule) == null)
                        molecule2node.put(molecule, graph.newNode(molecule));
                }
            }
            for (MoleculeType molecule : reaction.getReactants()) {
                graph.newEdge(molecule2node.get(molecule), reactionNode, EdgeType.Reactant);
            }
            for (MoleculeType molecule : reaction.getProducts()) {
                graph.newEdge(reactionNode, molecule2node.get(molecule), EdgeType.Product);
            }
            for (MoleculeType molecule : reaction.getCatalysts()) {
                graph.newEdge(molecule2node.get(molecule), reactionNode, EdgeType.Catalyst);
            }
            for (MoleculeType molecule : reaction.getInhibitors()) {
                graph.newEdge(molecule2node.get(molecule), reactionNode, EdgeType.Inhibitor);
            }
        }

        AService<NodeArray<APoint2D>> service = new AService<>();
        service.setCallable((() -> {
            final FruchtermanReingoldLayout layout = new FruchtermanReingoldLayout(graph);

            return layout.apply(100);
        }));

        service.setOnSucceeded((e) -> {
            world.getChildren().addAll(setupGraphView(graph, service.getValue()));
        });
        service.start();

    }

    private Collection<? extends javafx.scene.Node> setupGraphView(Graph graph, NodeArray<APoint2D> coordinates) {
        final ArrayList<javafx.scene.Node> all = new ArrayList<>();
        final ArrayList<javafx.scene.Node> labels = new ArrayList<>();

        final Map<Node, Shape> node2shape = new HashMap<>();


        for (Node node : graph.nodes()) {
            if (node.getInfo() instanceof Reaction) {
                final Shape shape = new CircleShape(10);
                shape.setStroke(Color.BLACK);
                shape.setFill(Color.WHITE);
                shape.setTranslateX(coordinates.get(node).getX());
                shape.setTranslateY(coordinates.get(node).getY());
                setupMouseInteraction(shape);
                node2shape.put(node, shape);

                final Text text = new Text(((Reaction) node.getInfo()).getName());
                text.setFont(Font.font("Arial", 12));
                text.setLayoutX(10);
                text.translateXProperty().bind(shape.translateXProperty());
                text.translateYProperty().bind(shape.translateYProperty());
                labels.add(text);
                setupMouseInteraction(text);

            } else if (node.getInfo() instanceof MoleculeType) {
                final Shape shape = new SquareShape(10);
                shape.setStroke(Color.BLACK);
                shape.setFill(Color.WHITE);
                shape.setTranslateX(coordinates.get(node).getX());
                shape.setTranslateY(coordinates.get(node).getY());
                node2shape.put(node, shape);
                setupMouseInteraction(shape);

                final Text text = new Text(((MoleculeType) node.getInfo()).getName());
                text.setFont(Font.font("Arial", 12));
                text.setLayoutX(10);
                text.translateXProperty().bind(shape.translateXProperty());
                text.translateYProperty().bind(shape.translateYProperty());
                labels.add(text);

                setupMouseInteraction(text);

            } else
                System.err.println("Unsupported node type: " + node.getInfo());
        }

        for (Edge edge : graph.edges()) {
            final EdgeType edgeType = (EdgeType) edge.getInfo();

            final Shape sourceShape = node2shape.get(edge.getSource());
            final Shape targetShape = node2shape.get(edge.getTarget());

            final Line line = new Line();
            line.startXProperty().bind(sourceShape.translateXProperty());
            line.startYProperty().bind(sourceShape.translateYProperty());

            line.endXProperty().bind(targetShape.translateXProperty());
            line.endYProperty().bind(targetShape.translateYProperty());

            if (edgeType == EdgeType.Catalyst) {
                line.getStrokeDashArray().addAll(2.0, 4.0);
            }

            all.add(new Arrow(line));

        }
        all.addAll(node2shape.values());
        all.addAll(labels);
        return all;
    }

    public Model getModel() {
        return model;
    }

    public Group getWorld() {
        return world;
    }

    public void setupMouseInteraction(javafx.scene.Node node) {
        final double[] mouseDown = new double[2];

        node.setOnMousePressed((e) -> {
            mouseDown[0] = e.getSceneX();
            mouseDown[1] = e.getSceneY();
        });

        node.setOnMouseDragged((e) -> {
            final double mouseX = e.getSceneX();
            final double mouseY = e.getSceneY();

            if (node.translateXProperty().isBound())
                node.setLayoutX(node.getLayoutX() + (mouseX - mouseDown[0]));
            else
                node.setTranslateX(node.getTranslateX() + (mouseX - mouseDown[0]));
            if (node.translateYProperty().isBound())
                node.setLayoutY(node.getLayoutY() + (mouseY - mouseDown[1]));

            else
                node.setTranslateY(node.getTranslateY() + (mouseY - mouseDown[1]));

            mouseDown[0] = e.getSceneX();
            mouseDown[1] = e.getSceneY();
        });
    }

    public class Arrow extends Group {
        final private Line part1 = new Line();
        final private Line part2 = new Line();
        final private Line line;

        public Arrow(Line line) {
            this.line = line;

            getChildren().add(part1);
            getChildren().add(part2);
            getChildren().add(line);

            line.startXProperty().addListener((e) -> update());
            line.startYProperty().addListener((e) -> update());
            line.endXProperty().addListener((e) -> update());
            line.endYProperty().addListener((e) -> update());

            update();
        }

        private void update() {
            Point2D start = new Point2D(line.getStartX(), line.getStartY());
            Point2D end = new Point2D(line.getEndX(), line.getEndY());
            double radian = computeAngle(end.subtract(start));

            (new Point2D(line.getStartX(), line.getStartY())).angle(line.getEndX(), line.getEndY());

            double dx = 6 * Math.cos(radian);
            double dy = 6 * Math.sin(radian);

            Point2D mid = start.midpoint(end);
            Point2D head = mid.add(dx, dy);
            Point2D one = mid.add(-dy, dx);
            Point2D two = mid.add(dy, -dx);

            part1.setStartX(one.getX());
            part1.setStartY(one.getY());
            part1.setEndX(head.getX());
            part1.setEndY(head.getY());

            part2.setStartX(two.getX());
            part2.setStartY(two.getY());
            part2.setEndX(head.getX());
            part2.setEndY(head.getY());
        }
    }

    /**
     * compute angle of vector in radian
     *
     * @param p
     * @return angle of vector in radian
     */
    public static double computeAngle(Point2D p) {
        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return a;
                else
                    return 2.0 * Math.PI - a;
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return Math.PI - a;
                else
                    return Math.PI + a;
            }
        } else if (p.getY() > 0)
            return 0.5 * Math.PI;
        else // p.y<0
            return -0.5 * Math.PI;
    }

}
