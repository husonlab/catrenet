/*
 * Animate.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.view;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import jloda.graph.*;

import java.util.ArrayList;

/**
 * run animation on graph
 * Daniel Huson, 7.2019
 */
public class MoleculeFlowAnimation {
    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    /**
     * setup molecule flow animation
     *
     * @param reactionGraph
     * @param foodNodes
     * @param edge2Group
     * @param world
     */
    public MoleculeFlowAnimation(Graph reactionGraph, NodeSet foodNodes, EdgeArray<Group> edge2Group, Group world) {
        final EdgeIntegerArray edge2count = new EdgeIntegerArray(reactionGraph);

        final Service<Boolean> foodService = new Service<>() {
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        while (!isCancelled()) {
                            Thread.sleep(500);
                            for (Node v : foodNodes) {
                                Edge lowest = null;
                                for (Edge e : v.outEdges()) {
                                    if (lowest == null || edge2count.get(e) < edge2count.get(lowest))
                                        lowest = e;
                                }
                                if (lowest != null) {
                                    final Edge use = lowest;
                                    Platform.runLater(() -> animateEdge(playing, use, edge2count, edge2Group, Color.DARKRED, world));
                                }
                            }
                        }
                        return Boolean.TRUE;
                    }
                };
            }

            ;
        };

        foodService.setOnFailed((e) ->
                System.err.println("Failed: " + foodService.getException()));

        playing.addListener((c, o, n) -> {
            if (n)
                foodService.restart();
            else
                foodService.cancel();
        });
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    /**
     * animate an edge
     *
     * @param edge
     * @param edge2count
     * @param edge2Group
     * @param world
     */
    private static void animateEdge(BooleanProperty playing, Edge edge, EdgeIntegerArray edge2count, EdgeArray<Group> edge2Group, Color color0, Group world) {
        if (playing.get()) {
            final Path path = getPath(edge2Group.get(edge));

            if (path != null) {
                edge2count.increment(edge);
                final Color color = color0.brighter();

                Ellipse blob = new Ellipse(4, 4);
                PathTransition pathTransition = new PathTransition(Duration.seconds(2), path, blob);
                blob.setFill(color);
                blob.setStroke(Color.DARKGRAY);
                world.getChildren().add(blob);
                pathTransition.setOnFinished((e) -> {
                    world.getChildren().remove(blob);

                    for (Edge f : computeEdgesReadyToFire(edge.getTarget(), edge2count)) {
                        animateEdge(playing, f, edge2count, edge2Group, color, world);
                    }
                });
                pathTransition.play();
            }
        }
    }

    /**
     * compute all edges ready to fire
     *
     * @param v
     * @param edge2count
     * @return edges ready to fire
     */
    private static ArrayList<Edge> computeEdgesReadyToFire(Node v, EdgeIntegerArray edge2count) {
        final ArrayList<Edge> result = new ArrayList<>();

        if (v.getInfo() instanceof Reaction) {
            boolean hasCatalyst = false;
            boolean hasInhibitor = false;

            for (Edge e : v.inEdges()) {
                if (e.getInfo() == ReactionGraphView.EdgeType.Catalyst && edge2count.get(e) > 0) {
                    hasCatalyst = true;
                } else if (e.getInfo() == ReactionGraphView.EdgeType.Inhibitor && edge2count.get(e) > 0) {
                    hasInhibitor = true;
                }
            }
            final int reactantThreshold = (hasCatalyst && !hasInhibitor ? 1 : 5);
            for (Edge e : v.inEdges()) {
                if (e.getInfo() == ReactionGraphView.EdgeType.Reactant && edge2count.get(e) < reactantThreshold)
                    return result;
            }
            for (Edge e : v.inEdges()) {
                if (e.getInfo() == ReactionGraphView.EdgeType.Reactant && edge2count.get(e) < reactantThreshold)
                    edge2count.decrement(e, reactantThreshold);
            }
            for (Edge e : v.outEdges()) {
                if (e.getInfo() == ReactionGraphView.EdgeType.Product)
                    result.add(e);
            }
        } else if (v.getInfo() instanceof ReactionGraphView.AndNode) {
            for (Edge e : v.inEdges()) {
                if (edge2count.get(e) <= 0)
                    return result;
            }
            for (Edge e : v.inEdges()) {
                edge2count.decrement(e);
            }
            for (Edge e : v.outEdges()) {
                result.add(e);
            }
        } else if (v.getInfo() instanceof MoleculeType) {
            Edge lowest = null;
            for (Edge e : v.outEdges()) {
                if (lowest == null || edge2count.get(e) < edge2count.get(lowest))
                    lowest = e;
            }
            if (lowest != null)
                result.add(lowest);
            return result;
        }
        return result;
    }

    /**
     * find a path in a group
     *
     * @param group
     * @return path, if found
     */
    private static Path getPath(Group group) {
        for (javafx.scene.Node child : group.getChildren()) {
            if (child instanceof Path)
                return (Path) child;
        }
        return null;
    }
}
