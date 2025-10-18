/*
 *  ModelIO.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.io;

import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;
import catrenet.settings.ArrowNotation;
import catrenet.settings.ReactionNotation;
import jloda.util.Basic;
import jloda.util.StringUtils;
import jloda.util.TriConsumer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static catrenet.io.LineRec.FOOD_BLOCK_START_P;

/**
 * input and output of model
 * Daniel Huson, 7.2019
 */
public class ModelIO {
	public static final MoleculeType FORMAL_FOOD = MoleculeType.valueOf("$");

	/**
	 * read a CRS
	 * @param reactionSystem
	 * @param r
	 * @param reactionNotation
	 * @return
	 * @throws IOException
	 */
	public static String read(ReactionSystem reactionSystem, Reader r, ReactionNotation reactionNotation) throws IOException {
		final BufferedReader br;
		if (r instanceof BufferedReader)
			br = (BufferedReader) r;
		else
			br = new BufferedReader(r);

		// -------- Pass 1: read all lines --------
		List<LineRec> lines = new ArrayList<>();
		String line;
		int idx = 0;
		while ((line = br.readLine()) != null) {
			lines.add(new LineRec(idx++, line));
		}

		// Identify first and last content lines
		int firstContentIdx = -1, lastContentIdx = -1;
		for (int i = 0; i < lines.size(); i++)
			if (lines.get(i).isContent()) {
				firstContentIdx = i;
				break;
			}
		for (int i = lines.size() - 1; i >= 0; i--)
			if (lines.get(i).isContent()) {
				lastContentIdx = i;
				break;
			}

		// Unique-end F: rule (exactly one end is F:)
		boolean startIsF = firstContentIdx >= 0 && lines.get(firstContentIdx).startsWithF();
		boolean endIsF = lastContentIdx >= 0 && lines.get(lastContentIdx).startsWithF();
		boolean uniqueEndF = startIsF ^ endIsF;


		var comments = new StringBuilder();
		var foodItems = new ArrayList<String>();
		var reactionLines = new ArrayList<String>();

		BiConsumer<String, Integer> commentConsumer = (trimmed, n) -> {
			comments.append(trimmed);
		};

		TriConsumer<String, Integer, Boolean> foodLineConsumer = (trimmed, n, fromBlock) -> {
			var tokens = trimmed.split("[:,\\s]+");
			foodItems.addAll(Arrays.asList(tokens).subList((fromBlock ? 0 : 1), tokens.length));
		};

		BiConsumer<String, Integer> reactionLineConsumer = (trimmed, n) -> {
			reactionLines.add(trimmed);
		};


		// -------- Pass 2: classify with Food: block handling --------
		boolean inFoodBlock = false;
		for (var rec : lines) {
			if (rec.isBlank() || rec.trimmed().equalsIgnoreCase("reactions:")) {
				continue;
			}
			if (rec.isComment()) {
				commentConsumer.accept(rec.raw(), rec.lineNo());
				continue;
			}

			if (inFoodBlock) {
				// If current line contains a colon, the Food: block ends *before* this line.
				if (rec.containsColon()) {
					inFoodBlock = false;
					// fall through to classify this line normally
				} else {
					// This line is part of the ongoing Food: block (no colon present)
					foodLineConsumer.accept(rec.raw(), rec.lineNo(), true);
					continue;
				}
			}

			// Not inside a Food: block — classify the current line
			var mBlk = FOOD_BLOCK_START_P.matcher(rec.raw());
			if (mBlk.find()) {
				// Start Food: block
				String inline = mBlk.group(1); // items on same line after 'Food:'
				if (!inline.isBlank()) {
					foodLineConsumer.accept(inline, rec.lineNo(), true);
				}
				inFoodBlock = true;
				continue;
			}

			if (rec.startsFoodSimple()) {
				// Single-line "Food ..." or "FoodSet ..." (no colon)
				foodLineConsumer.accept(rec.raw(), rec.lineNo(), false);
				continue;
			}

			if (rec.startsWithF()) {
				// Ambiguous "F:" line — FoodSet only if unique first-or-last content line
				var isAtStart = (rec.idx() == firstContentIdx);
				var isAtEnd = (rec.idx() == lastContentIdx);
				if (uniqueEndF && (isAtStart || isAtEnd)) {
					foodLineConsumer.accept(rec.raw(), rec.lineNo(), /*fromBlock*/false);
				} else {
					reactionLineConsumer.accept(rec.raw(), rec.lineNo());
				}
				continue;
			}
			// Default: reaction
			reactionLineConsumer.accept(rec.raw(), rec.lineNo());
		}

		final var reactionNames = new HashSet<String>();
		final var auxReactions = new HashSet<Reaction>();

		if (!foodItems.isEmpty()) {
			reactionSystem.getFoods().addAll(foodItems.stream().filter(f -> !f.isEmpty()).map(MoleculeType::valueOf).collect(Collectors.toSet()));
		}

		if (!reactionLines.isEmpty()) {
			for (var reactionLine : reactionLines) {
				if (!reactionLine.isBlank()) {
					var reaction = Reaction.parse(reactionLine, auxReactions, reactionNotation.equals(ReactionNotation.Tabbed));
					if (reactionNames.contains(reaction.getName()))
						throw new IOException("Multiple reactions have the same name: " + reaction.getName());
					reactionSystem.getReactions().add(reaction);
					reactionNames.add(reaction.getName());
					if (reaction.getCatalysts().contains(FORMAL_FOOD.getName())) {
						if (!reactionSystem.getFoods().contains(FORMAL_FOOD))
							reactionSystem.getFoods().add(FORMAL_FOOD);
					}
				}
			}
		}
		return comments.toString();
	}


