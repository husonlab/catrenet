/*
 * NodeView.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.view;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.shapes.CircleShape;
import jloda.fx.shapes.SquareShape;
import jloda.fx.util.ProgramProperties;
import jloda.graph.Node;

import java.util.Collection;

/**
 * node view
 * Daniel Huson, 2.2020
 */
public class NodeView {
    public enum NodeStyle {Square, Circle, BoldSquare, BoldCircle}

    private final Node v;
    private final Shape shape;
    private final Label text;

    private final NodeStyle reactionNodeStyle = NodeStyle.valueOf(ProgramProperties.get("reactionNodeStyle", NodeStyle.Square.name()));
    private final NodeStyle moleculeNodeStyle = NodeStyle.valueOf(ProgramProperties.get("moleculeNodeStyle", NodeStyle.Circle.name()));
    private final NodeStyle foodNodeStyle = NodeStyle.valueOf(ProgramProperties.get("foodNodeStyle", NodeStyle.BoldCircle.name()));
    private final NodeStyle andNodeStyle = NodeStyle.valueOf(ProgramProperties.get("andNodeStyle", NodeStyle.Circle.name()));

    private final Color reactionNodeFillColor = ProgramProperties.get("reactionNodeFillColor", Color.WHITE);
    private final Color moleculeNodeFillColor = ProgramProperties.get("moleculeNodeFillColor", Color.WHITE);
    private final Color foodNodeFillColor = ProgramProperties.get("foodNodeFillColor", Color.WHITE);
    private final Color andNodeFillColor = ProgramProperties.get("andNodeFillColor", Color.WHITE);

    private final int reactionNodeSize = ProgramProperties.get("reactionNodeSize", 10);
    private final int moleculeNodeSize = ProgramProperties.get("moleculeNodeSize", 10);
    private final int foodNodeSize = ProgramProperties.get("foodNodeSize", 10);
    private final int andNodeSize = ProgramProperties.get("andNodeSize", 10);

    private NodeView() {
        v = null;
        shape = null;
        text = null;
    }

    /**
     * constructor
     *
	 */
    public NodeView(ReactionGraphView graphView, Collection<MoleculeType> food, Node v, double x, double y) {
        this.v = v;

        if (v.getInfo() instanceof Reaction) {
            shape = createShape(getReactionNodeShape(), getReactionNodeSize(), getReactionNodeFillColor());
            text = new Label(((Reaction) v.getInfo()).getName());
            text.setLayoutX(getReactionNodeSize() + 2);
            text.setLayoutY(-ReactionGraphView.getFont().getSize() / 2);
            graphView.setupMouseInteraction(text, text, v, null);
           // text.setBackground(labelBackground);
        } else if (v.getInfo() instanceof MoleculeType) {
            if (food.contains((MoleculeType) v.getInfo()))
                shape = createShape(getFoodNodeShape(), getFoodNodeSize(), getFoodNodeFillColor());
            else
                shape = createShape(getMoleculeNodeShape(), getMoleculeNodeSize(), getMoleculeNodeFillColor());
            text = new Label(((MoleculeType) v.getInfo()).getName());
            text.setLayoutX(getFoodNodeSize() + 2);
            text.setLayoutY(-ReactionGraphView.getFont().getSize() / 2);
            graphView.setupMouseInteraction(text, text, v, null);
            // text.setBackground(labelBackground);
        } else if (v.getInfo() instanceof ReactionGraphView.AndNode) {
            shape = createShape(getAndNodeShape(), getAndNodeSize(), getAndNodeFillColor());
            shape.setStrokeWidth(1);

            text = new Label("&");
            text.setFont(Font.font("Courier New", 8));
            text.setAlignment(Pos.CENTER);
            text.setLayoutX(-4);
            text.setLayoutY(-8);
            graphView.setupMouseInteraction(text, shape, v, null);

        } else {
            System.err.println("Unsupported node type: " + v.getInfo());
            shape = new CircleShape(10);
            shape.setFill(Color.RED);
            shape.setStrokeWidth(2);

            text = new Label(((Reaction) v.getInfo()).getName());
            text.setLayoutX(10);
            graphView.setupMouseInteraction(text, text, v, null);
            // text.setBackground(labelBackground);
        }

        shape.setTranslateX(x);
        shape.setTranslateY(y);
        graphView.setupMouseInteraction(shape, shape, v, null);

        // setupMouseInteraction(text, shape, v, null);
        text.setFont(ReactionGraphView.getFont());
        text.getStyleClass().add("above-label");
        text.translateXProperty().bind(shape.translateXProperty());
        text.translateYProperty().bind(shape.translateYProperty());
    }

    public Node getV() {
        return v;
    }

    public Shape getShape() {
        return shape;
    }

    public Label getLabel() {
        return this.text;
    }

    public void translate(double dx, double dy) {
        shape.setTranslateX(shape.getTranslateX() + dx);
        shape.setTranslateY(shape.getTranslateY() + dy);
    }

    private Shape createShape(NodeStyle nodeStyle, int size, Color fillColor) {
        final Shape shape;
        int strokeWidth = 2;
        switch (nodeStyle) {
            case BoldSquare:
                strokeWidth = 4;
            case Square:
                shape = new SquareShape(size);
                break;
            default:
            case BoldCircle:
                strokeWidth = 4;
            case Circle:
                shape = new CircleShape(size);
        }
        if (!fillColor.equals(Color.WHITE))
            shape.getStyleClass().add("graph-node-hollow");

        shape.setStroke(Color.BLACK);
        shape.setFill(fillColor);
        shape.setStrokeWidth(strokeWidth);
        shape.setUserData(nodeStyle);
        return shape;
    }

    public NodeStyle getReactionNodeShape() {
        return reactionNodeStyle;
    }

    public NodeStyle getFoodNodeShape() {
        return foodNodeStyle;
    }

    public NodeStyle getAndNodeShape() {
        return andNodeStyle;
    }

    public int getReactionNodeSize() {
        return reactionNodeSize;
    }

    public int getFoodNodeSize() {
        return foodNodeSize;
    }

    public int getAndNodeSize() {
        return andNodeSize;
    }

    public NodeStyle getMoleculeNodeShape() {
        return moleculeNodeStyle;
    }

    public int getMoleculeNodeSize() {
        return moleculeNodeSize;
    }

    public Color getReactionNodeFillColor() {
        return reactionNodeFillColor;
    }

    public Color getMoleculeNodeFillColor() {
        return moleculeNodeFillColor;
    }

    public Color getFoodNodeFillColor() {
        return foodNodeFillColor;
    }

    public Color getAndNodeFillColor() {
        return andNodeFillColor;
    }

    public static NodeView createNullNodeView() {
        return new NodeView();
    }
}
