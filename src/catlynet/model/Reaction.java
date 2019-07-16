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

    /**
     * parses a reaction
     * ReactionNotation:
     * name tab: reactant ... [ catalyst ...] -> product ...
     * or
     * name tab: reactant ... [ catalyst ...] <- product ...
     * or
     * name tab: reactant ... [ catalyst ...] <-> product ...
     * <p>
     * Reactants can be separated by white space or +
     * Products can be separated by white space or +
     * Catalysts can be separated by white space or , (for or), or all can be separated by & (for 'and')
     *
     * @param line
     * @return one or two reactions
     * @throws IOException
     */
    public static Reaction[] parse(String line, final Set<Reaction> auxReactions, boolean tabbedFormat) throws IOException {
        line = line.replaceAll("->", "=>").replaceAll("<-", "<=");

        if (tabbedFormat) { // name <tab>  a+b -> c <tab> catalysts
            final String[] tokens = Basic.trimAll(Basic.split(line, '\t'));
            if (tokens.length == 3) {

                int arrowStart = tokens[1].indexOf("<=");
                if (arrowStart == -1)
                    arrowStart = tokens[1].indexOf("=>");

                if (arrowStart != -1) {
                    line = tokens[0] + ": " + tokens[1].substring(0, arrowStart) + " [" + tokens[2] + "] " + tokens[1].substring(arrowStart);
                }
            }
        }

        final int colonPos = line.indexOf(':');
        final int openSquare = line.indexOf('[');
        final int closeSquare = line.indexOf(']');

        final int endArrow;
        boolean forward;
        boolean reverse;
        {
            if (line.indexOf("<=>") > 0) {
                forward = true;
                reverse = true;
                endArrow = line.indexOf("<=>") + 3;
            } else if (line.indexOf("=>") > 0) {
                forward = true;
                reverse = false;
                endArrow = line.indexOf("=>") + 2;
            } else if (line.indexOf("<=") > 0) {
                forward = false;
                reverse = true;
                endArrow = line.indexOf("<=") + 2;
            } else
                throw new IOException("Can't parse reaction: " + line);
        }

        if (!(colonPos > 0 && openSquare > colonPos && closeSquare > openSquare))
            throw new IOException("Can't parse reaction: " + line);

        final String reactionName = line.substring(0, colonPos).trim();

        final String[] reactants = Basic.trimAll(line.substring(colonPos + 1, openSquare).trim().split("[+\\s]+"));

        final String[] catalysts;
        final String catalystsString = line.substring(openSquare + 1, closeSquare).trim()
                .replaceAll(",", " ")
                .replaceAll("\\*", "&")
                .replaceAll("\\s*&\\s*", "&");

        catalysts = Basic.trimAll(catalystsString.split("\\s+"));


        final String[] products = Basic.trimAll(line.substring(endArrow + 1).trim().split("[+\\s]+"));

        final ArrayList<Reaction> result = new ArrayList<>();

        if (forward && reverse) {
            final Reaction reaction1 = new Reaction(reactionName + "+");
            reaction1.getReactants().addAll(MoleculeType.valueOf(reactants));
            reaction1.getProducts().addAll(MoleculeType.valueOf(products));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            result.add(reaction1);

            final Reaction reaction2 = new Reaction(reactionName + "-");
            reaction2.getReactants().addAll(MoleculeType.valueOf(products)); // switch roles
            reaction2.getProducts().addAll(MoleculeType.valueOf(reactants));
            reaction2.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            result.add(reaction2);
        } else if (forward) {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(reactants));
            reaction1.getProducts().addAll(MoleculeType.valueOf(products));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            result.add(reaction1);
        } else // reverse
        {
            final Reaction reaction1 = new Reaction(reactionName);
            reaction1.getReactants().addAll(MoleculeType.valueOf(products)); // switch roles
            reaction1.getProducts().addAll(MoleculeType.valueOf(reactants));
            reaction1.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
            result.add(reaction1);
        }

        for (String catalyst : catalysts) {
            if (catalyst.contains("&")) {
                final String[] foods = Basic.split(catalyst, '&');
                final Reaction auxReaction = new Reaction(reactionName + "/" + catalyst + "/");
                for (String reactantName : foods) {
                    auxReaction.getReactants().add(MoleculeType.valueOf(reactantName));
                }
                auxReaction.getCatalysts().add(MoleculeType.valueOf(catalyst));
                auxReaction.getProducts().add(MoleculeType.valueOf(catalyst));
                boolean found = false;
                for (Reaction other : auxReactions) {
                    if (other.getReactants().equals(auxReaction.getReactants()) && other.getCatalysts().equals(auxReaction.getCatalysts()) && other.getProducts().equals(auxReaction.getProducts())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    auxReactions.add(auxReaction);
                    result.add(auxReaction);
                }
            }

        }
        return result.toArray(new Reaction[0]);
    }

    @Override
    public int compareTo(Reaction that) {
        return this.getName().compareTo(that.getName());
    }
}
