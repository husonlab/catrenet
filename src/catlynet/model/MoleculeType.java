/*
 * MoleculeType.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * a molecule type
 * Daniel Huson, 6.2019
 */
public class MoleculeType implements Comparable<MoleculeType> {
    private final static Map<String, MoleculeType> name2type = new HashMap<>();

    private final String name;

    /**
     * creates the named molecule
     *
     * @return molecule
     */
    public static MoleculeType valueOf(String name) {
        if (!name2type.containsKey(name))
            name2type.put(name, new MoleculeType(name));
        return name2type.get(name);
    }

    private MoleculeType(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(MoleculeType b) {
        return name.compareTo(b.name);
    }

    /**
     * create molecule types from a list of names
     *
     * @return types
     */
    public static Collection<MoleculeType> valuesOf(String[] names) {
        final ArrayList<MoleculeType> list = new ArrayList<>(names.length);
        for (String name : names) {
            list.add(valueOf(name));
        }
        return list;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof MoleculeType && name.equals(((MoleculeType) that).name);
    }
}
