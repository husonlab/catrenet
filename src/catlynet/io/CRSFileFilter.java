/*
 * CRSFileFilter.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.io;

import javafx.stage.FileChooser;

/**
 * catalytic reactions system file filter
 * Daniel Huson, 7.2019
 */
public class CRSFileFilter {
    private static FileChooser.ExtensionFilter instance;

    public static FileChooser.ExtensionFilter getInstance() {
        if (instance == null)
            instance = new FileChooser.ExtensionFilter("Catalytic reaction system file", "*.crs", "*.crs.gz", "*.txt", "*.txt.gz");
        return instance;
    }
}
