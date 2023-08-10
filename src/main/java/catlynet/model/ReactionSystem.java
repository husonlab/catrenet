/*
 * ReactionSystem.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import jloda.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a catalytic reaction system
 * Daniel Huson, 6.2019
 */
public class ReactionSystem {
    private final ObservableList<Reaction> reactions = FXCollections.observableArrayList();
    private final ObservableList<MoleculeType> foods = FXCollections.observableArrayList();

    private final BooleanProperty inhibitorsPresent = new SimpleBooleanProperty(false);

    private final IntegerProperty size = new SimpleIntegerProperty();
    private final IntegerProperty foodSize = new SimpleIntegerProperty();

    private int numberOfTwoWayReactions = 0;

    private final StringProperty name = new SimpleStringProperty("Reactions");

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
        foodSize.bind(Bindings.size(foods));

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

    public int getFoodSize() {
        return foodSize.get();
    }

    public IntegerProperty foodSizeProperty() {
        return foodSize;
    }

    public void setFoodSize(int foodSize) {
        this.foodSize.set(foodSize);
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

    public String getHeaderLine() {
        var buf = new StringBuilder(getName() + " has " + size());
        if (getNumberOfOneWayReactions() == 0 && getNumberOfTwoWayReactions() > 0)
            buf.append(" two-way reactions");
        else if (getNumberOfOneWayReactions() > 0 && getNumberOfTwoWayReactions() == 0)
            buf.append(" one-way reactions");
        else if (getNumberOfOneWayReactions() > 0 && getNumberOfTwoWayReactions() > 0)
            buf.append(" reactions (").append(getNumberOfTwoWayReactions()).append(" two-way and ").append(getNumberOfOneWayReactions()).append(" one-way)");
        else buf.append(" reactions");
        buf.append(" on ").append(getFoods().size()).append(" food items");
        return buf.toString();

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
     * gets all mentioned molecule types
     *
	 */
    public Set<MoleculeType> getFoodAndReactantAndProductMolecules() {
        final Set<MoleculeType> moleculeTypes = new TreeSet<>(getFoods());
        for (Reaction reaction : getReactions()) {
            moleculeTypes.addAll(reaction.getReactants());
            moleculeTypes.addAll(reaction.getProducts());
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

    public Collection<MoleculeType> computeMentionedFoods(Collection<MoleculeType> foods) {
        final Set<MoleculeType> set = new HashSet<>();
        reactions.forEach(r -> {
            set.addAll(r.getReactants());
            set.addAll(r.getInhibitions());
            set.addAll(r.getProducts());
            set.addAll(r.getCatalystConjunctions().stream().map(c -> MoleculeType.valuesOf(c.getName().split("&"))).flatMap(Collection::stream).collect(Collectors.toSet()));
        });
        return foods.parallelStream().filter(set::contains).collect(Collectors.toList());
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

	public ReactionSystem sorted() {
		final ReactionSystem reactionSystem = new ReactionSystem(getName());
		reactionSystem.getFoods().addAll(new TreeSet<>(getFoods()));
		reactionSystem.getReactions().addAll(new TreeSet<>(getReactions()));
		return reactionSystem;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ReactionSystem that)) return false;
		return CollectionUtils.equalsAsSets(this.getFoods(), that.getFoods()) && CollectionUtils.equalsAsSets(this.getReactions(), that.getReactions());
	}

	@Override
	public int hashCode() {
		return Objects.hash(reactions, foods);
	}
}
