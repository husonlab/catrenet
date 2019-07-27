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
    public enum Direction {forward, reverse, both}
    private final String name;

    private final Set<MoleculeType> reactants = new TreeSet<>();
    private final Set<MoleculeType> products = new TreeSet<>();
    private final Set<MoleculeType> catalysts = new TreeSet<>();
    private final Set<MoleculeType> inhibitors = new TreeSet<>();

    private Direction direction = Direction.forward;

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

    public Set<MoleculeType> getInhibitors() {
        return inhibitors;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * parses a reaction
     * ReactionNotation:
     * name tab: reactant ... [ catalyst ...] -|inhibitor ...|- > -> product ...
     * or
     * name tab: reactant ... [ catalyst ...] -|inhibitor ...|- <- product ...
     * or
     * name tab: reactant ... [ catalyst ...] -|inhibitor ...|- <-> product ...
     * <p>
     * Reactants can be separated by white space or +
     * Products can be separated by white space or +
     * Catalysts can be separated by white space or , (for or), or all can be separated by & (for 'and')
     *
     * @param line
     * @return the reaction
     * @throws IOException
     */
    public static Reaction parse(String line, final Set<Reaction> auxReactions, boolean tabbedFormat) throws IOException {
        line = line.replaceAll("->", "=>").replaceAll("<-", "<=");

        if (tabbedFormat) { // name <tab>  a+b -> c <tab> catalysts
            final String[] tokens = Basic.trimAll(Basic.split(line, '\t'));
            if (tokens.length == 3 || tokens.length == 4) {

                int arrowStart = tokens[1].indexOf("<=");
                if (arrowStart == -1)
                    arrowStart = tokens[1].indexOf("=>");

                if (arrowStart != -1) {
                    if (tokens.length == 3)
                        line = tokens[0] + ": " + tokens[1].substring(0, arrowStart) + " [" + tokens[2] + "] " + tokens[1].substring(arrowStart);
                    else // tokens.length==4
                        line = tokens[0] + ": " + tokens[1].substring(0, arrowStart) + " [" + tokens[2] + "] (" + tokens[3] + ") " + tokens[1].substring(arrowStart);
                }
            }
        }

        final int colonPos = line.indexOf(':');
        final int openSquareBracket = line.indexOf('[');
        final int closeSquareBracket = line.indexOf(']');

        final int openRoundBracket = line.indexOf("(");
        final int closeRoundBracket = line.indexOf(")");

        final int endArrow;

        final Reaction.Direction direction;
        {
            if (line.indexOf("<=>") > 0) {
                direction = Direction.both;
                endArrow = line.indexOf("<=>") + 2;
            } else if (line.indexOf("=>") > 0) {
                direction = Direction.forward;
                endArrow = line.indexOf("=>") + 1;
            } else if (line.indexOf("<=") > 0) {
                direction = Direction.reverse;
                endArrow = line.indexOf("<=") + 1;
            } else
                throw new IOException("Can't parse reaction: " + line);
        }

        if (!(colonPos > 0 && openSquareBracket > colonPos && closeSquareBracket > openSquareBracket))
            throw new IOException("Can't parse reaction: " + line);

        final String reactionName = line.substring(0, colonPos).trim();

        final String[] reactants = Basic.trimAll(line.substring(colonPos + 1, openSquareBracket).trim().split("[+\\s]+"));

        final String[] catalysts;
        {
            final String catalystsString = line.substring(openSquareBracket + 1, closeSquareBracket).trim()
                    .replaceAll(",", " ")
                    .replaceAll("\\*", "&")
                    .replaceAll("\\s*&\\s*", "&");
            catalysts = Basic.trimAll(catalystsString.split("\\s+"));
        }

        final String[] inhibitors;
        if (openRoundBracket != -1 && closeRoundBracket != -1) {
            final String inhibitorsString = line.substring(openRoundBracket + 1, closeRoundBracket).trim().replaceAll(",", " ");
            inhibitors = Basic.trimAll(inhibitorsString.split("\\s+"));
        } else if ((openRoundBracket >= 0) != (closeRoundBracket >= 0))
            throw new IOException("Can't parse reaction: " + line);
        else
            inhibitors = new String[0];

        final String[] products = Basic.trimAll(line.substring(endArrow + 1).trim().split("[+\\s]+"));

        final Reaction reaction = new Reaction(reactionName);
        reaction.getReactants().addAll(MoleculeType.valueOf(reactants));
        reaction.getProducts().addAll(MoleculeType.valueOf(products));
        reaction.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
        reaction.getInhibitors().addAll(MoleculeType.valueOf(inhibitors));
        reaction.setDirection(direction);
        return reaction;
    }

    @Override
    public int compareTo(Reaction that) {
        return this.getName().compareTo(that.getName());
    }

    public String toString() {
        return getName();
    }

    /**
     * creates the reverse reaction (and adds a - to the name) as a forward reaction by swapping reactants and products
     *
     * @return reverse reaction
     */
    public Reaction createReverse() {
        final Reaction reverse = new Reaction(getName() + "-");
        reverse.getReactants().addAll(getProducts());
        reverse.getProducts().addAll(getReactants());
        reverse.getCatalysts().addAll(getCatalysts());
        reverse.getInhibitors().addAll(getInhibitors());
        return reverse;
    }

    /**
     * creates the forward reaction (and adds a + to the name)
     *
     * @return forward reaction
     */
    public Reaction createForward() {
        final Reaction forward = new Reaction(getName() + "+");
        forward.getReactants().addAll(getReactants());
        forward.getProducts().addAll(getProducts());
        forward.getCatalysts().addAll(getCatalysts());
        forward.getInhibitors().addAll(getInhibitors());
        return forward;
    }

    /**
     * creates the both reaction (and removes a trailing + from the name)
     *
     * @return both reaction
     */
    public Reaction createBoth() {
        if (!getName().endsWith("+"))
            throw new IllegalArgumentException("name must end on '+'");
        final Reaction both = new Reaction(getName().substring(0, getName().length() - 1));
        both.getReactants().addAll(getReactants());
        both.getProducts().addAll(getProducts());
        both.getCatalysts().addAll(getCatalysts());
        both.getInhibitors().addAll(getInhibitors());
        both.setDirection(Direction.both);
        return both;
    }
}