	/**
	 * write model as string
	 *
	 * @return string
	 */
	public static String toString(ReactionSystem reactionSystem, boolean includeFood, ReactionNotation reactionNotation, ArrowNotation arrowNotation) {
		try (StringWriter w = new StringWriter()) {
			write(reactionSystem, w, includeFood, reactionNotation, arrowNotation);
			return w.toString();
		} catch (IOException e) {
			Basic.caught(e);
			return "";
		}
	}

	/**
	 * write model
	 *
	 * @param includeFood include food line
	 */
	public static void write(ReactionSystem reactionSystem, Writer w, boolean includeFood, ReactionNotation reactionNotation, ArrowNotation arrowNotation) throws IOException {
		if (includeFood) {
			w.write("Food: " + getFoodString(reactionSystem, reactionNotation) + "\n\n");
		}

		for (Reaction reaction : reactionSystem.getReactions()) {
			w.write(toString(reaction, reactionNotation, arrowNotation) + "\n");
		}
	}


	/**
	 * get the food string
	 *
	 * @return food string
	 */
	public static String getFoodString(ReactionSystem reactionSystem, ReactionNotation reactionNotation) {
		try (StringWriter w = new StringWriter()) {
			w.write(StringUtils.toString(reactionSystem.getFoods().stream().filter(s -> !s.equals(FORMAL_FOOD)).collect(Collectors.toList()), reactionNotation == ReactionNotation.Full ? ", " : " "));
			return w.toString();
		} catch (IOException ex) {
			return "";
		}
	}

	public static String toString(Reaction reaction, ReactionNotation reactionNotation, ArrowNotation arrowNotation) {
		final String arrow;
		switch (reaction.getDirection()) {
			default:
			case forward:
				arrow = (arrowNotation == ArrowNotation.UsesEquals ? "=>" : "->");
				break;
			case reverse:
				arrow = (arrowNotation == ArrowNotation.UsesEquals ? "<=" : "<-");
				break;
			case both:
				arrow = (arrowNotation == ArrowNotation.UsesEquals ? "<=>" : "<->");
				break;
		}

		var catalystFree = reaction.getCatalysts().equals(FORMAL_FOOD.getName());
		if (reactionNotation == ReactionNotation.Tabbed) {
			return String.format("%s\t%s %s %s\t%s%s",
					reaction.getName(), StringUtils.toString(reaction.getReactants(), " + "),
					arrow, StringUtils.toString(reaction.getProducts(), " + "), (catalystFree ? "" : reaction.getCatalysts()),
					(reaction.getInhibitions().size() == 0 ? "" : "\t" + StringUtils.toString(reaction.getInhibitions(), " ")));
		} else {
			var reactantString = StringUtils.toString(reaction.getReactants(), " + ");
			var catalystString = (catalystFree ? "" : "[%s]".formatted((reactionNotation == ReactionNotation.Full ? reaction.getCatalysts() : reaction.getCatalysts().replaceAll("\\s+,\\s+", " "))));
			var inhibitorString = (reaction.getInhibitions().size() == 0 ? " " : " {" + StringUtils.toString(reaction.getInhibitions(), reactionNotation == ReactionNotation.Full ? "," : " ") + "} ");
			var productString = StringUtils.toString(reaction.getProducts(), " + ");
			return String.format("%s : %s %s%s%s %s", reaction.getName(), reactantString, catalystString, inhibitorString, arrow, productString);
		}
	}
}
