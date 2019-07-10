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
import java.util.ArrayList;
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

    private boolean catalystsAnd = false;

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

    public boolean isCatalystsAnd() {
        return catalystsAnd;
    }

    public void setCatalystsAnd(boolean catalystsAnd) {
        this.catalystsAnd = catalystsAnd;
    }

    public String toString() {
        return String.format("%s : %s [%s] -> %s",
                name, Basic.toString(reactants, " + "), Basic.toString(catalysts, catalystsAnd ? " & " : " "), Basic.toString(products, " + "));
    }

    public String toStringBothWays() {
        return String.format("%s : %s [%s] <-> %s",
                (name.endsWith("+") ? name.substring(0, name.length() - 1) : name), Basic.toString(reactants, " + "), Basic.toString(catalysts, catalystsAnd ? " & " : " "), Basic.toString(products, " + "));
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
     * Catalysts can be separated by white space or , (for or), or all can be separated by & (for 'and')
     * @param line
     * @return one or two reactions
     * @throws IOException
     */
    public static Reaction[] parse(String line, final Set<Reaction> auxReactions) throws IOException {
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

        final boolean catalystsAnd;
        final String[] catalysts;
        final String catalystsString = line.substring(openSquare + 1, closeSquare).trim();
        if (catalystsString.contains("&")) {
            catalysts = Basic.trimAll(line.substring(openSquare + 1, closeSquare).trim().split("[\\s]*&[\\s]*"));
            for (String catalyst : catalysts) {
                if (catalyst.replaceAll("\\s+", " ").contains(" ")) {
                    throw new IOException("Catalyst contains both 'or' and 'and', not implemented: " + line);
                }
            }
            catalystsAnd = true;
        } else {
            catalysts = Basic.trimAll(line.substring(openSquare + 1, closeSquare).trim().split("[,\\s]+"));
            catalystsAnd = false;
        }

        final String[] products = Basic.trimAll(line.substring(endArrow + 1).trim().split("[+\\s]+"));

        final ArrayList<Reaction> result = new ArrayList<>();

        if (forward && reverse) {
            final Reaction reaction1 = new Reaction(reactionName + "+");
            reaction1.getReactants().addAll(MoleculeType.valueOf(reactants));
            reaction1.getProducts().addAll(MoleculeType.valueOf(products));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            reaction1.setCatalystsAnd(catalystsAnd);
            result.add(reaction1);

            final Reaction reaction2 = new Reaction(reactionName + "-");
            reaction2.getReactants().addAll(MoleculeType.valueOf(products)); // switch roles
            reaction2.getProducts().addAll(MoleculeType.valueOf(reactants));
            reaction2.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            reaction2.setCatalystsAnd(catalystsAnd);
            result.add(reaction2);
        } else if (forward) {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(reactants));
            reaction1.getProducts().addAll(MoleculeType.valueOf(products));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            reaction1.setCatalystsAnd(catalystsAnd);
            result.add(reaction1);
        } else // reverse
        {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(products)); // switch roles
            reaction1.getProducts().addAll(MoleculeType.valueOf(reactants));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            reaction1.setCatalystsAnd(catalystsAnd);
            result.add(reaction1);

        }

        if (catalystsAnd) {
            final Reaction auxReaction = new Reaction(reactionName + "/");
            auxReaction.getReactants().addAll(MoleculeType.valueOf(catalysts));
            boolean found = false;
            for (Reaction other : auxReactions) {
                if (other.getReactants().equals(auxReaction.getReactants())) {
                    found = true;
                    break;
                }

            }
            if (!found) {
                final String auxMolecule = "/" + (auxReactions.size() + 1);
                auxReaction.getProducts().add(MoleculeType.valueOf(auxMolecule));
                auxReaction.getCatalysts().add(MoleculeType.valueOf(auxMolecule));
                auxReactions.add(auxReaction);
                result.add(auxReaction);
            }
        }
        return result.toArray(new Reaction[0]);
    }

    @Override
    public int compareTo(Reaction that) {
        return this.getName().compareTo(that.getName());
    }
}
