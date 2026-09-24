/*
 *  LayeredLayout.java Copyright (C) 2026 Daniel H. Huson
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

import catrenet.algorithm.Stratification;
import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;
import javafx.geometry.Point2D;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeSet;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

/**
 * layered layout of a reaction network. Nodes are placed in layers according to the generation in which they first
 * become available from the food set (catalysts are ignored), with the food set in the top layer. Reactions of
 * generation k are placed below the molecules of generation k-1 and above the molecules that they first produce, so
 * that reactant and product edges point downward. Nodes that are not reachable from the food set are placed below all
 * reachable nodes. Within layers, nodes are ordered and positioned by repeated barycenter sweeps. Edges that point to
 * the same or an earlier layer are feedback edges, in particular, catalysis by molecules that are produced later.
 * Daniel Huson, 9.2026
 */
public class LayeredLayout {
	private static final int numberOfSweeps = 24;
	private static final double minReduction = 0.4; // never reduce the node spacing below this fraction of the requested spacing
	private static final double labelPadding = 16; // space between the labels of adjacent nodes

	/**
	 * apply the layout
	 *
	 * @param graph          the graph to lay out, nodes carry Reaction, MoleculeType or AndNode info
	 * @param reactionSystem the reaction system that provides the generations
	 * @param nodeWidth      the horizontal space needed by a node, including its label
	 * @param layerGap       vertical distance between layers
	 * @param nodeSpacing    horizontal distance between the centers of adjacent nodes in a layer, increased when their
	 *                       labels need more room
	 * @param maxWidth       if the widest layer would be wider than this, the node spacing is reduced, but not below
	 *                       a minimum
	 * @param levels         output: the level of each node, 0 for the top layer
	 * @param result         output: the node coordinates
	 */
	public static void apply(Graph graph, ReactionSystem reactionSystem, ToDoubleFunction<Node> nodeWidth, double layerGap, double nodeSpacing, double maxWidth, NodeArray<Integer> levels, BiConsumer<Node, Point2D> result) {
		if (graph.getNumberOfNodes() == 0)
			return;

		computeLevels(graph, reactionSystem, levels);

		final var numberOfLayers = 1 + graph.nodeStream().mapToInt(levels::get).max().orElse(0);
		final var layers = new ArrayList<List<Node>>();
		for (var i = 0; i < numberOfLayers; i++)
			layers.add(new ArrayList<>());
		for (var v : graph.nodes())
			layers.get(levels.get(v)).add(v);

		final var width = new NodeArray<Double>(graph);
		for (var v : graph.nodes())
			width.put(v, Math.max(1.0, nodeWidth.applyAsDouble(v)));

		// reduce all gaps by a common factor, if the widest layer would be too wide
		var reduction = 1.0;
		{
			var widest = 0.0;
			for (var layer : layers)
				widest = Math.max(widest, packedWidth(layer, width, nodeSpacing, 1.0));
			if (widest > maxWidth)
				reduction = Math.max(minReduction, maxWidth / widest);
		}
		final var spacing = nodeSpacing;
		final var factor = reduction;

		final var x = new NodeArray<Double>(graph);

		// initial order and positions: top layer by name, each further layer by the barycenter of its neighbors above
		for (var i = 0; i < numberOfLayers; i++) {
			var layer = layers.get(i);
			layer.sort(Comparator.comparing(LayeredLayout::getName));
			final Map<Node, Double> desired = new HashMap<>();
			if (i == 0) {
				var pos = 0.0;
				for (var v : layer) {
					desired.put(v, pos);
					pos += spacing;
				}
			} else {
				for (var v : layer)
					desired.put(v, barycenter(v, x, levels, levels.get(v), true));
			}
			placeLayer(layer, desired, width, spacing, factor, x);
		}

		// barycenter sweeps, alternating downward and upward
		for (var sweep = 0; sweep < numberOfSweeps; sweep++) {
			final var down = (sweep % 2 == 0);
			for (var k = 0; k < numberOfLayers; k++) {
				var layer = layers.get(down ? k : numberOfLayers - 1 - k);
				final Map<Node, Double> desired = new HashMap<>();
				for (var v : layer)
					desired.put(v, barycenter(v, x, levels, levels.get(v), false));
				placeLayer(layer, desired, width, spacing, factor, x);
			}
		}

		final var minX = graph.nodeStream().mapToDouble(x::get).min().orElse(0);
		for (var v : graph.nodes())
			result.accept(v, new Point2D(x.get(v) - minX, levels.get(v) * layerGap));
	}

