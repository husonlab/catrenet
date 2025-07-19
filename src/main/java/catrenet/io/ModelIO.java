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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	 * @param reactionsOnly
	 * @return
	 * @throws IOException
	 */
	public static String read(ReactionSystem reactionSystem, Reader r, ReactionNotation reactionNotation) throws IOException {
		final BufferedReader br;
		if (r instanceof BufferedReader)
			br = (BufferedReader) r;
		else
			br = new BufferedReader(r);

		var comments = new StringBuilder();
		var foodItems = new ArrayList<String>();
		var reactionLines = new ArrayList<String>();

		var foodSectionPattern = Pattern.compile("(?i)^\\s*(Food set|Food|F):\\s*(.*)");
		var reactionSectionPattern = Pattern.compile("(?i)^\\s*(Reactions|R):\\s*(.*)");

		enum Section {HEADER, FOOD, REACTIONS}
		var section = Section.HEADER;


		String line;
		while ((line = br.readLine()) != null) {
			var trimmed = line.trim();
			if (trimmed.isEmpty()) continue;

			switch (section) {
				case HEADER -> {
					if (trimmed.startsWith("#")) {
						comments.append(trimmed).append("\n");
					} else if (foodSectionPattern.matcher(trimmed).matches()) {
						var matcher = foodSectionPattern.matcher(trimmed);
						if (matcher.find()) {
							var words = matcher.group(2);
							if (!words.isBlank())
								foodItems.addAll(Arrays.asList(words.trim().split("[,\\s]+")));
						}
						section = Section.FOOD;
					} else if (reactionSectionPattern.matcher(trimmed).matches()) {
						var matcher = reactionSectionPattern.matcher(trimmed);
						if (matcher.find()) {
							var reaction = matcher.group(2);
							if (!reaction.isBlank())
								reactionLines.add(reaction.trim());
						}
						section = Section.REACTIONS;
					}
				}

				case FOOD -> {
					if (reactionSectionPattern.matcher(trimmed).matches()) {
						var matcher = reactionSectionPattern.matcher(trimmed);
						if (matcher.find()) {
							var reaction = matcher.group(2);
							if (!reaction.isBlank())
								reactionLines.add(reaction.trim());
						}
						section = Section.REACTIONS;
						} else {
						foodItems.addAll(Arrays.asList(trimmed.split("[,\\s]+")));
					}
				}

				case REACTIONS -> reactionLines.add(trimmed);
			}
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
