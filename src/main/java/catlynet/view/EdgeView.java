/*
 * EdgeView.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.fx.shapes.CircleShape;
import jloda.fx.util.BasicFX;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.ProgramProperties;
import jloda.graph.Edge;

/**
 * edge view
 * Daniel Huson, 2.2020
 */
public class EdgeView extends Group {
    public enum EdgeStyle {Solid, Dashed, Dotted}

    private final EdgeStyle reactionEdgeStyle = EdgeStyle.valueOf(ProgramProperties.get("reactionEdgeStyle", EdgeStyle.Solid.name()));
    private final EdgeStyle catalystEdgeStyle = EdgeStyle.valueOf(ProgramProperties.get("catalystEdgeStyle", EdgeStyle.Dashed.name()));
    private final EdgeStyle inhibitionEdgeStyle = EdgeStyle.valueOf(ProgramProperties.get("inhibitionEdgeStyle", EdgeStyle.Dashed.name()));

	private final Color reactionColor = ProgramProperties.get("reactionEdgeColor", Color.BLACK);
	private final Color catalystColor = ProgramProperties.get("catalystEdgeColor", Color.GRAY);
	private final Color inhibitionColor = ProgramProperties.get("inhibitionEdgeColor", Color.LIGHTGREY);

    private final int reactionEdgeWidth = ProgramProperties.get("reactionEdgeWidth", 2);
    private final int catalystEdgeWidth = ProgramProperties.get("catalystEdgeWidth", 2);
    private final int inhibitionEdgeWidth = ProgramProperties.get("inhibitionEdgeWidth", 2);

	private final Path path = new Path();

	private EdgeType edgeType;

	private final MoveTo moveToA = new MoveTo();
	private final LineTo lineToB = new LineTo();
	private final QuadCurveTo quadCurveToD = new QuadCurveTo();
	private final LineTo lineToE = new LineTo();

	private final CircleShape circleShape = new CircleShape(3);

    private EdgeView() {
    }

