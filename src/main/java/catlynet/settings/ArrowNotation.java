/*
 * ArrowNotation.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.settings;

/**
 * arrow notation
 * Daniel Huson, 7.2019
 */
public enum ArrowNotation {
    UsesEquals("=>"), UsesMinus("->");

    private final String label;

    ArrowNotation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ArrowNotation valueOfLabel(String label) {
        if (UsesEquals.getLabel().equalsIgnoreCase(label))
            return UsesEquals;
        else if (UsesMinus.getLabel().equalsIgnoreCase(label))
            return UsesMinus;
        else
            return null;
    }
}
