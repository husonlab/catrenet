/*
 * NodeView.java Copyright (C) 2020. Daniel H. Huson
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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.shapes.CircleShape;
import jloda.fx.shapes.SquareShape;
import jloda.graph.Node;

import java.util.Collection;

/**
 * node view
 * Daniel Huson, 2.2020
 */
public class NodeView {
    final private static Background labelBackground = new Background(new BackgroundFill(Color.WHITE.deriveColor(1, 1, 1, 0.7), null, null));

    private final Node v;
    private final Shape shape;
    private final Label text;
    private final Shape spacer;

    /**
     * constructor
     *
     * @param graphView
     * @param food
     * @param v
     * @param x
     * @param y
     */
    public NodeView(ReactionGraphView graphView, Collection<MoleculeType> food, Node v, double x, double y) {
        this.v = v;

        if (v.getInfo() instanceof Reaction) {
            shape = new CircleShape(10);
            shape.setStroke(Color.BLACK);
            shape.setFill(Color.WHITE);
            shape.setStrokeWidth(2);

            text = new Label(((Reaction) v.getInfo()).getName());
            text.setLayoutX(10);
            graphView.setupMouseInteraction(text, text, v, null);
            text.setBackground(labelBackground);
        } else if (v.getInfo() instanceof MoleculeType) {
            shape = new SquareShape(10);
            shape.setStroke(Color.BLACK);
            shape.setFill(Color.WHITE);
            if (food.contains((MoleculeType) v.getInfo()))
                shape.setStrokeWidth(4);
            else
                shape.setStrokeWidth(2);

            text = new Label(((MoleculeType) v.getInfo()).getName());
            text.setLayoutX(10);
            graphView.setupMouseInteraction(text, text, v, null);
            text.setBackground(labelBackground);
        } else if (v.getInfo() instanceof ReactionGraphView.AndNode) {
            shape = new CircleShape(10);
            shape.setStroke(Color.TRANSPARENT);
            shape.setFill(Color.WHITE);
            //shape.setStrokeWidth(1);

            text = new Label("&");
            text.setFont(Font.font("Courier New", 8));
            text.setAlignment(Pos.CENTER);
            text.setLayoutX(-4);
            text.setLayoutY(-8);
            graphView.setupMouseInteraction(text, shape, v, null);

        } else {
            System.err.println("Unsupported node type: " + v.getInfo());
            shape = new CircleShape(10);
            shape.setStroke(Color.BLACK);
            shape.setFill(Color.RED);
            shape.setStrokeWidth(2);

            text = new Label(((Reaction) v.getInfo()).getName());
            text.setLayoutX(10);
            graphView.setupMouseInteraction(text, text, v, null);
            text.setBackground(labelBackground);
        }

        shape.setTranslateX(x);
        shape.setTranslateY(y);
        graphView.setupMouseInteraction(shape, shape, v, null);

        // setupMouseInteraction(text, shape, v, null);
        text.setFont(ReactionGraphView.getFont());
        text.translateXProperty().bind(shape.translateXProperty());
        text.translateYProperty().bind(shape.translateYProperty());

        spacer = new Circle(100);
        spacer.translateXProperty().bind(shape.translateXProperty());
        spacer.translateYProperty().bind(shape.translateYProperty());
        spacer.setFill(Color.TRANSPARENT);
        spacer.setStroke(Color.TRANSPARENT);
        spacer.setMouseTransparent(true);
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

    public Shape getSpacer() {
        return spacer;
    }

    public void translate(double dx, double dy) {
        shape.setTranslateX(shape.getTranslateX() + dx);
        shape.setTranslateY(shape.getTranslateY() + dy);
    }
}