    public EdgeView(ReactionGraphView graphView, Edge e, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY, EdgeType edgeType) {
		this.edgeType = edgeType;

        var arrowHead = switch (edgeType) {
            case Association, Catalyst, Reactant, Product -> new Polygon(-6, -4, 6, 0, -6, 4);
            case ReactantReversible, ProductReversible -> new Polygon(-7, 0, 0, 5, 7, 0, 0, -5);
            case Inhibitor -> new Polyline(0, -7, 0, 7);
        };


        final InvalidationListener invalidationListener = v -> {
            var lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(e));
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
            var lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(e));
            if (lineCenter != null) {
                circleShape.setTranslateX(lineCenter.getX());
                circleShape.setTranslateY(lineCenter.getY());
                circleShape.translateXProperty().addListener((c, o, n) ->
                        updatePath(aX.get(), aY.get(), bX.get(), bY.get(), new Point2D(circleShape.getTranslateX(), circleShape.getTranslateY()), moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(e)));
                circleShape.translateYProperty().addListener((c, o, n) ->
                        updatePath(aX.get(), aY.get(), bX.get(), bY.get(), new Point2D(circleShape.getTranslateX(), circleShape.getTranslateY()), moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(e)));
                // setupMouseInteraction(circleShape,circleShape);
                circleShape.setFill(Color.TRANSPARENT);
                circleShape.setStroke(Color.TRANSPARENT);
            }
        }

		path.getElements().addAll(moveToA, lineToB, quadCurveToD, lineToE);
        path.setStrokeWidth(2);

        graphView.setupMouseInteraction(path, circleShape, null, e);

        switch (edgeType) {
            case Catalyst -> {
                if (getCatalystEdgeStyle() == EdgeStyle.Dashed) {
                    path.getStrokeDashArray().setAll(4.0, 6.0);
                } else if (getCatalystEdgeStyle() == EdgeStyle.Dotted) {
                    path.getStrokeDashArray().setAll(1.0, 5.0);
                }
                path.setStroke(getCatalystColor());
                arrowHead.setFill(getCatalystColor());
                path.setStrokeWidth(getCatalystEdgeWidth());
            }
            case Inhibitor -> {
                if (getInhibitionEdgeStyle() == EdgeStyle.Dashed) {
                    path.getStrokeDashArray().setAll(4.0, 6.0);
                } else if (getInhibitionEdgeStyle() == EdgeStyle.Dotted) {
                    path.getStrokeDashArray().setAll(1.0, 5.0);
                }
                path.setStroke(getInhibitionColor());
                arrowHead.setFill(getInhibitionColor());
                path.setStrokeWidth(getInhibitionEdgeWidth());

            }
            default -> {
                if (getReactionEdgeStyle() == EdgeStyle.Dashed) {
                    path.getStrokeDashArray().setAll(4.0, 6.0);
                } else if (getReactionEdgeStyle() == EdgeStyle.Dotted) {
                    path.getStrokeDashArray().setAll(1.0, 5.0);
                }
                path.setStroke(getReactionColor());
                arrowHead.setFill(getReactionColor());
                path.setStrokeWidth(getReactionEdgeWidth());
            }
        }
        arrowHead.setStroke(arrowHead.getFill());

        if (path.getStroke() instanceof Color color && color.equals(Color.BLACK)) {
            arrowHead.getStyleClass().add("graph-node"); // yes, graph-node
            path.getStyleClass().add("graph-edge");
        }

        getChildren().addAll(path, arrowHead, circleShape);
    }

    /**
     * update the path representing an edge
     *
     * @return center point
     */
    private static Point2D updatePath(double ax, double ay, double ex, double ey, Point2D center, MoveTo moveToA, LineTo lineToB, QuadCurveTo quadCurveToD, LineTo lineToE, EdgeType edgeType, Shape arrowHead, boolean clockwise) {
        var straightSegmentLength = 25.0;
        var liftFactor = 0.2;

        final var distance = GeometryUtilsFX.distance(ax, ay, ex, ey);

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
            var alpha = GeometryUtilsFX.computeAngle(new Point2D(ex - ax, ey - ay));
            var m = new Point2D(0.5 * (ax + ex), 0.5 * (ay + ey));
            if (center == null) {
                if (!clockwise)
                    center = m.add(-Math.sin(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance, Math.cos(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance);
                else
                    center = m.subtract(-Math.sin(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance, Math.cos(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance);
            }

            var beta = GeometryUtilsFX.computeAngle(center.subtract(ax, ay));

            var b = new Point2D(ax + straightSegmentLength * Math.cos(GeometryUtilsFX.deg2rad(beta)), ay + straightSegmentLength * Math.sin(GeometryUtilsFX.deg2rad(beta)));

            lineToB.setX(b.getX());
            lineToB.setY(b.getY());

            var delta = GeometryUtilsFX.computeAngle(center.subtract(ex, ey));
            var d = new Point2D(ex + straightSegmentLength * Math.cos(GeometryUtilsFX.deg2rad(delta)), ey + straightSegmentLength * Math.sin(GeometryUtilsFX.deg2rad(delta)));

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

        var angle = GeometryUtilsFX.computeAngle(new Point2D(lineToE.getX() - quadCurveToD.getX(), lineToE.getY() - quadCurveToD.getY()));
        arrowHead.setRotationAxis(new Point3D(0, 0, 1));
        arrowHead.setRotate(angle);
        return center;
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}

	public double getControlX() {
		return quadCurveToD.getControlX();
	}

	public double getControlY() {
		return quadCurveToD.getControlY();
	}

	public Path getPath() {
		return path;
    }

    /**
     * is this edge the second of two edges that both connect the same two nodes?
     * (If so, will flip its bend)
     *
     * @return true, if second of two edges
     */
    public static boolean isSecondOfTwoEdges(Edge edge) {
        for (Edge f : edge.getSource().adjacentEdges()) {
            if (f.getTarget() == edge.getTarget() && edge.getId() > f.getId())
                return true;
        }
        return false;
    }

    public EdgeStyle getReactionEdgeStyle() {
        return reactionEdgeStyle;
    }

    public EdgeStyle getCatalystEdgeStyle() {
        return catalystEdgeStyle;
    }

    public EdgeStyle getInhibitionEdgeStyle() {
        return inhibitionEdgeStyle;
    }

    public Color getReactionColor() {
        return reactionColor;
    }

    public Color getCatalystColor() {
        return catalystColor;
    }

    public Color getInhibitionColor() {
        return inhibitionColor;
    }

    public int getReactionEdgeWidth() {
        return reactionEdgeWidth;
    }

    public int getCatalystEdgeWidth() {
        return catalystEdgeWidth;
    }

    public int getInhibitionEdgeWidth() {
        return inhibitionEdgeWidth;
    }

    public static EdgeView createNullEdgeView() {
        return new EdgeView();
    }

    public boolean intersectsInScene(Bounds sceneBounds) {
        for (var part : BasicFX.getAllRecursively(this, Shape.class)) {
            var bounds = part.sceneToLocal(sceneBounds);
            if (part.intersects(bounds))
                return true;
        }
        return false;
    }
}
