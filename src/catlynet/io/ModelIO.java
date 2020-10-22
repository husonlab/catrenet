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
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
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
     * @param r
     * @return leading comments
     * @throws IOException
     */
    public static String read(ReactionSystem reactionSystem, Reader r, ReactionNotation reactionNotation) throws IOException {
        final Set<String> reactionNames = new HashSet<>();
        final Set<Reaction> auxReactions = new HashSet<>();

        final StringBuilder buf = new StringBuilder();

        int lineNumber = 0;
        String line;
        final BufferedReader br;
        if (r instanceof BufferedReader)
            br = (BufferedReader) r;
        else
            br = new BufferedReader(r);

        boolean inLeadingComments = true;

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
                            Reaction reaction = Reaction.parse(line, auxReactions, reactionNotation.equals(ReactionNotation.Tabbed));
                            if (reactionNames.contains(reaction.getName()))
                                throw new IOException("Multiple reactions have the same name: " + reaction.getName());
                            reactionSystem.getReactions().add(reaction);
                            reactionNames.add(reaction.getName());
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
     * @param reactionSystem
     * @param includeFood
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
     * @param reactionSystem
     * @param w
     * @param includeFood    include food line
     * @throws IOException
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
     * @param reactionSystem
     * @param reactionNotation
     * @return food string
     */
    public static String getFoodString(ReactionSystem reactionSystem, ReactionNotation reactionNotation) {
        try (StringWriter w = new StringWriter()) {
            w.write(Basic.toString(reactionSystem.getFoods(), reactionNotation == ReactionNotation.Full ? ", " : " "));
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

        if (reactionNotation == ReactionNotation.Tabbed)
            return String.format("%s\t%s %s %s\t%s%s",
                    reaction.getName(), Basic.toString(reaction.getReactants(), " + "),
                    arrow, Basic.toString(reaction.getProducts(), " + "), reaction.getCatalysts(),
                    (reaction.getInhibitions().size() == 0 ? "" : "\t" + Basic.toString(reaction.getInhibitions(), " ")));
        else
            return String.format("%s : %s [%s]%s%s %s",
                    reaction.getName(), Basic.toString(reaction.getReactants(), " + "), (reactionNotation == ReactionNotation.Full ? reaction.getCatalysts() : reaction.getCatalysts().replaceAll("\\s+,\\s+", " ")),
                    (reaction.getInhibitions().size() == 0 ? " " : " {" + Basic.toString(reaction.getInhibitions(), reactionNotation == ReactionNotation.Full ? "," : " ") + "} "),
                    arrow, Basic.toString(reaction.getProducts(), " + "));
    }
}
