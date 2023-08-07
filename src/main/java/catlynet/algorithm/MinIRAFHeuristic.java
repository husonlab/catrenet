/*
 * MinIRAFHeuristic.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.model.ReactionSystem;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.util.CanceledException;
import jloda.util.CollectionUtils;
import jloda.util.Single;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.util.ArrayList;

/**
 * heuristically tries to compute a minimum irreducible RAF
 * Daniel Huson, 3.2020
 * Based on notes by Mike Steel
 */
public class MinIRAFHeuristic extends AlgorithmBase {
    public static final String Name = "iRAF";

    private final IntegerProperty numberOfRandomInsertionOrders = new SimpleIntegerProperty(100);

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "searches for irreducible RAFs in a heuristic fashion [HS23]";
    }

    /**
	 * heuristically tries to compute a minimum irreducible RAF
     *
     * @param input - unexpanded catalytic reaction system
     * @return irr RAF or null
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
		var list = applyAllSmallest(input, progress);
		if (!list.isEmpty())
			return list.get(0);
		else return null;
    }

    /**
	 * heuristically tries to compute a minimum irreducible RAF
     *
     * @param input - unexpanded catalytic reaction system
     * @return irr RAF or null
     */
    public ArrayList<ReactionSystem> applyAllSmallest(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress.setMaximum(getNumberOfRandomInsertionOrders());
        progress.setProgress(0);

        final var maxRAF = new MaxRAFAlgorithm().apply(input, new ProgressSilent());
        final var reactions = new ArrayList<>(maxRAF.getReactions());

        final var seeds = new ArrayList<Integer>();
        for (var i = 0; i < getNumberOfRandomInsertionOrders(); i++) {
            seeds.add(123 * i); // different seeds
        }

        final var best = new ArrayList<ReactionSystem>();
        final var bestSize = new Single<>(maxRAF.size());
        for (var seed : seeds) {
            var ordering = CollectionUtils.randomize(reactions, seed);
            var work = maxRAF.shallowCopy();
			work.setName(Name);
            for (var r : ordering) {
                work.getReactions().remove(r);
                try {
                    progress.checkForCancel();
                    var next = new MaxRAFAlgorithm().apply(work, new ProgressSilent());
					next.setName(Name);
                    if (next.size() > 0 && next.size() <= work.size()) {
                        work = next;
                        if (next.size() < bestSize.get()) {
                            best.clear();
                            bestSize.set(next.size());
                            progress.setSubtask("" + bestSize.get());
                        }
                        if (next.size() == bestSize.get() && best.stream().noneMatch(a -> CollectionUtils.equalsAsSets(next.getReactions(), a.getReactions()))) {
                            best.add(next);
                        }
                        if (bestSize.get() == 1)
                            break;
                    } else
                        work.getReactions().add(r); // put back
                } catch (CanceledException ignored) {
                }
            }
        }
		if (best.isEmpty()) {
			var result = maxRAF.shallowCopy();
			result.setName(Name);
			best.add(result);
		}
        return best;
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
