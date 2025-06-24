/*
 * SetupPrecedenceReactionNetwork.java Copyright (C) 2025 Daniel H. Huson
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
 *
 */

package catrenet.view;

import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.IteratorUtils;
import jloda.util.SetUtils;

import java.util.HashMap;
import java.util.HashSet;

import static catrenet.algorithm.Utilities.computeClosure;

public class SetupPrecedenceReactionNetwork {
	/**
	 * apply
	 */
	public static void apply(Graph reactionGraph, ReactionSystem reactionSystem) {
		reactionGraph.clear();

		final var reactionNodeMap = new HashMap<Reaction, Node>();
		reactionSystem.getReactions().forEach(r -> reactionNodeMap.put(r, reactionGraph.newNode(r)));

		var food = reactionSystem.getFoods();
		var allReactions = reactionSystem.getReactions();

		for (var r1 : reactionSystem.getReactions()) {
			for (var r2 : reactionSystem.getReactions()) {
				var intersection = IteratorUtils.asList(SetUtils.intersection(r1.getProducts(), r2.getReactants()));
				if (!intersection.isEmpty()) {
					var reactions = new HashSet<>(allReactions);
					reactions.remove(r1);
					var closure = computeClosure(food, reactions);
					for (var x : intersection) {
						if (!closure.contains(x)) {
							var v = reactionNodeMap.get(r1);
							var w = reactionNodeMap.get(r2);
							reactionGraph.newEdge(v, w, EdgeType.Association);
							break;
						}
					}
				}
			}
		}
	}
}
