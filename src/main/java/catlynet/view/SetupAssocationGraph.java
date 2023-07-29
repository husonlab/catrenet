/*
 * SetupAssocationGraph.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.CollectionUtils;
import jloda.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * setup up the  association graph
 * Daniel Huson, 2.2020
 */
public class SetupAssocationGraph {
	/**
	 * apply
	 */
	public static void apply(Graph reactionGraph, ReactionSystem reactionSystem, boolean useCatalysts) {
		final var reactionNodeMap = new HashMap<Reaction, Node>();

		reactionSystem.getReactions().forEach(r -> reactionNodeMap.put(r, reactionGraph.newNode(r)));

		reactionSystem.getReactions().forEach(r1 -> {
			final var v = reactionNodeMap.get(r1);

			reactionSystem.getReactions().stream().filter(r2 -> r2 != r1).forEach(r2 -> {
				final var w = reactionNodeMap.get(r2);

				for (var z = 0; z <= 1; z++) { // try forward, then reverse
					final var nonFoodProducts = new HashSet<MoleculeType>();
					if (z == 0) {
						if (r1.getDirection() == Reaction.Direction.forward || r1.getDirection() == Reaction.Direction.both) {
							nonFoodProducts.addAll(r1.getProducts());
							reactionSystem.getFoods().forEach(nonFoodProducts::remove);
						} else
							continue;
					} else // z==1
					{
						if (r1.getDirection() == Reaction.Direction.reverse || r1.getDirection() == Reaction.Direction.both) {
							nonFoodProducts.addAll(r1.getReactants());
							reactionSystem.getFoods().forEach(nonFoodProducts::remove);
                        } else
                            continue;
                    }

                    if (nonFoodProducts.size() > 0) {
						final var catalysts = new HashSet<MoleculeType>();
						r2.getCatalystConjunctions().forEach(c -> catalysts.addAll(MoleculeType.valuesOf(StringUtils.split(c.getName(), '&'))));

						if ((r2.getDirection() == Reaction.Direction.forward || r2.getDirection() == Reaction.Direction.both) &&
							(CollectionUtils.intersects(nonFoodProducts, r2.getReactants()) || (useCatalysts && (CollectionUtils.intersects(nonFoodProducts, catalysts)) || CollectionUtils.intersects(nonFoodProducts, r2.getInhibitions())))
							&& v.getEdgeTo(w) == null) {
							reactionGraph.newEdge(v, w, EdgeType.Association);
						} else if ((r2.getDirection() == Reaction.Direction.reverse || r2.getDirection() == Reaction.Direction.both) &&
								   (CollectionUtils.intersects(nonFoodProducts, r2.getProducts()) || (useCatalysts && (CollectionUtils.intersects(nonFoodProducts, catalysts)) || CollectionUtils.intersects(nonFoodProducts, r2.getInhibitions())))
								   && v.getEdgeTo(w) == null) {
							reactionGraph.newEdge(v, w, EdgeType.Association);
						}
					}
                }
            });
        });
    }
}
