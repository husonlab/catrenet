/*
 *  ImproveLabelLayout.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.view;

import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * places node labels close to their nodes, but not on them, avoiding edges, other labels and other nodes where possible.
 * For each label, candidate positions around the node are scored by the edges that the label would cross, the labels
 * and nodes that it would overlap, how close its direction is to the directions of the incident edges, and a mild
 * preference for positions to the right of the node. The best candidate is chosen. Labels of nodes with many edges
 * are placed first, as they have the fewest options.
 * Daniel Huson, 3.2023, rewritten 9.2026
 */
public class ImproveLabelLayout {
	private static final double[] candidateAngles = {0, 180, -45, -135, 45, 135, -90, 90, -22.5, -157.5, 22.5, 157.5, -67.5, -112.5, 67.5, 112.5}; // in order of preference, negative is up

	private static final double gap = 3; // between the boundary of a node and its label

	private static final double edgeCrossingCost = 10;
	private static final double labelOverlapCost = 6;
	private static final double nodeOverlapCost = 6;
	private static final double edgeDirectionCost = 4; // for a label that lies in the direction of an incident edge
	private static final double edgeDirectionAngle = 50; // degrees within which an incident edge direction counts
	private static final double preferenceCost = 0.02; // per position in the list of candidates

	/**
	 * a line segment in world coordinates
	 */
	private record Segment(double x1, double y1, double x2, double y2) {
	}

	/**
	 * the drawn shape of an edge: a chain of line segments, with its bounding box
	 */
	private record EdgeShape(Edge edge, List<Segment> segments, Rectangle2D bbox) {
	}

	/**
	 * a placed or not yet placed label: the box that it occupies in world coordinates
	 */
	private static final class LabelBox {
		final Node node;
		Rectangle2D box;

		LabelBox(Node node, Rectangle2D box) {
			this.node = node;
			this.box = box;
		}
	}

	/**
	 * places the labels, once the labels have been given their sizes
	 */
	public static void apply(ReactionGraphView graphView) {
		Platform.runLater(() -> place(graphView));
	}

	/**
	 * places the labels
	 */
	public static void place(ReactionGraphView graphView) {
		final var graph = graphView.getReactionGraph();
		final var node2view = graphView.getNode2view();
		final var edge2view = graphView.getEdge2view();

		// the drawn edges as chains of line segments
		final var edgeShapes = new ArrayList<EdgeShape>();
		final EdgeArray<EdgeShape> edge2shape = new EdgeArray<>(graph);
		for (var e : graph.edges()) {
			var ev = edge2view.get(e);
			if (ev != null) {
				var shape = createEdgeShape(e, ev);
				edgeShapes.add(shape);
				edge2shape.put(e, shape);
			}
		}

		// nodes with labels, and the boxes that the labels currently occupy
		final var labeled = new ArrayList<Node>();
		final NodeArray<LabelBox> node2labelBox = new NodeArray<>(graph);
		final NodeArray<Double> node2radius = new NodeArray<>(graph);
		for (var v : graph.nodes()) {
			var nv = node2view.get(v);
			if (nv == null)
				continue;
			node2radius.put(v, computeRadius(nv.getShape()));
			if (v.getInfo() instanceof Reaction || v.getInfo() instanceof MoleculeType) {
				var label = nv.getLabel();
				label.applyCss();
				label.layout();
				var bounds = label.getLayoutBounds();
				if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
					labeled.add(v);
					node2labelBox.put(v, new LabelBox(v, new Rectangle2D(nv.getShape().getTranslateX() + label.getLayoutX() + bounds.getMinX(),
							nv.getShape().getTranslateY() + label.getLayoutY() + bounds.getMinY(), bounds.getWidth(), bounds.getHeight())));
				}
			}
		}
		final var labelBoxes = new ArrayList<LabelBox>();
		for (var v : labeled)
			labelBoxes.add(node2labelBox.get(v));

		// nodes with many edges have the fewest options, so they are placed first
		labeled.sort(Comparator.comparingInt(Node::getDegree).reversed().thenComparingInt(Node::getId));

