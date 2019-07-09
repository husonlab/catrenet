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

package catlynet.model;

import jloda.util.Basic;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * a reaction
 * Daniel Huson, 6.2019
 */
public class Reaction implements Comparable<Reaction> {
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
        return String.format("%s : %s [%s] -> %s",
                name, Basic.toString(reactants, " + "), Basic.toString(catalysts, " "), Basic.toString(products, " + "));
    }

    public String toStringBothWays() {
        return String.format("%s : %s [%s] <-> %s",
                (name.endsWith("+") ? name.substring(0, name.length() - 1) : name), Basic.toString(reactants, " + "), Basic.toString(catalysts, " "), Basic.toString(products, " + "));
    }

    /**
     * parses a reaction
     * Format:
     * name tab: reactant ... [ catalyst ...] -> product ...
     * or
     * name tab: reactant ... [ catalyst ...] <- product ...
     * or
     * name tab: reactant ... [ catalyst ...] <-> product ...
     *
     * Reactants can be separated by white space or +
     * Products can be separated by white space or +
     * Catalysts can be separated by white space or , (for or), or all can be separated by & (for and)
     * @param line
     * @return one or two reactions
     * @throws IOException
     */
    public static Reaction[] parse(String line) throws IOException {
        final int colonPos = line.indexOf(':');
        final int openSquare = line.indexOf('[');
        final int closeSquare = line.indexOf(']');

        final int endArrow;
        boolean forward;
        boolean reverse;
        {
            if (line.indexOf("<->") > 0) {
                forward = true;
                reverse = true;
                endArrow = line.indexOf("<->") + 3;
            } else if (line.indexOf("->") > 0) {
                forward = true;
                reverse = false;
                endArrow = line.indexOf("->") + 2;
            } else if (line.indexOf("<-") > 0) {
                forward = false;
                reverse = true;
                endArrow = line.indexOf("<-") + 2;
            } else
                throw new IOException("Can't parse reaction: " + line);
        }

        if (!(colonPos > 0 && openSquare > colonPos && closeSquare > openSquare))
            throw new IOException("Can't parse reaction: " + line);

        final String reactionName = line.substring(0, colonPos).trim();

        final String[] reactants = Basic.trimAll(line.substring(colonPos + 1, openSquare).trim().split("[+\\s]+"));

        final String[] catalysts = Basic.trimAll(line.substring(openSquare + 1, closeSquare).trim().split("[,\\s]+"));

        final String[] products = Basic.trimAll(line.substring(endArrow + 1).trim().split("[+\\s]+"));

        if (forward && reverse) {
            final Reaction reaction1 = new Reaction(reactionName + "+");
            reaction1.getReactants().addAll(MoleculeType.valueOf(reactants));
            reaction1.getProducts().addAll(MoleculeType.valueOf(products));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));

            final Reaction reaction2 = new Reaction(reactionName + "-");
            reaction2.getReactants().addAll(MoleculeType.valueOf(products)); // switch roles
            reaction2.getProducts().addAll(MoleculeType.valueOf(reactants));
            reaction2.getCatalysts().addAll(MoleculeType.valueOf(catalysts));

            return new Reaction[]{reaction1, reaction2};
        } else if (forward) {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(reactants));
            reaction1.getProducts().addAll(MoleculeType.valueOf(products));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            return new Reaction[]{reaction1};
        } else // reverse
        {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(products)); // switch roles
            reaction1.getProducts().addAll(MoleculeType.valueOf(reactants));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            return new Reaction[]{reaction1};
        }
    }

    @Override
    public int compareTo(Reaction that) {
        return this.getName().compareTo(that.getName());
    }
}
