/*
 * Model.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import jloda.util.Basic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a catalytic reaction system
 * Daniel Huson, 6.2019
 */
public class ReactionSystem {
     public enum Type {Input, maxCAF, maxRAF, maxPseudoRAF, Reactions, muCAF, uRAF, minIrrRAF}

    private final ObservableList<Reaction> reactions = FXCollections.observableArrayList();
    private final ObservableList<MoleculeType> foods = FXCollections.observableArrayList();

    private final BooleanProperty inhibitorsPresent = new SimpleBooleanProperty(false);

    private final IntegerProperty size = new SimpleIntegerProperty();

    private int numberOfTwoWayReactions = 0;

    private final StringProperty name = new SimpleStringProperty(Type.Reactions.toString());

    /**
     * construct a reactions systems
     */
    public ReactionSystem() {
        this(null);
    }

    /**
     * construct a reactions systems
     */
    public ReactionSystem(final String name) {
        size.bind(Bindings.size(reactions));
        setName(name);

        reactions.addListener((ListChangeListener<Reaction>) e -> {
            while (e.next()) {
                for (Reaction reaction : e.getAddedSubList()) {
                    if (reaction.getDirection() == Reaction.Direction.both)
                        numberOfTwoWayReactions++;
                }
                for (Reaction reaction : e.getRemoved()) {
                    if (reaction.getDirection() == Reaction.Direction.both)
                        numberOfTwoWayReactions--;
                }
            }
        });
        updateIsInhibitorsPresent();
    }

    public ObservableList<Reaction> getReactions() {
        return reactions;
    }

    public ObservableList<MoleculeType> getFoods() {
        return foods;
    }

    /**
     * create a shallow copy that references reactions
     *
     * @return shallow copy of this model
     */
    public ReactionSystem shallowCopy() {
        final ReactionSystem result = new ReactionSystem();
        result.shallowCopy(this);
        return result;
    }

    /**
     * sets this to a shallow copy of that
     *
     * @param that
     */
    public void shallowCopy(ReactionSystem that) {
        clear();
        setName(that.getName());
        foods.addAll(that.foods);
        reactions.addAll(that.reactions);
    }

    public void clear() {
        reactions.clear();
        foods.clear();
        name.set(Type.Reactions.toString());
    }

    /**
     * gets the size
     *
     * @return number of reactions
     */
    public int size() {
        return size.get();
    }

