/*
 * Stratification.java Copyright (C) 2025 Daniel H. Huson
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

import catrenet.algorithm.Utilities;
import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;

import java.util.HashSet;

public class Stratification {
	public static String report(ReactionSystem reactionSystem) {
		var buf = new StringBuilder();
		buf.append("\nStratification of reactions and molecules:\n");
		var i = 0;
		buf.append("Rank ").append(i).append(" molecules (food set):\n");
		var molecules = new HashSet<>(reactionSystem.getFoods());
		for (var moleculeType : molecules) {
			if (!moleculeType.getName().equals("$"))
				buf.append("\t").append(moleculeType.getName()).append("\n");
		}
		buf.append("\n");

		var reactions = new HashSet<Reaction>();

		while (true) {
			i++;
			var pair = Utilities.computeOneStep(molecules, reactionSystem.getReactions());
			if (pair.getKey().size() == reactions.size())
				break;
			buf.append("Rank ").append(i).append(" reactions:\n");
			for (var reaction : pair.getKey()) {
				if (!reactions.contains(reaction)) {
					buf.append("\t").append(reaction.getName()).append("\n");
				}
			}
			reactions.addAll(pair.getKey());
			buf.append("Rank ").append(i).append(" molecules:\n");
			for (var moleculeType : pair.getValue()) {
				if (!molecules.contains(moleculeType)) {
					buf.append("\t").append(moleculeType.getName()).append("\n");
				}
			}
			molecules.addAll(pair.getValue());
			buf.append("\n");
		}
		return buf.toString();
	}
}