	/**
	 * the mean x-coordinate of the neighbors of a node in other layers, or the node's current x-coordinate, if it has none
	 *
	 * @param aboveOnly only consider neighbors in layers above the node
	 */
	private static double barycenter(Node v, NodeArray<Double> x, NodeArray<Integer> levels, int level, boolean aboveOnly) {
		var sum = 0.0;
		var count = 0;
		for (var e : v.adjacentEdges()) {
			var w = e.getOpposite(v);
			var wLevel = levels.get(w);
			if (w != v && wLevel != level && (!aboveOnly || wLevel < level) && x.get(w) != null) {
				sum += x.get(w);
				count++;
			}
		}
		if (count > 0)
			return sum / count;
		else
			return x.get(v) != null ? x.get(v) : 0.0;
	}

	/**
	 * orders the nodes of a layer by their desired positions and then places them as close as possible to their
	 * desired positions, keeping the required gaps. With the order fixed, the required gaps translate into the
	 * constraint that the values x(v)-offset(v) are non-decreasing along the layer, where offset(v) is the position of v
	 * when all nodes are packed tightly, so the least-squares placement is an isotonic regression, computed by pooling
	 * adjacent violators
	 */
	private static void placeLayer(List<Node> layer, Map<Node, Double> desired, NodeArray<Double> width, double spacing, double reduction, NodeArray<Double> x) {
		layer.sort(Comparator.comparingDouble(desired::get)); // stable, so ties keep their order
		final var n = layer.size();
		final var offset = new double[n];
		for (var i = 1; i < n; i++)
			offset[i] = offset[i - 1] + gap(layer.get(i - 1), layer.get(i), width, spacing, reduction);

		final var blockStart = new int[n];
		final var blockSum = new double[n];
		final var blockCount = new int[n];
		var top = -1;
		for (var i = 0; i < n; i++) {
			top++;
			blockStart[top] = i;
			blockSum[top] = desired.get(layer.get(i)) - offset[i];
			blockCount[top] = 1;
			while (top > 0 && blockSum[top] / blockCount[top] < blockSum[top - 1] / blockCount[top - 1]) {
				blockSum[top - 1] += blockSum[top];
				blockCount[top - 1] += blockCount[top];
				top--;
			}
		}
		for (var b = 0; b <= top; b++) {
			final var shift = blockSum[b] / blockCount[b];
			final var end = (b < top ? blockStart[b + 1] : n);
			for (var i = blockStart[b]; i < end; i++)
				x.put(layer.get(i), shift + offset[i]);
		}
	}

	/**
	 * the distance between the centers of two adjacent nodes in a layer: the node spacing, or more, if their labels
	 * need it, reduced by the given factor
	 */
	private static double gap(Node u, Node v, NodeArray<Double> width, double spacing, double reduction) {
		return reduction * Math.max(spacing, 0.5 * (width.get(u) + width.get(v)) + labelPadding);
	}

	/**
	 * the width of a layer when all its nodes are packed as tightly as allowed
	 */
	private static double packedWidth(List<Node> layer, NodeArray<Double> width, double spacing, double reduction) {
		var total = 0.0;
		for (var i = 1; i < layer.size(); i++)
			total += gap(layer.get(i - 1), layer.get(i), width, spacing, reduction);
		return total;
	}

