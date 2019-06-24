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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * the main model
 * Daniel Huson, 6.2019
 */
public class Model {
    private final ObservableList<Reaction> reactions = FXCollections.observableArrayList();
    private final ObservableList<Food> foods = FXCollections.observableArrayList();

    public ObservableList<Reaction> getReactions() {
        return reactions;
    }

    public ObservableList<Food> getFoods() {
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
    public void read(BufferedReader r) throws IOException {
        final Set<String> reactionNames = new HashSet<>();

        String line;
        while ((line = r.readLine()) != null)
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
}
