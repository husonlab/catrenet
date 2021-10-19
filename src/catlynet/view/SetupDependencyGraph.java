/*
 * SetupFullGraph.java Copyright (C) 2020. Daniel H. Huson
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
import catlynet.model.ReactionSystem;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.Basic;
import jloda.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * setup up the  dependency graph
 * Daniel HUson, 2.2020
 */
public class SetupDependencyGraph {
    /**
     * apply
     *
     * @param reactionGraph
     * @param reactionSystem
     * @param useCatalysts
     */
    public static void apply(Graph reactionGraph, ReactionSystem reactionSystem, boolean useCatalysts) {
        final Map<Reaction, Node> reactionNodeMap = new HashMap<>();

        reactionSystem.getReactions().forEach(r -> reactionNodeMap.put(r, reactionGraph.newNode(r)));

        reactionSystem.getReactions().forEach(r1 -> {
            final Node v = reactionNodeMap.get(r1);

            reactionSystem.getReactions().stream().filter(r2 -> r2 != r1).forEach(r2 -> {
                final Node w = reactionNodeMap.get(r2);

                for (int z = 0; z <= 1; z++) { // try forward, then reverse
                    final Set<MoleculeType> nonFoodProducts = new HashSet<>();
                    if (z == 0) {
                        if (r1.getDirection() == Reaction.Direction.forward || r1.getDirection() == Reaction.Direction.both) {
                            nonFoodProducts.addAll(r1.getProducts());
                            nonFoodProducts.removeAll(reactionSystem.getFoods());
                        } else
                            continue;
                    } else // z==1
                    {
                        if (r1.getDirection() == Reaction.Direction.reverse || r1.getDirection() == Reaction.Direction.both) {
                            nonFoodProducts.addAll(r1.getReactants());
                            nonFoodProducts.removeAll(reactionSystem.getFoods());
                        } else
                            continue;
                    }

                    if (nonFoodProducts.size() > 0) {
                        final Set<MoleculeType> catalysts = new HashSet<>();
						r2.getCatalystConjunctions().forEach(c -> catalysts.addAll(MoleculeType.valuesOf(StringUtils.split(c.getName(), '&'))));

                        if ((r2.getDirection() == Reaction.Direction.forward || r2.getDirection() == Reaction.Direction.both) &&
                                (Basic.intersects(nonFoodProducts, r2.getReactants()) || (useCatalysts && (Basic.intersects(nonFoodProducts, catalysts)) || Basic.intersects(nonFoodProducts, r2.getInhibitions())))
                                && v.getEdgeTo(w) == null) {
                            reactionGraph.newEdge(v, w, EdgeType.Dependency);
                        } else if ((r2.getDirection() == Reaction.Direction.reverse || r2.getDirection() == Reaction.Direction.both) &&
                                (Basic.intersects(nonFoodProducts, r2.getProducts()) || (useCatalysts && (Basic.intersects(nonFoodProducts, catalysts)) || Basic.intersects(nonFoodProducts, r2.getInhibitions())))
                                && v.getEdgeTo(w) == null) {
                            reactionGraph.newEdge(v, w, EdgeType.Dependency);
                        }
                    }
                }
            });
        });
    }
}
