/*
 *  MaxURAFsAlgorithm.java Copyright (C) 2026 Daniel H. Huson
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

package catrenet.algorithm;

import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.SetUtils;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.util.*;

/**
 * computes all maximal uninhibited RAFs (uRAFs)
 * <p>
 * A uRAF for a CRS with inhibition is a RAF (of the associated CRS without inhibition) in which no
 * reaction is inhibited by the product of any reaction in the RAF, nor by any food element.
 * <p>
 * By Steel (2026, Proposition 2 / Corollary 3.1), a set is a uRAF exactly when it is a RAF of Q|J for
 * some subset J of the inhibiting elements, where Q|J keeps only the reactions r whose inhibitors lie
 * within J and whose products avoid J. Each maximal uRAF is the (unique) maximal RAF of some such Q|J,
 * so there are at most 2^k of them, where k is the number of inhibiting elements. Enumerating the 2^k
 * subsets and computing the maximal RAF of each is polynomial once k is fixed.
 * <p>
 * Reactions inhibited by a food element are dropped up front: food is always present, so such a
 * reaction is always inhibited and can never lie in a uRAF (this also removes those elements from the
 * set of inhibiting elements that must be enumerated).
 * <p>
 * Daniel Huson, 9.2026, based on notes by Mike Steel
 */
public class MaxURAFsAlgorithm implements IDescribed {
    public static final String Name = "Max U RAFs";

    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "computes all maximal uRAFs by examining the max RAF for each subset of the inhibiting elements (Steel 2026); feasible when the number of inhibiting elements is small";
    }

    /**
     * the elements that inhibit at least one reaction that is not itself inhibited by a food element.
     * Its size k determines the 2^k running time and drives the "too many inhibiting elements" warning.
     *
     * @return inhibiting elements to be enumerated over
     */
    public static Set<MoleculeType> inhibitingElements(ReactionSystem input) {
        final var food = new HashSet<>(input.getFoods());
        final var result = new TreeSet<MoleculeType>();
        for (var reaction : input.getReactions()) {
            if (!SetUtils.intersect(reaction.getInhibitions(), food))
                result.addAll(reaction.getInhibitions());
        }
        return result;
    }

    /**
     * computes all maximal uRAFs
     *
     * @param input unexpanded catalytic reaction system with inhibitions
     * @return the maximal uRAFs, empty if none exists
     */
    public Collection<ReactionSystem> apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        final var food = new TreeSet<>(input.getFoods());

        // drop reactions inhibited by a food element: food is always present, so such a reaction is
        // always inhibited and can never lie in a uRAF
        final var reactions = new ArrayList<Reaction>();
        for (var reaction : input.getReactions()) {
            if (!SetUtils.intersect(reaction.getInhibitions(), food))
                reactions.add(reaction);
        }

        // the inhibiting elements among the remaining reactions (all non-food); enumerate their subsets
        final var inhibitors = new ArrayList<MoleculeType>();
        {
            final var set = new TreeSet<MoleculeType>();
            for (var reaction : reactions)
                set.addAll(reaction.getInhibitions());
            inhibitors.addAll(set);
        }
        final var k = inhibitors.size();

        final var candidates = new ArrayList<ReactionSystem>();
        final var maxRAFAlgorithm = new MaxRAFAlgorithm();
        final var silent = new ProgressSilent(); // the inner max-RAF runs drive their own (ignored) progress

        final var numberOfSubsets = 1L << k;
        progress.setMaximum(numberOfSubsets);
        progress.setProgress(0);

        for (var mask = 0L; mask < numberOfSubsets; mask++) {
            final var J = new HashSet<MoleculeType>();
            for (var b = 0; b < k; b++) {
                if ((mask & (1L << b)) != 0L)
                    J.add(inhibitors.get(b));
            }

            // R[J] = { r : inhibitors(r) is a subset of J, and products(r) is disjoint from J }
            final var qJ = new ReactionSystem();
            qJ.getFoods().setAll(input.getFoods());
            for (var reaction : reactions) {
                if (J.containsAll(reaction.getInhibitions()) && !SetUtils.intersect(reaction.getProducts(), J))
                    qJ.getReactions().add(reaction);
            }

            if (!qJ.getReactions().isEmpty()) {
                final var raf = maxRAFAlgorithm.apply(qJ, silent); // a RAF of Q|J is a uRAF of the input
                if (raf.size() > 0)
                    candidates.add(raf);
            }
            progress.incrementProgress();
        }

        return keepMaximal(candidates);
    }

    /**
     * keeps only the maximal uRAFs: removes duplicates and any uRAF strictly contained in another
     *
     * @return maximal uRAFs
     */
    private static Collection<ReactionSystem> keepMaximal(Collection<ReactionSystem> candidates) {
        // deduplicate by the set of reaction names, keeping one representative each
        final var reactionSet2system = new LinkedHashMap<Set<String>, ReactionSystem>();
        for (var system : candidates)
            reactionSet2system.putIfAbsent(new TreeSet<>(system.getReactionNames()), system);

        final var reactionSets = new ArrayList<>(reactionSet2system.keySet());

        final var result = new ArrayList<ReactionSystem>();
        for (var entry : reactionSet2system.entrySet()) {
            final var reactionSet = entry.getKey();
            var maximal = true;
            for (var other : reactionSets) {
                if (other.size() > reactionSet.size() && other.containsAll(reactionSet)) {
                    maximal = false;
                    break;
                }
            }
            if (maximal)
                result.add(entry.getValue());
        }
        return result;
    }
}