		for (var v : labeled) {
			final var nv = node2view.get(v);
			final var shape = nv.getShape();
			final var label = nv.getLabel();
			final var cx = shape.getTranslateX();
			final var cy = shape.getTranslateY();
			final var radius = node2radius.get(v) + gap;
			final var bounds = label.getLayoutBounds();
			final var w = bounds.getWidth();
			final var h = bounds.getHeight();

			// everything that could be in the way of a label of this node
			final var reach = radius + w + h;
			final var search = new Rectangle2D(cx - reach, cy - reach, 2 * reach, 2 * reach);
			final var nearbyEdges = new ArrayList<EdgeShape>();
			for (var edgeShape : edgeShapes) {
				if (edgeShape.bbox().intersects(search))
					nearbyEdges.add(edgeShape);
			}
			final var nearbyLabels = new ArrayList<LabelBox>();
			for (var labelBox : labelBoxes) {
				if (labelBox.node != v && labelBox.box.intersects(search))
					nearbyLabels.add(labelBox);
			}
			final var nearbyNodes = new ArrayList<Node>();
			for (var w1 : graph.nodes()) {
				if (w1 != v && node2view.get(w1) != null) {
					var otherShape = node2view.get(w1).getShape();
					var r = node2radius.get(w1);
					if (new Rectangle2D(otherShape.getTranslateX() - r, otherShape.getTranslateY() - r, 2 * r, 2 * r).intersects(search))
						nearbyNodes.add(w1);
				}
			}
			// the directions in which the incident edges leave the node
			final var edgeAngles = new ArrayList<Double>();
			for (var e : v.adjacentEdges()) {
				var edgeShape = edge2shape.get(e);
				if (edgeShape != null && !edgeShape.segments().isEmpty()) {
					var segments = edgeShape.segments();
					final double px, py;
					if (e.getSource() == v) { // the point where the first straight part ends
						px = segments.get(0).x2();
						py = segments.get(0).y2();
					} else {
						var last = segments.get(segments.size() - 1);
						px = last.x1();
						py = last.y1();
					}
					if (px != cx || py != cy)
						edgeAngles.add(GeometryUtilsFX.computeAngle(px - cx, py - cy));
				}
			}

			// score the candidates and keep the best
			var bestCost = Double.MAX_VALUE;
			double bestX = 0;
			double bestY = 0;
			for (var i = 0; i < candidateAngles.length; i++) {
				final var angle = candidateAngles[i];
				final var box = candidateBox(cx, cy, radius, w, h, angle);
				var cost = preferenceCost * i;
				for (var edgeShape : nearbyEdges) {
					if (edgeShape.bbox().intersects(box) && crosses(edgeShape, box)) {
						cost += edgeCrossingCost;
						if (cost >= bestCost)
							break;
					}
				}
				if (cost < bestCost) {
					for (var labelBox : nearbyLabels) {
						if (labelBox.box.intersects(box))
							cost += labelOverlapCost;
					}
				}
				if (cost < bestCost) {
					for (var w1 : nearbyNodes) {
						var otherShape = node2view.get(w1).getShape();
						if (circleIntersectsRect(otherShape.getTranslateX(), otherShape.getTranslateY(), node2radius.get(w1) + 1, box))
							cost += nodeOverlapCost;
					}
				}
				if (cost < bestCost) {
					for (var edgeAngle : edgeAngles) {
						var difference = Math.abs(angleDifference(angle, edgeAngle));
						if (difference < edgeDirectionAngle)
							cost += edgeDirectionCost * (1 - difference / edgeDirectionAngle);
					}
				}
				if (cost < bestCost) {
					bestCost = cost;
					bestX = box.getMinX();
					bestY = box.getMinY();
				}
			}
			label.setLayoutX(bestX - cx - bounds.getMinX());
			label.setLayoutY(bestY - cy - bounds.getMinY());
			node2labelBox.get(v).box = new Rectangle2D(bestX, bestY, w, h);
		}
	}

	/**
	 * the box that a label of the given size occupies when placed next to a node in the given direction: the box
	 * touches the circle of the given radius around the node center at the point in that direction
	 *
	 * @param angle direction in degrees, 0 is to the right, negative is up
	 */
	private static Rectangle2D candidateBox(double cx, double cy, double radius, double w, double h, double angle) {
		final var cos = Math.cos(Math.toRadians(angle));
		final var sin = Math.sin(Math.toRadians(angle));
		final var hw = 0.5 * w;
		final var hh = 0.5 * h;
		// the distance from the touching point to the center of the box, so that the touching point lies on the box boundary
		final var m = Math.min(Math.abs(cos) > 1e-8 ? hw / Math.abs(cos) : Double.MAX_VALUE, Math.abs(sin) > 1e-8 ? hh / Math.abs(sin) : Double.MAX_VALUE);
		final var centerX = cx + (radius + m) * cos;
		final var centerY = cy + (radius + m) * sin;
		return new Rectangle2D(centerX - hw, centerY - hh, w, h);
	}

	/**
	 * the drawn shape of an edge as a chain of line segments: its straight parts, and its curved part flattened
	 */
	private static EdgeShape createEdgeShape(Edge e, EdgeView ev) {
		final var points = new ArrayList<double[]>();
		for (var element : ev.getPath().getElements()) {
			if (element instanceof MoveTo moveTo)
				points.add(new double[]{moveTo.getX(), moveTo.getY()});
			else if (element instanceof LineTo lineTo)
				points.add(new double[]{lineTo.getX(), lineTo.getY()});
			else if (element instanceof QuadCurveTo quad && !points.isEmpty()) {
				var start = points.get(points.size() - 1);
				final var steps = 8;
				for (var i = 1; i <= steps; i++) {
					var t = (double) i / steps;
					var s = 1 - t;
					points.add(new double[]{s * s * start[0] + 2 * s * t * quad.getControlX() + t * t * quad.getX(),
							s * s * start[1] + 2 * s * t * quad.getControlY() + t * t * quad.getY()});
				}
			}
		}
		final var segments = new ArrayList<Segment>();
		var minX = Double.MAX_VALUE;
		var minY = Double.MAX_VALUE;
		var maxX = -Double.MAX_VALUE;
		var maxY = -Double.MAX_VALUE;
		for (var i = 0; i < points.size(); i++) {
			var p = points.get(i);
			minX = Math.min(minX, p[0]);
			minY = Math.min(minY, p[1]);
			maxX = Math.max(maxX, p[0]);
			maxY = Math.max(maxY, p[1]);
			if (i > 0) {
				var q = points.get(i - 1);
				if (q[0] != p[0] || q[1] != p[1])
					segments.add(new Segment(q[0], q[1], p[0], p[1]));
			}
		}
		if (points.isEmpty())
			return new EdgeShape(e, segments, new Rectangle2D(0, 0, 0, 0));
		return new EdgeShape(e, segments, new Rectangle2D(minX, minY, maxX - minX, maxY - minY));
	}

	private static boolean crosses(EdgeShape edgeShape, Rectangle2D box) {
		for (var segment : edgeShape.segments()) {
			if (segmentIntersectsRect(segment, box))
				return true;
		}
		return false;
	}

	/**
	 * does the segment have a point inside the rectangle? (Liang-Barsky clipping)
	 */
	private static boolean segmentIntersectsRect(Segment s, Rectangle2D r) {
		final var dx = s.x2() - s.x1();
		final var dy = s.y2() - s.y1();
		final double[] p = {-dx, dx, -dy, dy};
		final double[] q = {s.x1() - r.getMinX(), r.getMaxX() - s.x1(), s.y1() - r.getMinY(), r.getMaxY() - s.y1()};
		var t0 = 0.0;
		var t1 = 1.0;
		for (var i = 0; i < 4; i++) {
			if (p[i] == 0) {
				if (q[i] < 0)
					return false;
			} else {
				var t = q[i] / p[i];
				if (p[i] < 0)
					t0 = Math.max(t0, t);
				else
					t1 = Math.min(t1, t);
			}
		}
		return t0 <= t1;
	}

	private static boolean circleIntersectsRect(double cx, double cy, double radius, Rectangle2D r) {
		final var nearestX = Math.max(r.getMinX(), Math.min(cx, r.getMaxX()));
		final var nearestY = Math.max(r.getMinY(), Math.min(cy, r.getMaxY()));
		final var dx = cx - nearestX;
		final var dy = cy - nearestY;
		return dx * dx + dy * dy <= radius * radius;
	}

	/**
	 * the difference between two angles in degrees, in the range -180 to 180
	 */
	private static double angleDifference(double a, double b) {
		var difference = (a - b) % 360;
		if (difference > 180)
			difference -= 360;
		else if (difference < -180)
			difference += 360;
		return difference;
	}

	public static double computeRadius(javafx.scene.Node node) {
		var bounds = node.getLayoutBounds();
		return 0.5 * Math.sqrt(bounds.getWidth() * bounds.getWidth() + bounds.getHeight() * bounds.getHeight());
	}
}
