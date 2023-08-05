/*
 * MuCAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.CollectionUtils;
import jloda.util.progress.ProgressListener;

import java.util.*;

/**
 * computes a maximally uninhibited CAF (MU CAF)
 * Daniel Huson, 7.2019
 * Based on notes by Mike Steel
 */
public class MuCAFAlgorithm extends AlgorithmBase {
    public static final String Name = "MU CAF";

    @Override
    public String getName() {
        return Name;
    }

	@Override
	public String getDescription() {
		return "computes one maximal uninhibited CAF";
	}

	/**
     * computes a MU CAF
     *
     * @param input - unexpanded catalytic reaction system
     * @return MU CAF or empty set
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        final ReactionSystem result = new ReactionSystem();
        result.setName(Name);

		final ArrayList<Reaction> inputReactions = CollectionUtils.randomize(input.getReactions(), new Random());
		final Set<MoleculeType> inputFood = new TreeSet<>(input.getFoods());

        final ArrayList<Set<MoleculeType>> molecules = new ArrayList<>();
        final ArrayList<Set<Reaction>> reactions = new ArrayList<>();
        final ArrayList<Set<MoleculeType>> inhibitions = new ArrayList<>();

        molecules.add(0, inputFood);
        reactions.add(0, new HashSet<>());
        inhibitions.add(0, new HashSet<>());

        progress.setMaximum(100);
        int i = 0;
        while (true) {
            i++;

            Reaction anUninhibitedReaction = null;
			for (Reaction reaction : CollectionUtils.difference(inputReactions, reactions.get(i - 1))) {
				if (reaction.isCatalyzedAndUninhibitedAndHasAllReactants(molecules.get(i - 1), molecules.get(i - 1), inhibitions.get(i - 1), reaction.getDirection())) {
					anUninhibitedReaction = reaction;
					break;
				}
			}

            if (anUninhibitedReaction != null) {
				reactions.add(i, CollectionUtils.union(reactions.get(i - 1), Collections.singleton(anUninhibitedReaction)));
				molecules.add(i, Utilities.addAllMentionedProducts(molecules.get(i - 1), reactions.get(i)));
				inhibitions.add(i, CollectionUtils.union(inhibitions.get(i - 1), anUninhibitedReaction.getInhibitions()));
            } else
                break;
            progress.setProgress(reactions.size());
        }

        if (reactions.get(i - 1).size() > 0) {
            result.getReactions().setAll(reactions.get(i - 1));
            result.getFoods().setAll(result.computeMentionedFoods(input.getFoods()));
        }

        return result;
    }
}
