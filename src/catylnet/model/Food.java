/*
 * Food.java Copyright (C) 2019. Daniel H. Huson
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

/**
 * a food item
 * Daniel Huson, 6.2019
 */
public class Food extends MoleculeType {
    public Food(String name) {
        super(name);
    }

    /**
     * parse a one line description of food
     *
     * @param aLine
     * @return food
     */
    public static Food[] parse(String aLine) {
        aLine = aLine.replaceAll(",", " ").replaceAll("\\s+", " ");
        if (aLine.startsWith("Food:")) {
            if (aLine.length() > "Food:".length())
                aLine = aLine.substring("Food:".length() + 1).trim();
            else
                aLine = "";
        }

        final String[] names = aLine.split("\\s+");
        final Food[] array = new Food[names.length];
        for (int i = 0; i < names.length; i++)
            array[i] = new Food(names[i]);
        return array;
    }

}
