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
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.CanceledException;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import static catlynet.algorithm.ComputeReactionDependencies.collectAllAncestors;

/**
 * computes the graph of dependencies between all molecules
 * Daniel Huson and Mike Steel, 3.2023
 */
public class ComputeMoleculeDependencies implements IDescribed {

	public String getDescription() {
		return "computes the graph of dependencies between all molecules [HXRS23]";
	}

	/**
	 * computes the graph of strict molecule dependencies. There is an edge from
	 *
	 * @param progress            progress
	 * @param inputReactionSystem input reactions
	 * @return graph containing all reactions and
	 * @throws CanceledException
	 */
	public static Graph apply(ProgressListener progress, ReactionSystem inputReactionSystem, Graph graph0) throws CanceledException {
		var allFood = inputReactionSystem.getFoods();
		var allReactions = new ArrayList<Reaction>();
		for (var r : inputReactionSystem.getReactions()) {
			allReactions.addAll(r.allAsForward());
		}
		var allMolecules = Utilities.computeClosure(allFood, allReactions);

		var graph = (graph0 != null ? graph0 : new Graph());
		graph.clear();

		allFood.forEach(allMolecules::remove);

		var moleculeNodeMap = new HashMap<MoleculeType, Node>();
		for (var m : allMolecules) {
			moleculeNodeMap.put(m, graph.newNode(m));
		}

		progress.setTasks("Computing molecule dependencies", "");
		progress.setMaximum(allMolecules.size());
		progress.setProgress(0);

		var ancestors = new HashSet<Node>();
		var count = 0;
		for (var b : allMolecules) {
			progress.setSubtask("%,d of %,d".formatted(++count, allFood.size()));
			var bNode = moleculeNodeMap.get(b);
			ancestors.clear();
			collectAllAncestors(bNode, ancestors);
			for (var a : allMolecules) {
				if (a != b) {
					var aNode = moleculeNodeMap.get(a);
					if (!ancestors.contains(aNode)) {
						if (!isFoodGenerated(allFood, allReactions, b, a)) {
							graph.newEdge(aNode, bNode);
						}
					}
				}
			}
			progress.incrementProgress();
		}
		return graph;
	}

	public static boolean isFoodGenerated(Collection<MoleculeType> food, Collection<Reaction> reactions, MoleculeType target, MoleculeType ignore) {
		if (food.contains(target))
			return true;
		var availableFood = new HashSet<>(food);
		var availableReactions = new ArrayList<>(reactions);
		while (true) {
			var generated = availableReactions.stream().filter(r -> !r.getProducts().contains(ignore)).filter(r -> r.isHasAllReactants(availableFood, r.getDirection())).collect(Collectors.toList());
			if (generated.size() > 0) {
				for (var r : generated) {
					if (r.getProducts().contains(target))
						return true;
					availableFood.addAll(r.getProducts());
				}
				availableReactions.removeAll(generated);
			} else
				break;
		}
		return false;
	}

	/**
	 * run the calculation in a separate thread and then post process the graph
	 *
	 * @param mainWindow
	 */
	public static void run(MainWindow mainWindow) {
		var service = new AService<Graph>(mainWindow.getStatusPane());
		service.setCallable(() -> apply(service.getProgressListener(), mainWindow.getInputReactionSystem(), null));
		service.setOnScheduled(e -> mainWindow.getDocument().setReactionDependencyNetwork(null));
		service.setOnFailed(e -> NotificationManager.showError(service.getException().getMessage()));
		service.setOnCancelled(e -> NotificationManager.showWarning("User canceled compute molecule dependencies"));
		service.setOnSucceeded(a -> {
			var graph = service.getValue();
			mainWindow.getDocument().setReactionDependencyNetwork(graph);
			final var textArea = mainWindow.getTabManager().getTextTab("Molecule dependencies", null).getTextArea();
			var buf = new StringBuilder();
			buf.append("# Molecule dependencies (%,d):%n".formatted(graph.getNumberOfEdges()));
			var lines = graph.edgeStream().map(e -> e.getSource().getInfo() + " -> " + e.getTarget().getInfo()).sorted().toList();
			buf.append(StringUtils.toString(lines, "\n"));
			textArea.setText(buf.toString());
		});
		service.restart();
	}
}