	/**
	 * computes the level of each node. Internally, reachable reactions of generation k get level 4k-2, molecules first
	 * produced in generation k get level 4k, and food gets level 0, leaving room for conjunction nodes on odd levels
	 * directly below their latest catalyst, or directly above the reaction that they feed, if all their catalysts are
	 * attached to them. Nodes that are not reachable from the food set are placed below all reachable nodes, in the
	 * order in which they depend on each other. A molecule node that is used by exactly one node and produced by none,
	 * such as a copy of a food node, is attached to the node that uses it, that is, placed in the layer directly above
	 * it. Finally, empty levels are removed, so that the reported levels are consecutive.
	 */
	public static void computeLevels(Graph graph, ReactionSystem reactionSystem, NodeArray<Integer> levels) {
		final var reactionLevel = new HashMap<Reaction, Integer>();
		final var moleculeLevel = new HashMap<MoleculeType, Integer>();
		for (var food : reactionSystem.getFoods())
			moleculeLevel.put(food, 0);
		final var molecules = new HashSet<>(reactionSystem.getFoods());
		final var reactions = new HashSet<Reaction>();
		var generation = 0;
		while (true) {
			var pair = Stratification.computeOneStep(molecules, reactions, reactionSystem.getReactions());
			if (pair.getKey().isEmpty())
				break;
			generation++;
			for (var reaction : pair.getKey())
				reactionLevel.put(reaction, 4 * generation - 2);
			for (var molecule : pair.getValue())
				moleculeLevel.putIfAbsent(molecule, 4 * generation);
			reactions.addAll(pair.getKey());
			molecules.addAll(pair.getValue());
		}

		levels.clear();
		final var attachable = new NodeSet(graph); // placed directly above the single node that uses them
		for (var v : graph.nodes()) {
			if (v.getInfo() instanceof MoleculeType && v.getInDegree() == 0 && v.getOutDegree() == 1 && v.getFirstOutEdge().getTarget() != v)
				attachable.add(v);
		}
		final var remaining = new ArrayList<Node>();
		var maxLevel = 0;
		for (var v : graph.nodes()) {
			if (attachable.contains(v))
				continue;
			Integer level = null;
			if (v.getInfo() instanceof Reaction reaction)
				level = reactionLevel.get(reaction);
			else if (v.getInfo() instanceof MoleculeType molecule)
				level = moleculeLevel.get(molecule);
			if (level != null) {
				levels.put(v, level);
				maxLevel = Math.max(maxLevel, level);
			} else
				remaining.add(v);
		}

		final var base = maxLevel;
		while (!remaining.isEmpty()) {
			var progress = false;
			for (var v : new ArrayList<>(remaining)) {
				var level = levelFromInNeighbors(v, levels, attachable, base, true);
				if (level != null) {
					levels.put(v, level);
					remaining.remove(v);
					progress = true;
				}
			}
			if (!progress) { // remaining nodes lie on cycles: place the first one using the neighbors that have levels
				var v = remaining.remove(0);
				levels.put(v, levelFromInNeighbors(v, levels, attachable, base, false));
			}
		}
		for (var v : attachable) {
			var w = v.getFirstOutEdge().getTarget();
			levels.put(v, levels.get(w) - (w.getInfo() instanceof ReactionGraphView.AndNode ? 1 : 2));
		}

		// remove empty levels
		final var used = new TreeSet<Integer>();
		for (var v : graph.nodes())
			used.add(levels.get(v));
		final var rank = new HashMap<Integer, Integer>();
		for (var level : used)
			rank.put(level, rank.size());
		for (var v : graph.nodes())
			levels.put(v, rank.get(levels.get(v)));
	}

	/**
	 * the level of a node, computed from the levels of its in-neighbors, ignoring attachable ones: a conjunction node
	 * is placed directly below its latest in-neighbor, or directly above the node that it feeds, if it has no other
	 * in-neighbors; any other node is placed two levels below its latest in-neighbor and below the base level, which
	 * keeps reactions and molecules on even levels
	 *
	 * @param requireAll if true, returns null when some in-neighbor does not have a level yet
	 */
	private static Integer levelFromInNeighbors(Node v, NodeArray<Integer> levels, NodeSet attachable, int base, boolean requireAll) {
		Integer max = null;
		for (var e : v.inEdges()) {
			var w = e.getSource();
			if (w == v || attachable.contains(w))
				continue;
			var level = levels.get(w);
			if (level == null) {
				if (requireAll)
					return null;
			} else
				max = (max == null ? level : Math.max(max, level));
		}
		if (v.getInfo() instanceof ReactionGraphView.AndNode) {
			if (max != null)
				return max + 1;
			for (var e : v.outEdges()) {
				var level = levels.get(e.getTarget());
				if (level != null)
					return level - 1;
			}
			return base + 1;
		} else
			return (max != null ? Math.max(max, base) : base) + 2;
	}

	private static String getName(Node v) {
		if (v.getInfo() instanceof Reaction reaction)
			return reaction.getName();
		else if (v.getInfo() instanceof MoleculeType molecule)
			return molecule.getName();
		else
			return "";
	}
}
