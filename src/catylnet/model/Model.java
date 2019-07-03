/*
 * Model.java Copyright (C) 2019. Daniel H. Huson
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

package catylnet.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * the main  model
 * Daniel Huson, 6.2019
 */
public class Model {
    private final ObservableList<Reaction> reactions = FXCollections.observableArrayList();
    private final ObservableList<MoleculeType> foods = FXCollections.observableArrayList();

    public ObservableList<Reaction> getReactions() {
        return reactions;
    }

    public ObservableList<MoleculeType> getFoods() {
        return foods;
    }

    public void clear() {
        reactions.clear();
        foods.clear();
    }

    /**
     * read reactions and foods
     *
     * @param r
     * @throws IOException
     */
    public void read(Reader r) throws IOException {
        final Set<String> reactionNames = new HashSet<>();

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
                            foods.setAll(Food.parse(line));
                        } else {
                            for (Reaction reaction : Reaction.parse(line)) {
                                if (reactionNames.contains(reaction.getName()))
                                    throw new IOException("Multiple reactions have the same name: " + reaction.getName());
                                reactions.add(reaction);
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
     * write the reactions and foods
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w) throws IOException {
        for (Reaction reaction : reactions) {
            if (reaction.getName().endsWith("+"))
                w.write(reaction.toStringBothWays() + "\n");
            else if (!reaction.getName().endsWith("-"))
                w.write(reaction.toString() + "\n");
        }
        w.write("Food: " + Basic.toString(foods, " ") + "\n");
    }

    public String getReactionsAsString() {
        try (StringWriter w = new StringWriter()) {
            write(w);
            return w.toString();
        } catch (IOException ex) {
            Basic.caught(ex); // can't happen
            return "";
        }
    }
}
