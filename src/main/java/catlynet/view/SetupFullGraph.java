/*
 * SetupFullGraph.java Copyright (C) 2022 Daniel H. Huson
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
import catlynet.model.ReactionSystem;
import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * setup up the full graph
 * Daniel Huson, 2.2020
 */
public class SetupFullGraph {
    /**
     * setup
     */
    public static void apply(Graph reactionGraph, ReactionSystem reactionSystem, NodeSet foodNodes, final Map<MoleculeType, Node> molecule2node, boolean suppressCatalystEdges, boolean useMultiCopyFoodNodes) {
        for (Reaction reaction : reactionSystem.getReactions()) {
            final Node reactionNode = reactionGraph.newNode(reaction);

            final Set<MoleculeType> molecules = new HashSet<>();
            molecules.addAll(reaction.getReactants());
            molecules.addAll(reaction.getProducts());

            if (!suppressCatalystEdges) {
                molecules.addAll(reaction.getCatalystElements());
                molecules.addAll(reaction.getCatalystConjunctions()); // will have one node for each conjunction
                molecules.addAll(reaction.getInhibitions());
            }

            for (MoleculeType molecule : molecules) {
                if (molecule2node.get(molecule) == null) { // must be a food molecule mentioned in a conjunction
                    final Node v = reactionGraph.newNode(molecule.getName().contains("&") ? new ReactionGraphView.AndNode() : molecule);
                    molecule2node.put(molecule, v);
                    if (reactionSystem.getFoods().contains(molecule))
                        foodNodes.add(v);
                }
            }

            for (MoleculeType molecule : reaction.getReactants()) {
                addNewEdgeIfNotPresent(reactionGraph, getNode(reactionGraph, reactionSystem, molecule, molecule2node, useMultiCopyFoodNodes), reactionNode, reaction.getDirection() == Reaction.Direction.both ? EdgeType.ReactantReversible : EdgeType.Reactant);
            }
            for (MoleculeType molecule : reaction.getProducts()) {
                addNewEdgeIfNotPresent(reactionGraph, reactionNode, getNode(reactionGraph, reactionSystem, molecule, molecule2node, useMultiCopyFoodNodes), reaction.getDirection() == Reaction.Direction.both ? EdgeType.ProductReversible : EdgeType.Product);
            }
            if (!suppressCatalystEdges) {
                for (MoleculeType molecule : reaction.getCatalystConjunctions()) {
                    if (molecule.getName().contains("&")) {
						for (MoleculeType catalyst : MoleculeType.valuesOf(StringUtils.trimAll(StringUtils.split(molecule.getName(), '&')))) {
							final Node andNode = getNode(reactionGraph, reactionSystem, molecule, molecule2node, useMultiCopyFoodNodes);
							if (useMultiCopyFoodNodes) {
								final Optional<Node> node = StreamSupport.stream(andNode.parents().spliterator(), true).filter(v -> v.getInfo().equals(catalyst)).findAny();
								if (node.isPresent())
									continue;
							}
							addNewEdgeIfNotPresent(reactionGraph, getNode(reactionGraph, reactionSystem, catalyst, molecule2node, useMultiCopyFoodNodes), andNode, EdgeType.Catalyst);
						}
                    }
                    addNewEdgeIfNotPresent(reactionGraph, getNode(reactionGraph, reactionSystem, molecule, molecule2node, useMultiCopyFoodNodes), reactionNode, EdgeType.Catalyst);
                }
                for (MoleculeType molecule : reaction.getInhibitions()) {
                    addNewEdgeIfNotPresent(reactionGraph, getNode(reactionGraph, reactionSystem, molecule, molecule2node, useMultiCopyFoodNodes), reactionNode, EdgeType.Inhibitor);
                }
            }
        }
        for (Node v : reactionGraph.nodes()) {
            if (v.getInfo() instanceof ReactionGraphView.AndNode) {
                for (Edge e : v.inEdges()) {
                    if (e.getSource().getDegree() == 1)
                        foodNodes.add(e.getSource());
                }
            } else if (v.getInfo() instanceof MoleculeType) {
                if (reactionSystem.getFoods().contains((MoleculeType) v.getInfo()))
                    foodNodes.add(v);

            }
        }

        if (useMultiCopyFoodNodes) {
            for (var v : reactionGraph.nodeStream().filter(v -> v.getDegree() == 0).collect(Collectors.toList())) {
                reactionGraph.deleteNode(v);
            }
        }
    }

    private static void addNewEdgeIfNotPresent(Graph reactionGraph, Node v, Node w, EdgeType type) {
        if (v.getEdgeTo(w) == null || v.getEdgeTo(w).getInfo() != type)
            reactionGraph.newEdge(v, w, type);
    }

    /**
     * gets the node to use for a given molecule
     *
     * @param multiCopyFoodNodes - if set, creates a new none for every usage of a food node
     * @return node
     */
    private static Node getNode(Graph reactionGraph, ReactionSystem reactionSystem, MoleculeType molecule, Map<MoleculeType, Node> molecule2node, boolean multiCopyFoodNodes) {
        final Node v;
        if (!multiCopyFoodNodes)
            v = molecule2node.get(molecule);
        else {
            if (molecule2node.containsKey(molecule)) {
                v = molecule2node.get(molecule);
                if (reactionSystem.getFoods().contains(molecule))
                    molecule2node.remove(molecule); // remove so that we recreate later
            } else
                v = reactionGraph.newNode(molecule);
        }
        return v;
    }

}
