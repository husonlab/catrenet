/*
 * Stratification.java Copyright (C) 2026 Daniel H. Huson
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

package catrenet.algorithm;

import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;
import catrenet.view.EdgeType;
import javafx.util.Pair;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.StringUtils;

import java.util.*;

public class Stratification {

	public static String report(ReactionSystem reactionSystem) {
		var buf = new StringBuilder();
		buf.append("\nStratification of reactions and molecules:\n");
		buf.append("Rank 0 molecules (food set):\n");
		var molecules = new HashSet<>(reactionSystem.getFoods());
		for (var moleculeType : molecules) {
			if (!moleculeType.getName().equals("$"))
				buf.append("\t").append(moleculeType.getName()).append("\n");
		}
		buf.append("\n");

		var reactions = new HashSet<Reaction>();

		var i = 0;
		while (true) {
			i++;
			var pair = computeOneStep(molecules, reactions, reactionSystem.getReactions());
			var nextReactions = pair.getKey();
			var nextMolecules = pair.getValue();

			if (nextReactions.isEmpty())
				break;

			buf.append("Rank ").append(i).append(" reactions:\n");
			for (var reaction : nextReactions) {
				buf.append("\t").append(reaction.getName()).append("\n");
			}
			reactions.addAll(nextReactions);
			buf.append("Rank ").append(i).append(" molecules:\n");
			for (var moleculeType : nextMolecules) {
				buf.append("\t").append(moleculeType.getName()).append("\n");
			}
			molecules.addAll(nextMolecules);
			buf.append("\n");
		}

		var reactants = new TreeSet<MoleculeType>();
		var products = new TreeSet<MoleculeType>();
		for (var reaction : reactions) {
			reactants.addAll(reaction.getReactants());
			products.addAll(reaction.getProducts());
		}

		buf.append("\nStratification contains %d reactions, %d reactants and %d products\n".formatted(reactions.size(), reactants.size(), products.size()));

		buf.append("\nUnused food items: %d\n".formatted(reactionSystem.getFoods().stream().filter(f -> !f.getName().equals("$") && !reactants.contains(f)).count()));
		for (var item : reactionSystem.getFoods()) {
			if (!item.getName().equals("$") && !reactants.contains(item)) {
				buf.append("\t").append(item.getName()).append("\n");
			}
		}

		var foodAndProducts = new HashSet<MoleculeType>();
		foodAndProducts.addAll(reactionSystem.getFoods());
		foodAndProducts.addAll(products);

		var countUnmentionedReactions = reactionSystem.getReactions().stream().filter(r -> !reactions.contains(r)).count();

		buf.append("\nUnmentioned reactions: %d\n".formatted(countUnmentionedReactions));
		if (countUnmentionedReactions > 0) {
			for (var reaction : reactionSystem.getReactions()) {
				if (!reactions.contains(reaction)) {
					buf.append("\t").append(reaction.getName());

					var missingReactants = new TreeSet<>(reaction.getReactants());
					missingReactants.removeAll(foodAndProducts);

					var missingProducts = new TreeSet<>(reaction.getProducts());
					missingProducts.removeAll(foodAndProducts);

					if (!missingReactants.isEmpty() || !missingProducts.isEmpty()) {
						buf.append("\t(");
						if (!missingReactants.isEmpty()) {
							buf.append("missing reactants: %s, ".formatted(StringUtils.toString(missingReactants, ", ")));
						}

						if (!missingProducts.isEmpty()) {
							buf.append(" missing products: %s".formatted(StringUtils.toString(missingProducts, ", ")));
						}
						buf.append(")");
					}
					buf.append("\n");
				}
			}
		}
		return buf.toString();
	}

	public enum StratificationDetails {ReactionsOnly, ReactionsRequiredMolecules, ReactionsAllMolecules}

	/**
	 * sets up the stratification graph. Reactions are added stratum by stratum, in the order in which they become
	 * available from the food set (catalysts are ignored). A reaction is treated as running in the direction in which
	 * it became available: forward, if all its reactants are available (this includes two-way reactions that are
	 * available in both directions), otherwise in reverse, in which case its products are its inputs and its reactants
	 * are its outputs.
	 *
	 * @param graph          the graph to set up
	 * @param reactionSystem the reaction system
	 * @param details        ReactionsOnly: reactions as nodes, with an edge r->s if r is the latest earlier reaction
	 *                       that provides a non-food input of s;
	 *                       ReactionsRequiredMolecules: edges r->m->s via molecule nodes, dropping output nodes that
	 *                       are not used by any later reaction;
	 *                       ReactionsAllMolecules: as before, but keeping all output nodes
	 */
	public static void setupStratificationGraph(Graph graph, ReactionSystem reactionSystem, StratificationDetails details) {
		var molecules = new HashSet<>(reactionSystem.getFoods());
		var reactions = new ArrayList<Reaction>(); // reactions of earlier strata, in the order in which they were added
		var reactionNodeMap = new HashMap<Reaction, Node>();
		var reactionOutputs = new HashMap<Reaction, Set<MoleculeType>>(); // what each reaction provides, in the direction in which it runs

		var latestMoleculeNode = new HashMap<MoleculeType, Node>(); // the most recently created node for each molecule

		if (details != StratificationDetails.ReactionsOnly) {
			for (var food : reactionSystem.getFoods()) {
				latestMoleculeNode.put(food, graph.newNode(food));
			}
		}
		System.err.println("Computing Reaction Stratification Graph (" + details.name() + ")...");
		while (true) {
			var pair = computeOneStep(molecules, reactions, reactionSystem.getReactions());
			var nextReactions = pair.getKey();
			var nextMolecules = pair.getValue();

			if (nextReactions.isEmpty())
				break;

			for (var reaction : nextReactions) {
				var v = graph.newNode(reaction);
				reactionNodeMap.put(reaction, v);

				var runsForward = (reaction.getDirection() == Reaction.Direction.forward || reaction.getDirection() == Reaction.Direction.both)
								  && molecules.containsAll(reaction.getReactants());
				var inputs = (runsForward ? reaction.getReactants() : reaction.getProducts());
				var outputs = (runsForward ? reaction.getProducts() : reaction.getReactants());
				reactionOutputs.put(reaction, outputs);

				var remainingInputs = new HashSet<>(inputs);
				if (details == StratificationDetails.ReactionsOnly) {
					remainingInputs.removeAll(reactionSystem.getFoods());
					// link to the latest earlier reaction that provides each input. A single pass suffices: every available
					// non-food molecule is an output of some reaction of an earlier stratum
					for (var j = reactions.size() - 1; j >= 0 && !remainingInputs.isEmpty(); j--) {
						var other = reactions.get(j);
						var provided = new HashSet<>(reactionOutputs.get(other));
						provided.retainAll(remainingInputs);
						if (!provided.isEmpty()) {
							graph.newEdge(reactionNodeMap.get(other), v, EdgeType.Association);
							remainingInputs.removeAll(provided);
						}
					}
				} else {
					// link from the latest node of each input molecule (a food node, or an output node of an earlier reaction)
					for (var molecule : inputs) {
						var w = latestMoleculeNode.get(molecule);
						if (w != null) {
							graph.newEdge(w, v, EdgeType.Association);
							remainingInputs.remove(molecule);
						}
					}
				}
				if (!remainingInputs.isEmpty()) // should not happen, as all inputs of the reaction are available
					System.err.printf("Stratification: no earlier reaction provides these inputs of reaction %s: %s%n", reaction.getName(), StringUtils.toString(remainingInputs, ", "));
			}

			if (details != StratificationDetails.ReactionsOnly) {
				// output nodes are created only after all reactions of the stratum have been linked to their inputs
				for (var reaction : nextReactions) {
					var v = reactionNodeMap.get(reaction);
					for (var molecule : reactionOutputs.get(reaction)) {
						var w = graph.newNode(molecule);
						latestMoleculeNode.put(molecule, w);
						graph.newEdge(v, w, EdgeType.Association);
					}
				}
			}
			reactions.addAll(nextReactions);
			molecules.addAll(nextMolecules);
		}

		if (details == StratificationDetails.ReactionsRequiredMolecules) {
			var toDelete = graph.nodeStream().filter(v -> v.getInfo() instanceof MoleculeType && v.getInDegree() > 0 && v.getOutDegree() == 0).toList();
			for (var v : toDelete) {
				graph.deleteNode(v);
			}
		}

		System.err.printf("Nodes: %d, Edges: %d%n", graph.getNumberOfNodes(), graph.getNumberOfEdges());
	}

	public static Pair<Set<Reaction>, Set<MoleculeType>> computeOneStep(Collection<MoleculeType> molecules, Collection<Reaction> reactions, Collection<Reaction> inputReactions) {
		var nextReactions = new TreeSet<Reaction>();
		var nextMolecules = new TreeSet<MoleculeType>();

		for (var reaction : inputReactions) {
			if (!reactions.contains(reaction)) {
				if ((reaction.getDirection() == Reaction.Direction.forward || reaction.getDirection() == Reaction.Direction.both) && molecules.containsAll(reaction.getReactants())) {
					nextReactions.add(reaction);
					for (var molecule : reaction.getProducts()) {
						if (!molecules.contains(molecule)) {
							nextMolecules.add(molecule);
						}
					}
				}
				if ((reaction.getDirection() == Reaction.Direction.reverse || reaction.getDirection() == Reaction.Direction.both) && molecules.containsAll(reaction.getProducts())) {
					nextReactions.add(reaction);
					for (var molecule : reaction.getReactants()) {
						if (!molecules.contains(molecule)) {
							nextMolecules.add(molecule);
						}
					}
				}
			}
		}
		return new Pair<>(nextReactions, nextMolecules);
	}
}
