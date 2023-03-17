/*
 * ComputeReactionDependencies.java Copyright (C) 2023 Daniel H. Huson
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
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.CanceledException;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.HashMap;

/**
 * computes the graph of dependencies between all food-set generated reactions
 * Daniel Huson and Mike Steel, 3.2023
 */
public class ComputeReactionDependencies {
	/**
	 * computes the graph of strict reaction dependencies. There is an edge from p to r if p is required to produce
	 * one of the reactants of r
	 *
	 * @param progress            progress
	 * @param inputReactionSystem input reactions
	 * @return graph containing all reactions and
	 * @throws CanceledException
	 */
	public static Graph apply(ProgressListener progress, ReactionSystem inputReactionSystem) throws CanceledException {
		progress.setTasks("Computing reaction dependencies", "F-generated set");
		var reactions = Utilities.computeFGenerated(inputReactionSystem.getFoods(), inputReactionSystem.getReactions());
		var one = MoleculeType.valueOf("!!!");
		var graph = new Graph();
		var nameNodeMap = new HashMap<String, Node>();
		for (var r : reactions) {
			nameNodeMap.put(r.getName(), graph.newNode(r.getName()));
		}
		progress.setSubtask("all pairs");
		progress.setMaximum(reactions.size());
		progress.setProgress(0L);
		for (int i = 0; i < reactions.size(); i++) {
			var r0 = reactions.get(i);
			for (int j = 0; j < reactions.size(); j++) {
				if (j != i) {
					var s0 = reactions.get(j);
					var ok = true;
					doubleLoop:
					for (var r : r0.allAsForward()) {
						reactions.set(i, r);
						try {
							r.getReactants().add(one);
							for (var s : s0.allAsForward()) {
								reactions.set(j, s);
								try {
									s.getProducts().add(one);
									var generatedSize = Utilities.computeFGenerated(inputReactionSystem.getFoods(), reactions).size();
									if (generatedSize == reactions.size()) {
										ok = false;
										break doubleLoop;
									}
								} finally {
									reactions.set(j, s0);
								}
							}
						} finally {
							reactions.set(i, r0);
						}
					}

					if (ok) {
						var rNode = nameNodeMap.get(r0.getName());
						var sNode = nameNodeMap.get(s0.getName());
						graph.newEdge(rNode, sNode);
					}
				}
			}
			progress.incrementProgress();
		}
		progress.reportTaskCompleted();
		return graph;
	}

	/**
	 * run the calculation in a separate thread and then post process the graph
	 *
	 * @param mainWindow
	 */
	public static void run(MainWindow mainWindow) {

		var service = new AService<Graph>(mainWindow.getStatusPane());
		service.setCallable(() -> apply(service.getProgressListener(), mainWindow.getInputReactionSystem()));
		service.restart();
		service.setOnFailed(e -> NotificationManager.showError(service.getException().getMessage()));
		service.setOnSucceeded(a -> {
			var graph = service.getValue();
			final var textArea = mainWindow.getTabManager().getTextArea("Dependencies");
			var buf = new StringBuilder();
			buf.append("# Dependencies:\n");
			var lines = graph.edgeStream().map(e -> e.getSource().getInfo() + " -> " + e.getTarget().getInfo()).sorted().toList();
			buf.append(StringUtils.toString(lines, "\n"));
			textArea.setText(buf.toString());
		});
	}
}
