/*
 * QuotientMaxRAF.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.algorithm;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.ProgressSilent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * remove all trivial RAFs
 * Daniel Huson, 4.2020
 */
public class RemoveTrivialRAFsHeuristic extends AlgorithmBase {
    public static final String Name = "No trivial RAFs";

    @Override
    public String getName() {
        return Name;
    }

    /**
     * remove all trivial RAFs
     *
     * @param input
     * @param progress
     * @throws CanceledException
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress.setTasks("Remove all trivial RAFs", "");

        final int randomInsertionOrders = 2 * input.size();

        progress.setMaximum(randomInsertionOrders);
        progress.setProgress(0);

        final ReactionSystem maxRAF = new MaxRAFAlgorithm().apply(input, new ProgressSilent()).getCompressedSystem();
        final ArrayList<Reaction> reactions = new ArrayList<>(maxRAF.getReactions());

        final ArrayList<Integer> seeds = new ArrayList<>();
        for (int i = 0; i < randomInsertionOrders; i++) {
            seeds.add(123 * i); // different seeds
        }

        final Set<Reaction> trivialReactions = new HashSet<>();

        seeds.parallelStream().map(seed -> Basic.randomize(reactions, seed)).forEach(ordering -> {
            try {
                ReactionSystem work = maxRAF.shallowCopy();
                for (Reaction r : ordering) {
                    work.getReactions().remove(r);
                    try {
                        progress.checkForCancel();
                        final ReactionSystem next = new MaxRAFAlgorithm().apply(work, new ProgressSilent()).getCompressedSystem();
                        synchronized (trivialReactions) {
                            if (next.size() == 1 && !trivialReactions.contains(next.getReactions().get(0))) {
                                trivialReactions.add(next.getReactions().get(0));
                                progress.setSubtask(input.size() + " -> " + (input.size() - trivialReactions.size()));
                                return;
                            }
                        }
                        if (next.size() > 0 && next.size() <= work.size()) {
                            work = next;
                        } else
                            work.getReactions().add(r); // put back
                    } catch (CanceledException ignored) {
                    }
                }
            } finally {
                try {
                    progress.incrementProgress();
                } catch (CanceledException ignored) {
                }
            }
        });


        final ReactionSystem result = maxRAF.shallowCopy();
        result.setName(getName());
        progress.setMaximum(result.size());
        progress.setProgress(0);

        final Set<MoleculeType> food = new TreeSet<>(result.getFoods());

        for (Reaction reaction : trivialReactions) {
            if (reaction.getDirection() == Reaction.Direction.forward || reaction.getDirection() == Reaction.Direction.both) {
                food.addAll(reaction.getProducts());
            }
            if (reaction.getDirection() == Reaction.Direction.reverse || reaction.getDirection() == Reaction.Direction.both) {
                food.addAll(reaction.getReactants());
            }
            result.getReactions().remove(reaction);
            result.getFoods().setAll(food);
        }
        return result;
    }
}
