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
import catlynet.view.EdgeType;
import catlynet.window.MainWindow;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ExecuteInParallel;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * computes the graph of dependencies between all food-set generated reactions
 * Daniel Huson and Mike Steel, 3.2023
 */
public class ComputeReactionDependencies implements IDescribed {

	public String getDescription() {
		return "computes the graph of dependencies between all food-set generated reactions [HXRS23]";
	}

	/**
	 * computes the graph of strict reaction dependencies. There is an edge from p to r if p is required to produce
	 * one of the reactants of r
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

		var levelReactionsMap = new TreeMap<Integer, Collection<Reaction>>();
		var levelMoleculesMap = new TreeMap<Integer, Collection<MoleculeType>>();
		levelMoleculesMap.put(-1, allFood);

		var levels = 0;

		progress.setTasks("Computing reaction dependencies", "Computing levels");
		var maxProgress = allReactions.size();
		progress.setMaximum(maxProgress);
		progress.setProgress(0);

		var availableMolecules = new HashSet<>(allFood);
		var availableReactions = new ArrayList<>(allReactions);
		while (availableReactions.size() > 0) {
			var additionalReactions = new HashSet<Reaction>();
			var additionalMolecules = new HashSet<MoleculeType>();
			for (var r : availableReactions) {
				if (availableMolecules.containsAll(r.getReactants())) {
					additionalReactions.add(r);
					additionalMolecules.addAll(r.getProducts());
				}
			}
			if (additionalReactions.size() == 0)
				break;
			availableMolecules.addAll(additionalMolecules);
			levelReactionsMap.put(levels, additionalReactions);
			additionalMolecules.addAll(levelMoleculesMap.get(levels - 1));
			levelMoleculesMap.put(levels, additionalMolecules);
			availableReactions.removeAll(additionalReactions);
			levels++;
			progress.setProgress(maxProgress - availableReactions.size());
		}

		System.err.println("Levels: " + levels);

		progress.setTasks("Computing reaction dependencies", "");
		progress.setMaximum(levels);
		progress.setProgress(0);

		var graph = (graph0 != null ? graph0 : new Graph());
		graph.clear();

		var nameNodeMap = new HashMap<String, Node>();
		for (var r : allReactions) {
			nameNodeMap.put(r.getName(), graph.newNode(r));
		}
		var one = MoleculeType.valueOf("!!!");

		for (var k0 = 1; k0 < levels; k0++) {
			var k = k0;
			progress.setSubtask("%d of %d levels".formatted(k, levels - 1));

			try {
				ExecuteInParallel.apply(levelReactionsMap.get(k),
						s -> {
							var allReactionsBetween = new HashSet<Reaction>();
							s.getProducts().add(one);
							var ancestors = new HashSet<Node>();
							for (var i = k - 1; i >= 0; i--) {
								allReactionsBetween.addAll(levelReactionsMap.get(i));
								for (var r : levelReactionsMap.get(i)) {
									if (!ancestors.contains(nameNodeMap.get(r.getName()))) {
										var r1 = new Reaction(r.getName(), r);
										r1.getReactants().add(one);
										var foodSet = levelMoleculesMap.get(i - 1);
										if (!isFoodGenerated(foodSet, allReactionsBetween, r1, r, s)) {
											var rNode = nameNodeMap.get(r.getName());
											var sNode = nameNodeMap.get(s.getName());
											synchronized (graph) {
												graph.newEdge(rNode, sNode, EdgeType.Association);
											}
											collectAllAncestors(rNode, ancestors);
										}
										//r.getReactants().remove(one);
									}
								}
								progress.checkForCancel();
							}
							s.getProducts().remove(one);
						}, 15 /*ProgramExecutorService.getNumberOfCoresToUse()*/, progress);
			} catch (Exception ignored) {
			}

			try {
				progress.checkForCancel();
			} catch (CanceledException ex) {
				Basic.caught(ex);
				throw ex;
			}
		}
		progress.reportTaskCompleted();
		return graph;
	}

	public static boolean isFoodGenerated(Collection<MoleculeType> food, Collection<Reaction> reactions, Reaction add, Reaction ignore, Reaction reaction) {
		var availableFood = new HashSet<>(food);
		var availableReactions = new ArrayList<>(reactions);
		availableReactions.add(add);
		availableReactions.add(reaction);
		while (true) {
			var generated = availableReactions.stream().filter(r -> r != ignore).filter(r -> r.isHasAllReactants(availableFood, r.getDirection())).collect(Collectors.toList());
			if (generated.size() > 0) {
				if (generated.contains(reaction))
					return true;
				for (var r : generated) {
					availableFood.addAll(r.getProducts());
				}
				availableReactions.removeAll(generated);
			} else
				break;
		}
		return false;
	}

	public static void collectAllAncestors(Node v, Set<Node> ancestors) {
		var stack = new Stack<Node>();
		stack.push(v);
		while (stack.size() > 0) {
			v = stack.pop();
			if (!ancestors.contains(v)) {
				ancestors.add(v);
				v.parents().forEach(stack::push);
			}
		}
	}

	/**
	 * computes the graph of strict reaction dependencies. There is an edge from p to r if p is required to produce
	 * one of the reactants of r
	 *
	 * @param progress            progress
	 * @param inputReactionSystem input reactions
	 * @return graph containing all reactions and
	 * @throws CanceledException
	 */
	public static Graph applyOld(ProgressListener progress, ReactionSystem inputReactionSystem, Graph graph) throws CanceledException {
		progress.setTasks("Computing reaction dependencies", "F-generated set");
		var reactions = Utilities.computeFGenerated(inputReactionSystem.getFoods(), inputReactionSystem.getReactions());
		var one = MoleculeType.valueOf("!!!");
		if (graph == null)
			graph = new Graph();
		else
			graph.clear();
		var nameNodeMap = new HashMap<String, Node>();
		for (var r : reactions) {
			nameNodeMap.put(r.getName(), graph.newNode(r));
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
						graph.newEdge(rNode, sNode, EdgeType.Association);
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
		service.setCallable(() -> apply(service.getProgressListener(), mainWindow.getInputReactionSystem(), null));
		service.setOnScheduled(e -> mainWindow.getDocument().setReactionDependencyNetwork(null));
		service.setOnFailed(e -> NotificationManager.showError(service.getException().getMessage()));
		service.setOnCancelled(e -> NotificationManager.showWarning("User canceled compute dependencies"));
		service.setOnSucceeded(a -> {
			var graph = service.getValue();
			mainWindow.getDocument().setReactionDependencyNetwork(graph);
			final var textArea = mainWindow.getTabManager().getTextTab("Dependencies", null).getTextArea();
			var buf = new StringBuilder();
			buf.append("# Earliest reactions (%,d):%n".formatted(graph.nodeStream().filter(v -> v.getInDegree() == 0 && v.getOutDegree() > 0).count()));
			buf.append(StringUtils.toString(new TreeSet<>(graph.nodeStream().filter(v -> v.getInDegree() == 0 && v.getOutDegree() > 0).map(v -> v.getInfo().toString()).toList()), "\n")).append("\n");
			buf.append("# Dependencies (%,d):%n".formatted(graph.getNumberOfEdges()));
			var lines = graph.edgeStream().map(e -> e.getSource().getInfo() + " -> " + e.getTarget().getInfo()).sorted().toList();
			buf.append(StringUtils.toString(lines, "\n"));
			textArea.setText(buf.toString());
		});
		service.restart();
	}
}
