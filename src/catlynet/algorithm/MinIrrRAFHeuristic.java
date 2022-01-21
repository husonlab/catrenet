/*
 * MinIrrRAFHeuristic.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.algorithm;

import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.util.CanceledException;
import jloda.util.CollectionUtils;
import jloda.util.Single;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

/**
 * heuristically tries to compute a minimum irreducible RAD
 * Daniel Huson, 3.2020
 * Based on notes by Mike Steel
 */
public class MinIrrRAFHeuristic extends AlgorithmBase {
    public static final String Name = "irr RAF";

    private final IntegerProperty numberOfRandomInsertionOrders = new SimpleIntegerProperty(100);

    @Override
    public String getName() {
        return Name;
    }

    /**
     * heuristically tries to compute a minimum irreducible RAD
     *
     * @param input - unexpanded catalytic reaction system
     * @return irr RAF or null
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress.setMaximum(getNumberOfRandomInsertionOrders());
        progress.setProgress(0);

        final ReactionSystem maxRAF = new MaxRAFAlgorithm().apply(input, new ProgressSilent());
        final ArrayList<Reaction> reactions = new ArrayList<>(maxRAF.getReactions());

        final ArrayList<Integer> seeds = new ArrayList<>();
        for (int i = 0; i < getNumberOfRandomInsertionOrders(); i++) {
            seeds.add(123 * i); // different seeds
        }

        final Single<Integer> bestSize = new Single<>(maxRAF.size());

		final Optional<ReactionSystem> smallestRAF = seeds.parallelStream().map(seed -> CollectionUtils.randomize(reactions, seed)).map(ordering -> {
			ReactionSystem work = maxRAF.shallowCopy();
			for (Reaction r : ordering) {
				work.getReactions().remove(r);
				try {
					progress.checkForCancel();
					ReactionSystem next = new MaxRAFAlgorithm().apply(work, new ProgressSilent());
					if (next.size() > 0 && next.size() <= work.size()) {
						work = next;
						synchronized (bestSize) {
							if (next.size() < bestSize.get()) {
                                bestSize.set(next.size());
                                progress.setSubtask("" + bestSize.get());
                            }
                        }
                        if (bestSize.get() == 1)
                            break;
                    } else
                        work.getReactions().add(r); // put back
                } catch (CanceledException ignored) {
                }
            }
            try {
                progress.incrementProgress();
            } catch (CanceledException ignored) {
            }
            return work;
        }).filter(r -> r != null && r.size() > 0).min(Comparator.comparingInt(ReactionSystem::size));

        final ReactionSystem result = smallestRAF.orElseGet(maxRAF::shallowCopy);
        result.getFoods().setAll(result.computeMentionedFoods(input.getFoods()));

        result.setName(Name);
        return result;
    }

    public int getNumberOfRandomInsertionOrders() {
        return numberOfRandomInsertionOrders.get();
    }

    public IntegerProperty numberOfRandomInsertionOrdersProperty() {
        return numberOfRandomInsertionOrders;
    }

    public void setNumberOfRandomInsertionOrders(int numberOfRandomInsertionOrders) {
        this.numberOfRandomInsertionOrders.set(numberOfRandomInsertionOrders);
    }
}
