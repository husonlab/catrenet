/*
 * EdgeView.java Copyright (C) 2020. Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.fx.shapes.CircleShape;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;
import jloda.util.ProgramProperties;

/**
 * edge view
 * Daniel Huson, 2.2020
 */
public class EdgeView extends Group {
    public enum EdgeStyle {Solid, Dashed, Dotted}

    private final EdgeStyle reactionEdgeStyle = EdgeStyle.valueOf(ProgramProperties.get("reactionEdgeStyle", EdgeStyle.Solid.name()));
    private final EdgeStyle catalystEdgeStyle = EdgeStyle.valueOf(ProgramProperties.get("catalystEdgeStyle", EdgeStyle.Dashed.name()));
    private final EdgeStyle inhibitionEdgeStyle = EdgeStyle.valueOf(ProgramProperties.get("inhibitionEdgeStyle", EdgeStyle.Dashed.name()));

    private final Color reactionColor = ProgramProperties.get("reactionColor", Color.BLACK);
    private final Color catalystColor = ProgramProperties.get("catalystColor", Color.BLACK);
    private final Color inhibitionColor = ProgramProperties.get("inhibitionColor", Color.LIGHTGREY);

    private final int reactionEdgeWidth = ProgramProperties.get("reactionEdgeWidth", 2);
    private final int catalystEdgeWidth = ProgramProperties.get("catalystEdgeWidth", 2);
    private final int inhibitionEdgeWidth = ProgramProperties.get("inhibitionEdgeWidth", 2);

    private EdgeView() {
    }

    public EdgeView(ReactionGraphView graphView, Edge e, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY, EdgeType edgeType) {
        final Shape arrowHead;
        switch (edgeType) {
            default:
            case Catalyst: {
                arrowHead = new Polyline(-5, -3, 5, 0, -5, 3);
                break;
            }
            case Dependency:
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

        final InvalidationListener invalidationListener = v -> {
            final Point2D lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(e));

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
            final Point2D lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(e));
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

        final Path path = new Path(moveToA, lineToB, quadCurveToD, lineToE);
        path.setStrokeWidth(2);

        graphView.setupMouseInteraction(path, circleShape, null, e);


        switch (edgeType) {
            case Catalyst: {
                if (getCatalystEdgeStyle() == EdgeStyle.Dashed) {
                    path.getStrokeDashArray().setAll(4.0, 6.0);
                } else if (getCatalystEdgeStyle() == EdgeStyle.Dotted) {
                    path.getStrokeDashArray().setAll(1.0, 5.0);
                }
                path.setStroke(getCatalystColor());
                path.setStrokeWidth(getCatalystEdgeWidth());
                break;
            }
            case Inhibitor: {
                if (getInhibitionEdgeStyle() == EdgeStyle.Dashed) {
                    path.getStrokeDashArray().setAll(4.0, 6.0);
                } else if (getInhibitionEdgeStyle() == EdgeStyle.Dotted) {
                    path.getStrokeDashArray().setAll(1.0, 5.0);
                }
                path.setStroke(getInhibitionColor());
                path.setStrokeWidth(getInhibitionEdgeWidth());

                break;
            }
            default: {
                if (getReactionEdgeStyle() == EdgeStyle.Dashed) {
                    path.getStrokeDashArray().setAll(4.0, 6.0);
                } else if (getReactionEdgeStyle() == EdgeStyle.Dotted) {
                    path.getStrokeDashArray().setAll(1.0, 5.0);
                }
                path.setStroke(getReactionColor());
                path.setStrokeWidth(getReactionEdgeWidth());

                break;
            }
        }

        getChildren().addAll(path, arrowHead, circleShape);
    }

    /**
     * apply the path representing an edge
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
}
