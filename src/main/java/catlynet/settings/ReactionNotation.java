/*
 * ReactionNotation.java Copyright (C) 2023 Daniel H. Huson
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

import jloda.util.Pair;
import jloda.util.StringUtils;

import java.util.Collection;

/**
 * choice of input and output formats
 */
public enum ReactionNotation {
    Full, Sparse, Tabbed;

    public static ReactionNotation valueOfIgnoreCase(String name) {
		return StringUtils.valueOfIgnoreCase(ReactionNotation.class, name);
    }

    /**
     * detects the file format or returns null
     *
     * @return pair of reaction notation format and arrow notation
	 */
    public static Pair<ReactionNotation, ArrowNotation> detectNotation(Collection<String> lines) {
        boolean arrowsUseEquals = false;
        boolean arrowsUseMinus = false;
        boolean containsTabs = false;
        boolean containsSquareBrackets = false;
        boolean containsCommas = false;

        for (String line : lines) {
            if (!line.startsWith("#") && !line.startsWith("Food:") && !line.startsWith("F:")) {
                if (line.contains("\t"))
                    containsTabs = true;
                if (line.contains(","))
                    containsCommas = true;
                if (line.contains("[") || line.contains("]"))
                    containsSquareBrackets = true;
                if (line.contains("=>") || line.contains("<="))
                    arrowsUseEquals = true;
                if (line.contains("->") || line.contains("<-"))
                    arrowsUseMinus = true;
            }
        }
        if (arrowsUseEquals || arrowsUseMinus) { // arrows must be present
            if (containsSquareBrackets) {
                if (containsCommas)
                    return new Pair<>(Full, arrowsUseEquals ? ArrowNotation.UsesEquals : ArrowNotation.UsesMinus);
                else
                    return new Pair<>(Sparse, arrowsUseEquals ? ArrowNotation.UsesEquals : ArrowNotation.UsesMinus);
            } else if (containsTabs)
                return new Pair<>(Tabbed, arrowsUseEquals ? ArrowNotation.UsesEquals : ArrowNotation.UsesMinus);
        }
        return null;
    }
}
