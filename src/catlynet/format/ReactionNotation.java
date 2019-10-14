/*
 * ReactionNotation.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.format;

import jloda.util.Basic;
import jloda.util.FileLineIterator;
import jloda.util.Pair;

import java.io.File;
import java.io.IOException;

/**
 * choice of input and output formats
 */
public enum ReactionNotation {
    Full, Sparse, Tabbed;

    public static ReactionNotation valueOfIgnoreCase(String name) {
        return Basic.valueOfIgnoreCase(ReactionNotation.class, name);
    }

    /**
     * detects the file format or returns null
     *
     * @param file
     * @return pair of reaction notation format and arrow notation
     * @throws IOException
     */
    public static Pair<ReactionNotation, ArrowNotation> detectNotation(File file) throws IOException {
        boolean arrowsUseEquals = false;
        boolean arrowsUseMinus = false;
        boolean containsTabs = false;
        boolean containsSquareBrackets = false;
        boolean containsCommas = false;

        try (FileLineIterator it = new FileLineIterator(file)) {
            int seen = 0;
            while (it.hasNext()) {
                final String line = it.next();

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
                    seen++;
                }
                if (seen >= 10)
                    break; // ten lines should be enough...
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
