/*
 * CoreRAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressOverrideTaskName;
import jloda.util.progress.ProgressSilent;

import java.util.stream.Collectors;

/**
 * compute the core RAF
 * Daniel Huson, 5.2020
 */
public class CoreRAFAlgorithm extends AlgorithmBase {
    public static final String Name = "Core RAF";

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "computes the unique irreducible RAF, if it exists (Section 4.1 of [SXH20])";
    }

    /**
     * compute the core RAF
     *
	 */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        progress = new ProgressOverrideTaskName(progress, "Compute core RAF");

        final ReactionSystem maxRAF = new MaxRAFAlgorithm().apply(input, new ProgressSilent());

        final ReactionSystem importantReactions = new ReactionSystem();
        importantReactions.setName("Important");

        importantReactions.getReactions().addAll(Importance.computeReactionImportance(input, maxRAF, new MaxRAFAlgorithm(), progress)
                .stream().filter(p -> p.getSecond() == 100f).map(Pair::getFirst).collect(Collectors.toList()));
        importantReactions.getFoods().addAll(importantReactions.computeMentionedFoods(input.getFoods()));
        progress.setProgress(2);

        final ReactionSystem coreRAF = new MaxRAFAlgorithm().apply(importantReactions, progress);
        coreRAF.setName("Core RAF");
        return coreRAF;
    }
}
