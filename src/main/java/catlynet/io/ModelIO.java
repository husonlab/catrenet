/*
 * ModelIO.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.io;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.settings.ArrowNotation;
import catlynet.settings.ReactionNotation;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * input and output of model
 * Daniel Huson, 7.2019
 */
public class ModelIO {
	public static final MoleculeType FORMAL_FOOD = MoleculeType.valueOf("$");

	/**
	 * parse a one line description of food
	 *
	 * @return food
	 */
	public static ArrayList<MoleculeType> parseFood(String aLine) {
		aLine = aLine.replaceAll(",", " ").replaceAll("\\s+", " ");
		if (aLine.startsWith("Food:")) {
			if (aLine.length() > "Food:".length())
				aLine = aLine.substring("Food:".length() + 1).trim();
			else
				aLine = "";
		} else if (aLine.startsWith("Food")) {
			if (aLine.length() > "Food".length())
				aLine = aLine.substring("Food".length() + 1).trim();
			else
				aLine = "";
		} else if (aLine.startsWith("F:")) {
			if (aLine.length() > "F:".length())
				aLine = aLine.substring("F:".length() + 1).trim();
			else
				aLine = "";
		}

		final ArrayList<MoleculeType> array = new ArrayList<>();
		for (String name : aLine.split("\\s+")) {
			array.add(MoleculeType.valueOf(name));
		}
		return array;
	}

	/**
	 * read reactions and foods
	 *
	 * @return leading comments
	 */
	public static String read(ReactionSystem reactionSystem, Reader r, ReactionNotation reactionNotation) throws IOException {
		final var reactionNames = new HashSet<String>();
		final var auxReactions = new HashSet<Reaction>();

		final var buf = new StringBuilder();

		var lineNumber = 0;
		String line;
		final BufferedReader br;
		if (r instanceof BufferedReader)
			br = (BufferedReader) r;
		else
			br = new BufferedReader(r);

		var inLeadingComments = true;

		while ((line = br.readLine()) != null) {
			lineNumber++;
			if (!line.startsWith("#")) {
				inLeadingComments = false;
				line = line.trim();
				if (line.length() > 0)
					try {
						if (line.startsWith("Food:") || (line.startsWith("F:") && !line.contains("->") && !line.contains("=>") && !line.contains("<-") && !line.contains("<="))) {
							reactionSystem.getFoods().addAll(parseFood(line));
						} else {
							var reaction = Reaction.parse(line, auxReactions, reactionNotation.equals(ReactionNotation.Tabbed));
							if (reactionNames.contains(reaction.getName()))
								throw new IOException("Multiple reactions have the same name: " + reaction.getName());
							reactionSystem.getReactions().add(reaction);
							reactionNames.add(reaction.getName());
							if (reaction.getCatalysts().contains(FORMAL_FOOD.getName())) {
								if (!reactionSystem.getFoods().contains(FORMAL_FOOD))
									reactionSystem.getFoods().add(FORMAL_FOOD);
							}
						}
					} catch (Exception ex) {
						throw new IOExceptionWithLineNumber(ex.getMessage(), lineNumber);
					}
			} else if (inLeadingComments) {
				buf.append(line).append("\n");
			}
		}
		return buf.toString();
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
