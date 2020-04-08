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

import jloda.fx.window.NotificationManager;
import jloda.util.Basic;

import java.io.IOException;
import java.util.*;

/**
 * a reaction
 * Daniel Huson, 6.2019
 */
public class Reaction implements Comparable<Reaction> {
    public enum Direction {forward, reverse, both}

    private static boolean warnedAboutSuppressingCoefficients = false;

    private final String name;

    private final Set<MoleculeType> reactants = new TreeSet<>();
    private final Set<MoleculeType> products = new TreeSet<>();
    private final TreeSet<MoleculeType> catalysts = new TreeSet<>();
    private final Set<MoleculeType> inhibitions = new TreeSet<>();

    private final Map<MoleculeType, Integer> reactantCoefficient = new HashMap<>();
    private final Map<MoleculeType, Integer> productCoefficient = new HashMap<>();

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
     * copy constructor
     *
     * @param src
     */
    public Reaction(Reaction src) {
        this(src.getName());
        reactants.addAll(src.getReactants());
        products.addAll(src.getProducts());
        catalysts.addAll(src.getCatalysts());
        inhibitions.addAll(src.getInhibitions());
        direction = src.getDirection();
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

    public TreeSet<MoleculeType> getCatalysts() {
        return catalysts;
    }

    public Set<MoleculeType> getCatalystConjunctions() {
           final Set<MoleculeType> set = new TreeSet<>();
            for (MoleculeType catalyst : getCatalysts()) {
                final String string = catalyst.getName();
                    final String dnf = DisjunctiveNormalForm.compute(string);
                    for (String part : dnf.split(",")) {
                        set.add(MoleculeType.valueOf(part));
                    }
            }
            return set;
    }

    public Set<MoleculeType> getInhibitions() {
        return inhibitions;
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
     * name tab: [coefficient] reactant ... '[' catalyst ...']'  ['{' inhibitor ... '}'] -> [coefficient] product ...
     * or
     * name tab: [coefficient] reactant ... '[' catalyst ... ']'  ['{' inhibitor ... '}'] <- [coefficient] product ...
     * or
     * name tab: [coefficient] reactant ... '[' catalyst ... ']' ['{' inhibitor ... '}'] <-> [coefficient] product ...
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
                        line = tokens[0] + ": " + tokens[1].substring(0, arrowStart) + " [" + tokens[2] + "] {" + tokens[3] + "} " + tokens[1].substring(arrowStart);
                }
            }
        }

        final int colonPos = line.indexOf(':');
        final int openSquareBracket = line.indexOf('[');
        final int closeSquareBracket = line.indexOf(']');

        final int openCurlyBracket = line.indexOf("{");
        final int closeCurlyBracket = line.indexOf("}");

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
            String catalystsString = line.substring(openSquareBracket + 1, closeSquareBracket).trim()
                    .replaceAll("\\s*,\\s*", ",")
                    .replaceAll("\\*", "&")
                    .replaceAll("\\|", ",")
                    .replaceAll("\\s*&\\s*", "&");

            if (!catalystsString.contains("(") && !catalystsString.contains("&"))
                catalystsString = catalystsString.replaceAll(",", " ");
            catalysts = Arrays.stream(Basic.trimAll(catalystsString.split("\\s+"))).map(String::trim).filter(s -> s.length() > 0).toArray(String[]::new);
        }

        final String[] inhibitors;
        if (openCurlyBracket != -1 && closeCurlyBracket != -1) {
            final String inhibitorsString = line.substring(openCurlyBracket + 1, closeCurlyBracket).trim().replaceAll(",", " ");
            inhibitors = Basic.trimAll(inhibitorsString.split("\\s+"));
        } else if ((openCurlyBracket >= 0) != (closeCurlyBracket >= 0))
            throw new IOException("Can't parse reaction: " + line);
        else
            inhibitors = new String[0];

        final String[] products = Basic.trimAll(line.substring(endArrow + 1).trim().split("[+\\s]+"));

        final Reaction reaction = new Reaction(reactionName);

        if (Arrays.stream(reactants).allMatch(Basic::isDouble)) { // all tokens look like numbers, don't allow coefficients
            reaction.getReactants().addAll(MoleculeType.valueOf(reactants));
        } else { // some tokens are not numbers, assume this is mix of coefficients and reactants
            int coefficient = -1;
            for (String token : reactants) {
                if (Basic.isInteger(token)) {
                    if (coefficient == -1)
                        coefficient = Basic.parseInt(token);
                    else
                        throw new IOException("Can't distinguish between coefficients and reactant names : " + Basic.toString(reactants, " "));
                } else {
                    if (coefficient == -1 || coefficient > 0)
                        reaction.getReactants().add(MoleculeType.valueOf(token));
                    if (coefficient > 0) {
                        reaction.setReactantCoefficient(MoleculeType.valueOf(token), coefficient);
                        if (!warnedAboutSuppressingCoefficients) {
                            NotificationManager.showWarning("Any coefficients found in reactions are ignored");
                            warnedAboutSuppressingCoefficients = true;
                        }
                    }
                    coefficient = -1;
                }
                if (coefficient == -1 && Basic.isInteger(token))
                    coefficient = Basic.parseInt(token);
            }
            if (coefficient != -1)
                throw new IOException("Can't distinguish between coefficients and reactant names : " + Basic.toString(reactants, " "));
        }

        if (Arrays.stream(products).allMatch(Basic::isDouble)) { // all tokens look like numbers, don't allow coefficients
            reaction.getProducts().addAll(MoleculeType.valueOf(products));
        } else { // some tokens are not numbers, assume this is mix of coefficients and reactants
            int coefficient = -1;
            for (String token : products) {
                if (Basic.isInteger(token)) {
                    if (coefficient == -1)
                        coefficient = Basic.parseInt(token);
                    else
                        throw new IOException("Can't distinguish between coefficients and product names : " + Basic.toString(products, " "));
                } else {
                    if (coefficient == -1 || coefficient > 0)
                        reaction.getProducts().add(MoleculeType.valueOf(token));
                    if (coefficient > 0) {
                        reaction.setProductCoefficient(MoleculeType.valueOf(token), coefficient);
                        if (!warnedAboutSuppressingCoefficients) {
                            NotificationManager.showWarning("Any coefficients found in reactions are ignored");
                            warnedAboutSuppressingCoefficients = true;
                        }
                    }
                    coefficient = -1;
                }
                if (coefficient == -1 && Basic.isInteger(token))
                    coefficient = Basic.parseInt(token);
            }
            if (coefficient != -1)
                throw new IOException("Can't distinguish between coefficients and product names : " + Basic.toString(products, " "));
        }
        reaction.getCatalysts().addAll(MoleculeType.valueOf(catalysts));
        reaction.getInhibitions().addAll(MoleculeType.valueOf(inhibitors));
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
        reverse.getInhibitions().addAll(getInhibitions());
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
        forward.getInhibitions().addAll(getInhibitions());
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
        both.getInhibitions().addAll(getInhibitions());
        both.setDirection(Direction.both);
        return both;
    }

    public int getReactantCoefficient(MoleculeType reactant) {
        return reactantCoefficient.getOrDefault(reactant, 1);
    }

    public void setReactantCoefficient(MoleculeType reactant, int coefficient) {
        reactantCoefficient.put(reactant, coefficient);
    }

    public int getProductCoefficient(MoleculeType product) {
        return productCoefficient.getOrDefault(product, 1);
    }

    public void setProductCoefficient(MoleculeType product, int coefficient) {
        productCoefficient.put(product, coefficient);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reaction)) return false;
        Reaction reaction = (Reaction) o;
        return name.equals(reaction.name) &&
                reactants.equals(reaction.reactants) &&
                products.equals(reaction.products) &&
                catalysts.equals(reaction.catalysts) &&
                inhibitions.equals(reaction.inhibitions) &&
                reactantCoefficient.equals(reaction.reactantCoefficient) &&
                productCoefficient.equals(reaction.productCoefficient) &&
                direction == reaction.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reactants, products, catalysts, inhibitions, reactantCoefficient, productCoefficient, direction);
    }
}
