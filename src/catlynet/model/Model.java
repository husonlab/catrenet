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

import javafx.collections.FXCollections;
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

    public void clear() {
        reactions.clear();
        foods.clear();
    }
}
