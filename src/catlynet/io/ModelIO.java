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

import catlynet.model.Model;
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ModelIO {

    /**
     * read reactions and foods
     *
     * @param r
     * @throws IOException
     */
    public static void read(Model model, Reader r) throws IOException {
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
                        if (line.startsWith("Food:")) {
                            model.getFoods().addAll(parseFood(line));
                        } else {
                            for (Reaction reaction : Reaction.parse(line, auxReactions)) {
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
        }

        final ArrayList<MoleculeType> array = new ArrayList<>();
        for (String name : aLine.split("\\s+")) {
            array.add(MoleculeType.valueOf(name));
        }
        return array;

    }

    public static void write(Model model, Writer w, boolean includeFood, boolean simplify) throws IOException {
        if (includeFood)
            w.write("Food: " + Basic.toString(model.getFoods(), " ") + "\n");

        for (Reaction reaction : model.getReactions()) {
            if (!simplify)
                w.write(reaction.toString() + "\n");
            else {
                if (!reaction.getName().endsWith("/")) {
                    if (reaction.getName().endsWith("+"))
                        w.write(reaction.toStringBothWays() + "\n");
                    else if (!reaction.getName().endsWith("-"))
                        w.write(reaction.toString() + "\n");
                }
            }
        }
    }

    public static String getReactionsAsString(Model model) {
        try (StringWriter w = new StringWriter()) {
            write(model, w, false, true);
            return w.toString();
        } catch (IOException ex) {
            Basic.caught(ex); // can't happen
            return "";
        }
    }
}
