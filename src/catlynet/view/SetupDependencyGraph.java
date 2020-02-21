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

        for (Reaction reaction1 : reactionSystem.getReactions()) {
            final Node v = reactionGraph.newNode(reaction1);
            reactionNodeMap.put(reaction1, v);
            final Set<MoleculeType> nonFoodProducts = new HashSet<>(reaction1.getProducts());
            nonFoodProducts.removeAll(reactionSystem.getFoods());
            for (Reaction reaction2 : reactionSystem.getReactions()) {
                if (reaction2 == reaction1)
                    break;
                final Node w = reactionNodeMap.get(reaction2);

                if (Basic.intersects(nonFoodProducts, reaction2.getReactants()) || (useCatalysts && (Basic.intersects(nonFoodProducts, reaction2.getCatalysts())) || Basic.intersects(nonFoodProducts, reaction2.getInhibitions()))) {
                    reactionGraph.newEdge(v, w, EdgeType.Dependency);
                }
            }
        }
    }
}
