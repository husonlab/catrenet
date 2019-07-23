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

package catlynet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

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

    private int numberOfTwoWayReactions = 0;

    private final StringProperty name = new SimpleStringProperty("Reactions");


    public Model() {
        reactions.addListener((ListChangeListener<Reaction>) e -> {
            while (e.next()) {
                for (Reaction reaction : e.getAddedSubList()) {
                    if (reaction.getName().endsWith("+"))
                        numberOfTwoWayReactions++;
                }
                for (Reaction reaction : e.getRemoved()) {
                    if (reaction.getName().endsWith("+"))
                        numberOfTwoWayReactions--;
                }
            }
        });
    }

    /**
     * create a shallow copy that references reactions
     *
     * @return shallow copy of this model
     */
    public Model shallowCopy() {
        final Model result = new Model();
        result.foods.addAll(foods);
        result.reactions.addAll(reactions);
        return result;
    }

    public void clear() {
        reactions.clear();
        foods.clear();
        name.set("Reactions");
    }

    /**
     * gets the size
     *
     * @return number of reactions
     */
    public int size() {
        return reactions.size();
    }

    public int getNumberOfTwoWayReactions() {
        return numberOfTwoWayReactions;
    }

    public int getNumberOfOneWayReactions() {
        return size() - 2 * getNumberOfTwoWayReactions();
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * determines whether model currently contains inhibitors
     *
     * @return true, if inhibitors present
     */
    public boolean containsInhibitors() {
        for (Reaction reaction : getReactions()) {
            if (reaction.getInhibitors().size() > 0)
                return true;
        }
        return false;
    }
}
