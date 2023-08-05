/*
 * GreedilyGrowMaxCAF2MaxRAF.java Copyright (C) 2022 Daniel H. Huson
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
import catlynet.window.MainWindow;
import javafx.beans.value.ChangeListener;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressSilent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * greedily grow maxCAF to maxRAF by making reactions spontaneous
 * Daniel Huson, 3.2020
 */
public class GreedilyGrowMaxCAF2MaxRAF implements IDescribed {

    public String getDescription() {
        return "greedily grow maxCAF to maxRAF by making reactions spontaneous";
    }

    /**
     * tries to greedily grow a maxCAF to a maxRAF
     *
	 */
    public static void apply(MainWindow window, ReactionSystem inputReactionSystem, ChangeListener<Boolean> runningListener) {

        final AService<Result> service = new AService<>(window.getController().getStatusFlowPane());

        service.setCallable(() -> {
            final ProgressListener progress = service.getProgressListener();
            progress.setTasks("Grow maxCAF to maxRAF", "Initializing");
            final ReactionSystem maxCAF = (new MaxCAFAlgorithm()).apply(inputReactionSystem, new ProgressSilent());
            final ReactionSystem maxRAF = (new MaxRAFAlgorithm()).apply(inputReactionSystem, new ProgressSilent());

            final Set<String> remainingReactions = maxRAF.getReactions().stream().map(Reaction::getName).filter(n -> !maxCAF.getReactionNames().contains(n)).collect(Collectors.toSet());

            final ArrayList<String> augmentedReactions = new ArrayList<>();
            final ReactionSystem augmentedSystem = maxRAF.shallowCopy();

            int augmentedMaxCAFSize = maxCAF.size();

            progress.setSubtask("greedy extension");
            progress.setMaximum(remainingReactions.size());

            while (augmentedMaxCAFSize < maxRAF.size()) {
                final Optional<Pair<Reaction, Integer>> best = remainingReactions.parallelStream().map(name -> {
                    final Reaction reaction = maxRAF.getReaction(name);
                    final Reaction augmentedReaction = new Reaction(reaction);
                    augmentedReaction.setCatalysts("");
                    augmentedReaction.getInhibitions().clear();
                    final ReactionSystem workingSystem = augmentedSystem.shallowCopy();
                    workingSystem.replaceNamedReaction(name, augmentedReaction);
                    try {
                        progress.checkForCancel();
                        final ReactionSystem workingMaxCAF = (new MaxCAFAlgorithm()).apply(workingSystem, new ProgressSilent());
                        return new Pair<>(augmentedReaction, workingMaxCAF.size());
                    } catch (CanceledException ignored) {
                        return new Pair<>(augmentedReaction, 0);
                    }
                }).max(Comparator.comparingInt(Pair::getSecond));

                progress.incrementProgress(); // yes, need to check for cancel exactly here

                if (best.isPresent()) {
                    final Reaction augmentedReaction = best.get().getFirst();
                    augmentedMaxCAFSize = best.get().getSecond();
                    augmentedSystem.replaceNamedReaction(augmentedReaction.getName(), augmentedReaction);
                    augmentedReactions.add(augmentedReaction.getName());
                    remainingReactions.remove(augmentedReaction.getName());
                    progress.setSubtask(augmentedMaxCAFSize + " of " + maxRAF.size());
                } else { // we have gotten stuck
                    System.err.println("No valid greedy choice found");
                    break;
                }
            }
            return new Result(maxCAF.size(), maxRAF.size(), augmentedReactions);
        });

        service.runningProperty().addListener(runningListener);

        service.setOnFailed(e -> NotificationManager.showError("Greedily grow MaxCAF to MaxRAF: failed: " + service.getException()));

        service.setOnSucceeded(c -> {
            final Result result = service.getValue();
            final String message;
            if (result.getSpontaneousReactions().size() == 0 && result.getMaxCAFSize() == result.getMaxRAFSize()) {
                message = "Greedily grow MaxCAF to MaxRAF: no reactions required to be spontaneous, because MaxCAF=MaxRAF";
                NotificationManager.showInformation(message);
            } else {
                message = String.format("Greedily grow MaxCAF (size %d) to MaxRAF (size %d): required %d reactions to be spontaneous: %s", result.getMaxCAFSize(), result.getMaxRAFSize(),
                        result.getSpontaneousReactions().size(), StringUtils.toString(result.getSpontaneousReactions(), ", "));
                NotificationManager.showInformation(String.format("Greedily grew MaxCAF (size %d) to MaxRAF (size %d): required %d reactions to be spontaneous", result.getMaxCAFSize(), result.getMaxRAFSize(),
                        result.getSpontaneousReactions().size()));
            }

            window.getController().getLogTextArea().setText(window.getController().getLogTextArea().getText() + "\n" + message + "\n");
            window.getController().getLogTab().getTabPane().getSelectionModel().select(window.getController().getLogTab());
        });

        service.start();

    }

	private static class Result {
		private final int maxCAFSize;
		private final int maxRAFSize;
		private final Collection<String> spontaneousReactions;

		public Result(int maxCAFSize, int maxRAFSize, Collection<String> spontaneousReactions) {
			this.maxCAFSize = maxCAFSize;
			this.maxRAFSize = maxRAFSize;
			this.spontaneousReactions = spontaneousReactions;
		}

		public int getMaxCAFSize() {
			return maxCAFSize;
		}

        public int getMaxRAFSize() {
            return maxRAFSize;
        }

        public Collection<String> getSpontaneousReactions() {
            return spontaneousReactions;
        }
    }

}
