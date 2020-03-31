/*
 * URAFAlgorithm.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.fx.window.NotificationManager;
import jloda.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

/**
 * heuristically tries to compute a mininum irreducible RAD
 * Daniel Huson, 3.2020
 * Based on notes by Mike Steel
 */
public class MinIrrRAFHeuristic extends AlgorithmBase {
    private final IntegerProperty maxRounds = new SimpleIntegerProperty(200);
    private final Random random = new Random();

    /**
     * heuristically tries to compute a minimum irreducible RAD
     *
     * @param input - unexpanded catalytic reaction system
     * @return irr RAF or null
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress.setMaximum(getMaxRounds());
        progress.setProgress(0);

        final ReactionSystem maxRAF = new MaxRAFAlgorithm().apply(input, new ProgressSilent()).getCompressedSystem();

        final Single<ReactionSystem> smallestRAF = new Single<>(maxRAF);

        try {
            while (true) {
                progress.setSubtask("size: " + smallestRAF.get().size());

                final ArrayList<Reaction> reactions = new ArrayList<>(smallestRAF.get().getReactions());
                Basic.randomize(reactions, random);

                final Optional<ReactionSystem> best = reactions.parallelStream().map(r -> {
                    final ReactionSystem workingSystem = smallestRAF.get().shallowCopy();
                    workingSystem.getReactions().remove(r);
                    try {
                        progress.checkForCancel();
                        return new MaxRAFAlgorithm().apply(workingSystem, new ProgressSilent()).getCompressedSystem();
                    } catch (CanceledException ignored) {
                        return null;
                    }
                }).filter(r -> r != null && r.size() > 0).min(Comparator.comparingInt(ReactionSystem::size));
                if (best.isPresent()) {
                    smallestRAF.set(best.get());
                    if (smallestRAF.get().size() == 1)
                        break; // can't get any smaller than this
                } else
                    break;
                progress.incrementProgress();
            }
        } catch (CanceledException ex) {
            NotificationManager.showWarning("User CANCELED, showing smallest irr RAF found so far");
        }
        smallestRAF.get().setName("irr RAF");
        return smallestRAF.get();
    }

    public int getMaxRounds() {
        return maxRounds.get();
    }

    public IntegerProperty maxRoundsProperty() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds.set(maxRounds);
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }
}
