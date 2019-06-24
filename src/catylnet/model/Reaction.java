/*
 * Reaction.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.util.Basic;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * a reaction
 * Daniel Huson, 6.2019
 */
public class Reaction {
    private final String name;

    private final Set<MoleculeType> reactants = new TreeSet<>();
    private final Set<MoleculeType> products = new TreeSet<>();
    private final Set<MoleculeType> catalysts = new TreeSet<>();

    /**
     * constructor
     *
     * @param name
     */
    public Reaction(String name) {
        this.name = name;
    }

    /**
     * get the name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    public Set<MoleculeType> getReactants() {
        return reactants;
    }

    public Set<MoleculeType> getProducts() {
        return products;
    }

    public Set<MoleculeType> getCatalysts() {
        return catalysts;
    }

    public String toString() {
        return String.format("%s\t%s -> %s\t%s",
                name, Basic.toString(reactants, " "), Basic.toString(products, " "), Basic.toString(catalysts, " "));
    }

    public String toStringBothWays() {
        return String.format("%s\t%s <-> %s\t%s",
                name, Basic.toString(reactants, " "), Basic.toString(products, " "), Basic.toString(catalysts, " "));
    }

    /**
     * parses a reaction
     * Format:
     * name tab reactant ... => product ... tab catalyst ...
     * name tab reactant ... <=> product ... tab catalyst ...
     *
     * @param line
     * @return one or two reactions
     * @throws IOException
     */
    public static Reaction[] parse(String line) throws IOException {
        final String[] tokens = line.split("\t");
        if (tokens.length != 3)
            throw new IOException("Can't parse reaction: " + line);
        final String reactionName = tokens[0];

        int forwardPos = tokens[1].indexOf('>');
        int separatorPos = tokens[1].indexOf('-');
        if (separatorPos == -1)
            separatorPos = tokens[1].indexOf('=');
        int backwardPos = tokens[1].indexOf('<');

        int left = tokens[1].length();
        int right = 0;

        if (forwardPos != -1) {
            left = Math.min(left, forwardPos);
            right = Math.max(right, forwardPos);
        }
        if (separatorPos != -1) {
            left = Math.min(left, separatorPos);
            right = Math.max(right, separatorPos);
        }
        if (backwardPos != -1) {
            left = Math.min(left, backwardPos);
            right = Math.max(right, backwardPos);
        }

        if (right - left != 1 && right - left != 2)
            throw new IOException("Can't parse reaction: " + line);

        final String[] a = tokens[1].substring(0, left).trim().split(" ");

        final String[] b = tokens[1].substring(right + 1).trim().split(" ");

        final String[] c = tokens[2].split("\\s+");

        if (forwardPos >= 0 && backwardPos >= 0) {
            final Reaction reaction1 = new Reaction(reactionName + "+");
            reaction1.getReactants().addAll(MoleculeType.valueOf(a));
            reaction1.getProducts().addAll(MoleculeType.valueOf(b));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(c));

            final Reaction reaction2 = new Reaction(reactionName + "-");
            reaction2.getReactants().addAll(MoleculeType.valueOf(b));
            reaction2.getProducts().addAll(MoleculeType.valueOf(a));
            reaction2.getCatalysts().addAll(MoleculeType.valueOf(c));

            return new Reaction[]{reaction1, reaction2};
        } else if (forwardPos >= 0) {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(a));
            reaction1.getProducts().addAll(MoleculeType.valueOf(b));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(c));
            return new Reaction[]{reaction1};


        } else if (backwardPos >= 0) {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(b));
            reaction1.getProducts().addAll(MoleculeType.valueOf(a));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(c));
            return new Reaction[]{reaction1};
        } else
            return new Reaction[0];
    }
}
