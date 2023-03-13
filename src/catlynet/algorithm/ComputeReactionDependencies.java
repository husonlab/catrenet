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
import jloda.util.progress.ProgressListener;

import java.util.HashMap;

/**
 * computes the graph of dependencies between all food-set generated reactions
 * Daniel Huson and Mike Steel, 3.2023
 */
public class ComputeReactionDependencies {
	public static Graph apply(ProgressListener progress, ReactionSystem inputReactionSystem) throws CanceledException {
		progress.setTasks("Computing reaction dependencies", "F-generated set");
		var reactions = Utilities.computeFGenerated(inputReactionSystem.getFoods(), inputReactionSystem.getReactions());
		var one = MoleculeType.valueOf("!!!");
		var graph = new Graph();
		var nameNodeMap = new HashMap<String, Node>();
		progress.setSubtask("all pairs");
		progress.setMaximum(reactions.size());
		progress.setProgress(0L);
		for (var i = 0; i < reactions.size(); i++) {
			var r = reactions.get(i);
			r.getReactants().add(one);
			for (var j = i + 1; j < reactions.size(); j++) {
				var s = reactions.get(j);
				s.getProducts().add(one);
				var generatedSize = Utilities.computeFGenerated(inputReactionSystem.getFoods(), reactions).size();
				if (generatedSize < reactions.size()) {
					var rNode = nameNodeMap.computeIfAbsent(r.getName(), k -> graph.newNode(r.getName()));
					var sNode = nameNodeMap.computeIfAbsent(s.getName(), k -> graph.newNode(s.getName()));
					graph.newEdge(rNode, sNode);
				}
				s.getProducts().remove(one);
			}
			r.getReactants().remove(one);
			progress.incrementProgress();
		}
		progress.reportTaskCompleted();
		if (false) {
			System.err.println("Dependencies:");
			for (var e : graph.edges()) {
				System.err.println(e.getSource().getInfo() + " < " + e.getTarget().getInfo());
			}
		}
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
			for (var e : graph.edges()) {
				buf.append(e.getSource().getInfo()).append(" < ").append(e.getTarget().getInfo()).append("\n");
			}
			textArea.setText(buf.toString());
		});
	}
}
