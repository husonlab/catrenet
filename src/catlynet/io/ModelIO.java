/*
 * ModelIO.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.io;

import catlynet.format.ArrowNotation;
import catlynet.format.ReactionNotation;
import catlynet.model.Model;
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * input and output of model
 * Daniel Huson, 7.2019
 */
public class ModelIO {
    /**
     * parse a one line description of food
     *
     * @param aLine
     * @return food
     */
    public static ArrayList<MoleculeType> parseFood(String aLine) {
        aLine = aLine.replaceAll(",", " ").replaceAll("\\s+", " ");
        if (aLine.startsWith("Food:")) {
            if (aLine.length() > "Food:".length())
                aLine = aLine.substring("Food:".length() + 1).trim();
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
     * @param r
     * @throws IOException
     */
    public static void read(Model model, Reader r, ReactionNotation reactionNotation) throws IOException {
        final Set<String> reactionNames = new HashSet<>();
        final Set<Reaction> auxReactions = new HashSet<>();

        int lineNumber = 0;
        String line;
        final BufferedReader br;
        if (r instanceof BufferedReader)
            br = (BufferedReader) r;
        else
            br = new BufferedReader(r);
        while ((line = br.readLine()) != null) {
            lineNumber++;
            if (!line.startsWith("#")) {
                line = line.trim();
                if (line.length() > 0)
                    try {
                        if (line.startsWith("Food:") || (line.startsWith("F:") && !line.contains("->") && !line.contains("=>") && !line.contains("<-") && !line.contains("<="))) {
                            model.getFoods().addAll(parseFood(line));
                        } else {
                            for (Reaction reaction : Reaction.parse(line, auxReactions, reactionNotation.equals(ReactionNotation.Tabbed))) {
                                if (reactionNames.contains(reaction.getName()))
                                    throw new IOException("Multiple reactions have the same name: " + reaction.getName());
                                model.getReactions().add(reaction);
                                reactionNames.add(reaction.getName());
                            }
                        }
                    } catch (IOException ex) {
                        throw new IOExceptionWithLineNumber(ex.getMessage(), lineNumber);
                    }
            }
        }
    }

    /**
     * write model as string
     *
     * @param model
     * @param includeFood
     * @param simplify
     * @return string
     */
    public static String toString(Model model, boolean includeFood, boolean simplify, ReactionNotation reactionNotation, ArrowNotation arrowNotation) {
        try (StringWriter w = new StringWriter()) {
            write(model, w, includeFood, simplify, reactionNotation, arrowNotation);
            return w.toString();
        } catch (IOException e) {
            Basic.caught(e);
            return "";
        }
    }

    /**
     * write model
     *
     * @param model
     * @param w
     * @param includeFood include food line
     * @param simplify    simply all expanded reactions
     * @throws IOException
     */
    public static void write(Model model, Writer w, boolean includeFood, boolean simplify, ReactionNotation reactionNotation, ArrowNotation arrowNotation) throws IOException {
        if (includeFood) {
            w.write("Food: " + getFoodString(model, simplify, reactionNotation) + "\n\n");
        }

        for (Reaction reaction : model.getReactions()) {
            if (!simplify)
                w.write(toString(reaction, reactionNotation, arrowNotation) + "\n");
            else {
                if (!reaction.getName().endsWith("/")) {
                    if (reaction.getName().endsWith("+"))
                        w.write(Basic.ensureSpaceAround(toStringBothWays(reaction, reactionNotation, arrowNotation), '&') + "\n");
                    else if (!reaction.getName().endsWith("-"))
                        w.write(Basic.ensureSpaceAround(toString(reaction, reactionNotation, arrowNotation), '&') + "\n");
                }
            }
        }
    }


    /**
     * get the food string
     *
     * @param model
     * @param simplify
     * @param reactionNotation
     * @return food string
     */
    public static String getFoodString(Model model, boolean simplify, ReactionNotation reactionNotation) {
        try (StringWriter w = new StringWriter()) {
            if (!simplify)
                w.write(Basic.toString(model.getFoods(), reactionNotation == ReactionNotation.Full ? ", " : " "));
            else {
                boolean first = true;
                for (MoleculeType food : model.getFoods()) {
                    if (!food.getName().contains("&")) {
                        if (first)
                            first = false;
                        else
                            w.write(reactionNotation == ReactionNotation.Full ? ", " : " ");
                        w.write(food.getName());
                    }
                }
            }
            return w.toString();
        } catch (IOException ex) {
            return "";
        }
    }

    public static String toString(Reaction reaction, ReactionNotation reactionNotation, ArrowNotation arrowNotation) {
        if (reactionNotation == ReactionNotation.Tabbed)
            return String.format("%s\t%s %s %s\t%s%s",
                    reaction.getName(), Basic.toString(reaction.getReactants(), " + "),
                    arrowNotation == ArrowNotation.UsesEquals ? "=>" : "->", Basic.toString(reaction.getProducts(), " + "), Basic.toString(reaction.getCatalysts(), " "),
                    (reaction.getInhibitors().size() == 0 ? "" : "\t" + Basic.toString(reaction.getInhibitors(), " ")));
        else
            return String.format("%s : %s [%s]%s%s %s",
                    reaction.getName(), Basic.toString(reaction.getReactants(), " + "), Basic.toString(reaction.getCatalysts(), reactionNotation == ReactionNotation.Full ? ", " : " "),
                    (reaction.getInhibitors().size() == 0 ? " " : " (" + Basic.toString(reaction.getInhibitors(), reactionNotation == ReactionNotation.Full ? ", " : " ") + ") "),
                    arrowNotation == ArrowNotation.UsesEquals ? "=>" : "->", Basic.toString(reaction.getProducts(), " + "));
    }

    public static String toStringBothWays(Reaction reaction, ReactionNotation reactionNotation, ArrowNotation arrowNotation) {
        if (reactionNotation == ReactionNotation.Tabbed)
            return String.format("%s\t%s %s %s\t%s%s",
                    (reaction.getName().endsWith("+") ? reaction.getName().substring(0, reaction.getName().length() - 1) : reaction.getName()),
                    Basic.toString(reaction.getReactants(), " + "),
                    arrowNotation == ArrowNotation.UsesEquals ? "<=>" : "<->", Basic.toString(reaction.getProducts(), " + "), Basic.toString(reaction.getCatalysts(), " "),
                    (reaction.getInhibitors().size() == 0 ? "" : "\t" + Basic.toString(reaction.getInhibitors(), " ")));
        else
            return String.format("%s : %s [%s]%s%s %s",
                    (reaction.getName().endsWith("+") ? reaction.getName().substring(0, reaction.getName().length() - 1) : reaction.getName()),
                    Basic.toString(reaction.getReactants(), " + "),
                    Basic.toString(reaction.getCatalysts(), reactionNotation == ReactionNotation.Full ? ", " : " "), arrowNotation == ArrowNotation.UsesEquals ? "<=>" : "<->",
                    (reaction.getInhibitors().size() == 0 ? " " : " (" + Basic.toString(reaction.getInhibitors(), reactionNotation == ReactionNotation.Full ? ", " : " ") + ") "),
                    Basic.toString(reaction.getProducts(), " + "));
    }

}
