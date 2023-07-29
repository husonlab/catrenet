/*
 * Reaction.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.fx.window.NotificationManager;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static catlynet.io.ModelIO.FORMAL_FOOD;

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
    private String catalysts = "";
    private final Set<MoleculeType> inhibitions = new TreeSet<>();

    private final Map<MoleculeType, Integer> reactantCoefficient = new HashMap<>();
    private final Map<MoleculeType, Integer> productCoefficient = new HashMap<>();

    private Direction direction = Direction.forward;

    /**
     * constructor
     *
	 */
    public Reaction(String name) {
        this.name = name;
    }

    /**
     * copy constructor
     *
	 */
    public Reaction(Reaction src) {
        this(src.getName());
        reactants.addAll(src.getReactants());
        products.addAll(src.getProducts());
        catalysts = src.getCatalysts();
        inhibitions.addAll(src.getInhibitions());
        direction = src.getDirection();
    }


    /**
     * copy constructor
     *
	 */
    public Reaction(String name, Reaction src) {
        this(name);
        reactants.addAll(src.getReactants());
        products.addAll(src.getProducts());
        catalysts = src.getCatalysts();
        inhibitions.addAll(src.getInhibitions());
        direction = src.getDirection();
        productCoefficient.putAll(src.productCoefficient);
    }


    public boolean isCatalyzedAndUninhibitedAndHasAllReactants(Collection<MoleculeType> food, Direction direction) {
        return (((direction == Direction.forward || direction == Direction.both) && food.containsAll(getReactants()))
                || ((direction == Direction.reverse || direction == Direction.both) && food.containsAll(getProducts())))
               && (getCatalysts().length() == 0 || getCatalystConjunctions().stream().map(m -> MoleculeType.valuesOf(m.getName().split("&"))).anyMatch(food::containsAll))
               && (getInhibitions().size() == 0 || getInhibitions().stream().noneMatch(food::contains));
    }

    public boolean isCatalyzedAndUninhibitedAndHasAllReactants(Collection<MoleculeType> foodForReactants, Collection<MoleculeType> foodForCatalysts, Collection<MoleculeType> foodForInhibitors, Direction direction) {
        return (((direction == Direction.forward || direction == Direction.both) && foodForReactants.containsAll(getReactants()))
                || ((direction == Direction.reverse || direction == Direction.both) && foodForReactants.containsAll(getProducts())))
               && (getCatalysts().length() == 0 || getCatalystConjunctions().stream().map(m -> MoleculeType.valuesOf(m.getName().split("&"))).anyMatch(foodForCatalysts::containsAll))
               && (getInhibitions().size() == 0 || getInhibitions().stream().noneMatch(foodForReactants::contains));
    }

    public boolean isHasAllReactants(Collection<MoleculeType> food, Direction direction) {
        return (((direction == Direction.forward || direction == Direction.both) && food.containsAll(getReactants()))
                || ((direction == Direction.reverse || direction == Direction.both) && food.containsAll(getProducts())));
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
     * @return the reaction
	 */
    public static Reaction parse(String line, final Set<Reaction> auxReactions, boolean tabbedFormat) throws IOException {
        line = line.replaceAll("->", "=>").replaceAll("<-", "<=");

        if (tabbedFormat) { // name <tab>  a+b -> c <tab> catalysts
			final String[] tokens = StringUtils.trimAll(StringUtils.split(line, '\t'));
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

        final var colonPos = line.indexOf(':');
        if (colonPos == -1)
            throw new IOException("Can't parse reaction: " + line);

        final var openSquareBracket = line.indexOf('[');
        if (openSquareBracket != -1 && openSquareBracket < colonPos)
            throw new IOException("Can't parse reaction: " + line);

        final var closeSquareBracket = line.indexOf(']');

        if ((openSquareBracket == -1 && closeSquareBracket != -1) || (openSquareBracket != -1 && closeSquareBracket < openSquareBracket))
            throw new IOException("Can't parse reaction: " + line);

        final var openCurlyBracket = line.indexOf("{");
        final var closeCurlyBracket = line.indexOf("}");

        final int startArrow;
        final int endArrow;

        final Reaction.Direction direction;
        {
            if (line.indexOf("<=>") > 0) {
                direction = Direction.both;
                startArrow = line.indexOf("<=>");
                endArrow = startArrow + 2;
            } else if (line.indexOf("=>") > 0) {
                direction = Direction.forward;
                startArrow = line.indexOf("=>");
                endArrow = startArrow + 1;
            } else if (line.indexOf("<=") > 0) {
                direction = Direction.reverse;
                startArrow = line.indexOf("<=");
                endArrow = startArrow + 1;
            } else
                throw new IOException("Can't parse reaction: " + line);
        }

        final var reactionName = line.substring(0, colonPos).trim();

        var endOfReactants = (openSquareBracket != -1 ? openSquareBracket : startArrow);
        final var reactants = StringUtils.trimAll(line.substring(colonPos + 1, endOfReactants).trim().split("[+\\s]+"));

        final var catalysts = (openSquareBracket == -1 ? FORMAL_FOOD.getName() : line.substring(openSquareBracket + 1, closeSquareBracket).trim()
                .replaceAll("\\|", ",")
                .replaceAll("\\*", "&")
                .replaceAll("\\s*\\(\\s*", "(")
                .replaceAll("\\s*\\)\\s*", ")")
                .replaceAll("\\s*&\\s*", "&")
                .replaceAll("\\s*,\\s*", ",")
                .replaceAll("\\s+", ","));

        final String[] inhibitors;
        if (openCurlyBracket != -1 && closeCurlyBracket != -1) {
            final var inhibitorsString = line.substring(openCurlyBracket + 1, closeCurlyBracket).trim().replaceAll(",", " ");
			inhibitors = StringUtils.trimAll(inhibitorsString.split("\\s+"));
        } else if ((openCurlyBracket >= 0) != (closeCurlyBracket >= 0))
            throw new IOException("Can't parse reaction: " + line);
        else
            inhibitors = new String[0];

        final var products = StringUtils.trimAll(line.substring(endArrow + 1).trim().split("[+\\s]+"));

        final var reaction = new Reaction(reactionName);

        if (Arrays.stream(reactants).allMatch(NumberUtils::isDouble)) { // all tokens look like numbers, don't allow coefficients
            reaction.getReactants().addAll(MoleculeType.valuesOf(reactants));
        } else { // some tokens are not numbers, assume this is mix of coefficients and reactants
            var coefficient = -1;
            for (var token : reactants) {
                if (NumberUtils.isInteger(token)) {
                    if (coefficient == -1)
                        coefficient = NumberUtils.parseInt(token);
                    else
                        throw new IOException("Can't distinguish between coefficients and reactant names : " + StringUtils.toString(reactants, " "));
                } else {
                    if (coefficient == -1 || coefficient > 0)
                        reaction.getReactants().add(MoleculeType.valueOf(token));
                    if (coefficient > 0) {
                        reaction.setReactantCoefficient(MoleculeType.valueOf(token), coefficient);
                        if (!warnedAboutSuppressingCoefficients) {
                            NotificationManager.showWarning("Coefficients found in reactions, ignored");
                            warnedAboutSuppressingCoefficients = true;
                        }
                    }
                    coefficient = -1;
                }
                if (coefficient == -1 && NumberUtils.isInteger(token))
                    coefficient = NumberUtils.parseInt(token);
            }
            if (coefficient != -1)
				throw new IOException("Can't distinguish between coefficients and reactant names : " + StringUtils.toString(reactants, " "));
        }

        if (Arrays.stream(products).allMatch(NumberUtils::isDouble)) { // all tokens look like numbers, don't allow coefficients
            reaction.getProducts().addAll(MoleculeType.valuesOf(products));
        } else { // some tokens are not numbers, assume this is mix of coefficients and reactants
            var coefficient = -1;
            for (var token : products) {
                if (NumberUtils.isInteger(token)) {
                    if (coefficient == -1)
                        coefficient = NumberUtils.parseInt(token);
                    else
                        throw new IOException("Can't distinguish between coefficients and product names : " + StringUtils.toString(products, " "));
                } else {
                    if (coefficient == -1 || coefficient > 0)
                        reaction.getProducts().add(MoleculeType.valueOf(token));
                    if (coefficient > 0) {
                        reaction.setProductCoefficient(MoleculeType.valueOf(token), coefficient);
                        if (!warnedAboutSuppressingCoefficients) {
                            NotificationManager.showWarning("Coefficients found in reactions, ignored");
                            warnedAboutSuppressingCoefficients = true;
                        }
                    }
                    coefficient = -1;
                }
                if (coefficient == -1 && NumberUtils.isInteger(token))
                    coefficient = NumberUtils.parseInt(token);
            }
            if (coefficient != -1)
				throw new IOException("Can't distinguish between coefficients and product names : " + StringUtils.toString(products, " "));
        }
        reaction.setCatalysts(catalysts);
        reaction.getInhibitions().addAll(MoleculeType.valuesOf(inhibitors));
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

    public String getCatalysts() {
        return catalysts;
    }

    public void setCatalysts(String catalysts) {
        this.catalysts = (catalysts != null ? catalysts : "");
    }

    public Set<MoleculeType> getCatalystConjunctions() {
        final var set = new TreeSet<MoleculeType>();
        final var dnf = DisjunctiveNormalForm.compute(getCatalysts());
        for (var part : dnf.split(",")) {
            set.add(MoleculeType.valueOf(part));
        }
        return set;
    }

    public Set<MoleculeType> getCatalystElements() {
		return getCatalystConjunctions().parallelStream().map(c -> MoleculeType.valuesOf(StringUtils.split(c.getName(), '&'))).flatMap(Collection::stream).collect(Collectors.toSet());
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

    public List<Reaction> allAsForward() {
        return switch (getDirection()) {
            case forward -> {
                var forward = new Reaction(Reaction.this.name, this);
                yield List.of(forward);
            }
            case reverse -> {
                var reverse = new Reaction(Reaction.this.name, this);
                reverse.swapReactantsAndProducts();
                yield List.of(reverse);
            }
            case both -> {
                var forward = new Reaction(Reaction.this.name + "[+]", this);
                var reverse = new Reaction(Reaction.this.name + "[-]", this);
                reverse.swapReactantsAndProducts();
                yield List.of(forward, reverse);
            }
        };
    }

    private void swapReactantsAndProducts() {
        var products = new ArrayList<>(getProducts());
        getProducts().clear();
        getProducts().addAll(getReactants());
        getReactants().clear();
        getReactants().addAll(products);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reaction reaction)) return false;
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