    /**
     * size property
     *
     * @return size
     */
    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }

    public int getNumberOfTwoWayReactions() {
        return numberOfTwoWayReactions;
    }

    public int getNumberOfOneWayReactions() {
        return size() - numberOfTwoWayReactions;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public boolean isInhibitorsPresent() {
        return inhibitorsPresent.get();
    }

    public ReadOnlyBooleanProperty inhibitorsPresentProperty() {
        return inhibitorsPresent;
    }

    public void updateIsInhibitorsPresent() {
        for (Reaction reaction : reactions) {
            if (reaction.getInhibitions().size() > 0) {
                inhibitorsPresent.set(true);
                return;
            }
        }
        inhibitorsPresent.set(false);
    }

    /**
     * computes the expanded model in which each bi-direction reaction is replaced by two one-way reactions and
     * for each 'and' set of catalysts the correspond enforcing reaction has been added.
     * Unused food items are removed.
     *
     * @return expanded reaction system
     */
    public ReactionSystem computeExpandedSystem() {
        final ReactionSystem expanded = new ReactionSystem();
        expanded.setName(getName() + " (expanded)");

        final Set<MoleculeType> mentionedMolecules = new HashSet<>();

        final ArrayList<Reaction> auxiliaryReactions = new ArrayList<>();

        for (Reaction reaction : getReactions()) {
            {
                final String name = Basic.toString(reaction.getCatalysts(), ",");
                if (name.contains("&") || name.contains("(")) {
                    reaction = new Reaction(reaction);
                    reaction.getCatalysts().clear();
                    for (String part : DisjunctiveNormalForm.compute(name).split(","))
                        reaction.getCatalysts().add(MoleculeType.valueOf(part));
                }
            }

            switch (reaction.getDirection()) {
                case forward: {
                    expanded.getReactions().add(reaction); // don't use create forward, as that changes the name
                    break;
                }
                case reverse: {
                    expanded.getReactions().add(reaction.createReverse());
                    break;
                }
                case both: {
                    expanded.getReactions().add(reaction.createForward());
                    expanded.getReactions().add(reaction.createReverse());
                    break;
                }
            }

            for (MoleculeType catalyst : reaction.getCatalysts()) {
                final String name = catalyst.getName();
                if (name.contains("&")) {
                    final String[] molecules = Basic.split(name, '&');
                    final Reaction auxReaction = new Reaction(reaction.getName() + "/" + name + "/");
                    for (String reactantName : molecules) {
                        auxReaction.getReactants().add(MoleculeType.valueOf(reactantName));
                    }
                    mentionedMolecules.addAll(MoleculeType.valueOf(molecules));

                    auxReaction.getCatalysts().add(catalyst);
                    auxReaction.getProducts().add(catalyst);
                    boolean found = false;
                    for (Reaction other : auxiliaryReactions) {
                        if (other.getReactants().equals(auxReaction.getReactants()) && other.getCatalysts().equals(auxReaction.getCatalysts()) && other.getProducts().equals(auxReaction.getProducts())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        auxiliaryReactions.add(auxReaction);
                        expanded.getReactions().add(auxReaction);
                    }
                }
            }
            mentionedMolecules.addAll(reaction.getReactants());
            mentionedMolecules.addAll(reaction.getProducts());
            mentionedMolecules.addAll(reaction.getCatalysts());
            mentionedMolecules.addAll(reaction.getInhibitions());
        }

        expanded.getFoods().setAll(Basic.intersection(getFoods(), mentionedMolecules));

        return expanded;
    }

    /**
     * gets the compressed system (opposite of expanded)
     *
     * @return compressed reaction system
     */
    public ReactionSystem getCompressedSystem() {
        final ReactionSystem compressed = new ReactionSystem();
        compressed.setName(getName().replaceAll(" (expanded)", ""));
        compressed.getFoods().setAll(getFoods());
        for (Reaction reaction : reactions) {
            if (reaction.getName().endsWith("+")) {
                compressed.getReactions().add(reaction.createBoth());
            } else if (!reaction.getName().endsWith("-") && !reaction.getName().endsWith("/"))
                compressed.getReactions().add(reaction);
        }
        return compressed;
    }

    /**
     * gets all mentioned molecule types
     *
     * @return
     */
    public Set<MoleculeType> getMoleculeTypes(boolean foodSet, boolean reactants, boolean products, boolean catalysts, boolean inhibitors) {
        final Set<MoleculeType> moleculeTypes = new TreeSet<>();
        if (foodSet)
            moleculeTypes.addAll(getFoods());
        for (Reaction reaction : getReactions()) {
            if (reactants)
                moleculeTypes.addAll(reaction.getReactants());
            if (products)
                moleculeTypes.addAll(reaction.getProducts());
            if (catalysts)
                moleculeTypes.addAll(reaction.getCatalysts());
            if (inhibitors)
                moleculeTypes.addAll(reaction.getInhibitions());
        }
        return moleculeTypes;
    }

    public Set<String> getReactionNames() {
        final Set<String> names = new TreeSet<>();
        for (Reaction reaction : getReactions()) {
            names.add(reaction.getName());
        }
        return names;
    }

    public Collection<? extends MoleculeType> computeMentionedFoods(Collection<MoleculeType> foods) {
        final Set<MoleculeType> set = new HashSet<>();
        reactions.forEach(r -> {
            set.addAll(r.getReactants());
            set.addAll(r.getCatalysts());
            set.addAll(r.getInhibitions());
            set.addAll(r.getProducts());
        });
        return foods.stream().filter(set::contains).collect(Collectors.toList());
    }

    public Reaction getReaction(String name) {
        final Optional<Reaction> result = getReactions().stream().filter(r -> r.getName().equals(name)).findAny();
        return result.orElse(null);
    }

    public void replaceNamedReaction(String name, Reaction reaction) {
        final Reaction old = getReaction(name);
        if (old == null)
            throw new IllegalArgumentException("no such reaction: " + name);
        getReactions().remove(old);
        getReactions().add(reaction);
    }
}
