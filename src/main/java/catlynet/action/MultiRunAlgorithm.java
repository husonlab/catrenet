/*
 * MultiRunAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.action;

import catlynet.algorithm.AlgorithmBase;
import catlynet.io.ModelIO;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextArea;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.util.Pair;
import jloda.util.StringUtils;

import java.util.*;

/**
 * runs an algorithm multiple times
 * Daniel Huson, 8.2019
 */
public class MultiRunAlgorithm {
    /**
     * run an algorithm, return the resulting model and write to text area
     *
	 */
    public static void apply(MainWindow window, final ReactionSystem inputReactions, AlgorithmBase algorithm, TextArea textArea, int numberOfRuns, ChangeListener<Boolean> runningListener) {
        final MainWindowController controller = window.getController();

        final AService<Collection<Pair<ReactionSystem, Integer>>> service = new AService<>(controller.getStatusFlowPane());
        service.setCallable(() -> {
            final Map<Set<String>, Pair<ReactionSystem, Integer>> names2reactions2counts = new HashMap<>();
            for (int i = 0; i < numberOfRuns; i++) {
                final ReactionSystem outputReactions = algorithm.apply(inputReactions, service.getProgressListener());
                final Set<String> reactionNames = new HashSet<>(outputReactions.getReactionNames());
                Pair<ReactionSystem, Integer> pair = names2reactions2counts.get(reactionNames);
                if (pair == null) {
                    pair = new Pair<>(outputReactions, 0);
                    names2reactions2counts.put(reactionNames, pair);
                }
                pair.setSecond(pair.getSecond() + 1);
            }
            return names2reactions2counts.values();
        });

        service.runningProperty().addListener(runningListener);
        service.setOnSucceeded((c) -> {
            final Collection<Pair<ReactionSystem, Integer>> results = service.getValue();

            if (results.size() > 0) {
                final String headLine = "Running algorithm " + algorithm.getClass().getSimpleName() + " " + numberOfRuns + " times produced " + results.size() + " results";

                NotificationManager.showInformation(headLine);
                window.getLogStream().println("\n" + headLine);

                final StringBuilder buf = new StringBuilder();
                int count = 0;
                for (Pair<ReactionSystem, Integer> pair : results) {
					final String algorithmName = StringUtils.fromCamelCase(algorithm.getClass().getSimpleName()).replaceAll("Algorithm", "");
                    buf.append(String.format("# %s %d has %d reactions (and was seen %d/%d times)\n\n", algorithmName, ++count, pair.getFirst().size(), pair.getSecond(), numberOfRuns));
                    buf.append(ModelIO.toString(pair.getFirst(), false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
                    buf.append("\n");
                }
                textArea.setText(buf.toString());
            }
        });
        service.start();
    }
}
