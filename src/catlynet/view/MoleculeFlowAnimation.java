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
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.util.Duration;
import jloda.fx.util.SelectionEffectRed;
import jloda.graph.*;
import jloda.util.Basic;
import jloda.util.Triplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * run simulation on graph
 * Daniel Huson, 7.2019
 */
public class MoleculeFlowAnimation {
    static private final Random random = new Random();

    public enum Model {CAF, RAF, PseudoRAF}

    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final ObjectProperty<Model> model = new SimpleObjectProperty<>(Model.RAF);

    private final IntegerProperty uncatalyzedOrInhibitedThreshold = new SimpleIntegerProperty(20);

    private final BooleanProperty animateInhibitions = new SimpleBooleanProperty(false);

    /**
     * setup molecule flow simulation
     *
     * @param reactionGraph
     * @param foodNodes
     * @param edge2Group
     * @param world
     */
    public MoleculeFlowAnimation(Graph reactionGraph, NodeSet foodNodes, EdgeArray<Group> edge2Group, Group world) {
        final EdgeIntegerArray edge2count = new EdgeIntegerArray(reactionGraph);

        final Color color = Color.DARKRED.brighter().brighter().brighter().brighter();

        // this service pumps molecules into the system
        final Service<Boolean> service = new Service<>() {
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        int count = 0;
                        while (!isCancelled()) {
                            Thread.sleep(Math.round(nextGaussian(random, 100, 10, true)));
                            count++;
                            for (Node v : Basic.randomize(foodNodes, random)) {
                                for (Edge e : Basic.randomize(v.adjacentEdges(), random)) {
                                    if (e.getInfo() == EdgeType.Reactant || e.getInfo() == EdgeType.ReactantReversible || e.getInfo() == EdgeType.Catalyst) {
                                        final String label = ((MoleculeType) e.getSource().getInfo()).getName();
                                        Platform.runLater(() -> animateEdge(e, false, label, edge2count, edge2Group, color, world));
                                        break;
                                    } else if (e.getInfo() == EdgeType.ProductReversible) {
                                        final String label = ((MoleculeType) e.getTarget().getInfo()).getName();
                                        Platform.runLater(() -> animateEdge(e, true, label, edge2count, edge2Group, color, world));
                                        break;
                                    }
                                }
                            }
                            // in a pseudo RAF, need to pump molecules into none-food nodes, as well, to get things going
                            if (getModel() == Model.PseudoRAF && count == getUncatalyzedOrInhibitedThreshold()) {
                                count = 0;

                                loop:
                                for (Node v : Basic.randomize(reactionGraph.nodes(), random)) {
                                    if (!foodNodes.contains(v) && v.getInfo() instanceof MoleculeType) {
                                        for (Edge e : Basic.randomize(v.adjacentEdges(), random)) {
                                            if (e.getInfo() == EdgeType.Reactant || e.getInfo() == EdgeType.ReactantReversible || e.getInfo() == EdgeType.Catalyst) {
                                                final String label = ((MoleculeType) e.getSource().getInfo()).getName();
                                                Platform.runLater(() -> animateEdge(e, false, label, edge2count, edge2Group, color, world));
                                                break loop;
                                            } else if (e.getInfo() == EdgeType.ProductReversible) {
                                                final String label = ((MoleculeType) e.getTarget().getInfo()).getName();
                                                Platform.runLater(() -> animateEdge(e, true, label, edge2count, edge2Group, color, world));
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return Boolean.TRUE;
                    }
                };
            }
        };

        service.setOnFailed((e) ->
                System.err.println("Failed: " + service.getException()));

        playing.addListener((c, o, n) -> {
            if (n)
                service.restart();
            else {
                service.cancel();
                edge2count.clear();
            }
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

    public Model getModel() {
        return model.get();
    }

    public ObjectProperty<Model> modelProperty() {
        return model;
    }

    public void setModel(Model model) {
        this.model.set(model);
    }

    public int getUncatalyzedOrInhibitedThreshold() {
        return uncatalyzedOrInhibitedThreshold.get();
    }

    public IntegerProperty uncatalyzedOrInhibitedThresholdProperty() {
        return uncatalyzedOrInhibitedThreshold;
    }

    public void setUncatalyzedOrInhibitedThreshold(int uncatalyzedOrInhibitedThreshold) {
        this.uncatalyzedOrInhibitedThreshold.set(uncatalyzedOrInhibitedThreshold);
    }

    /**
     * animate an edge
     *
     * @param edge
     * @param edge2count
     * @param edge2Group
     * @param world
     */
    private void animateEdge(Edge edge, boolean reverse, String label, EdgeIntegerArray edge2count, EdgeArray<Group> edge2Group, Color color, Group world) {
        final Path path = ReactionGraphView.getPath(edge2Group.get(edge));

        if (path != null) {
            final Text text = new Text(label);
            text.setFont(ReactionGraphView.getFont());
            text.setFill(color);

            final PathTransition pathTransition = new PathTransition(Duration.seconds(2), path, text);

            pathTransition.setOnFinished((e) -> {
                world.getChildren().remove(text);
                if (edge.getOwner() != null) {
                    edge2count.increment(edge);
                }
                if (edge.getOwner() != null && playing.get()) {
                    path.setEffect(SelectionEffectRed.getInstance());

                    for (Triplet<Edge, Boolean, String> triplet : computeEdgesReadyToFire(edge, reverse ? edge.getSource() : edge.getTarget(), edge2count)) {
                        animateEdge(triplet.getFirst(), triplet.getSecond(), triplet.getThird(), edge2count, edge2Group, color.darker(), world);
                    }
                } else
                    path.setEffect(null);

            });

            if (reverse) {
                pathTransition.setRate(-pathTransition.getRate());
                pathTransition.jumpTo(pathTransition.getDuration());
                pathTransition.play();
            } else
                pathTransition.play();
            Platform.runLater(() -> world.getChildren().add(text));
        }
    }

    /**
     * compute all edges ready to fire
     *
     * @param v
     * @param edge2count
     * @return edges ready to fire
     */
    private ArrayList<Triplet<Edge, Boolean, String>> computeEdgesReadyToFire(Edge e, Node v, EdgeIntegerArray edge2count) {
        final ArrayList<Triplet<Edge, Boolean, String>> emptyList = new ArrayList<>();

        if (v.getInfo() instanceof Reaction) {
            boolean hasCatalyst = false;
            boolean hasInhibitor = false;

            for (Edge f : v.inEdges()) {
                if (f.getInfo() == EdgeType.Catalyst && edge2count.get(f) > 0) {
                    hasCatalyst = true;
                } else if (isAnimateInhibitions() && f.getInfo() == EdgeType.Inhibitor && edge2count.get(f) > 0) {
                    hasInhibitor = true;
                }
            }
            // in a CAF, reaction always requires a catalyst and can never run with inhibition
            // in other cases, run at lower rate
            final int reactantThreshold = (hasCatalyst && !hasInhibitor ? 1 : getModel() == Model.CAF ? Integer.MAX_VALUE : getUncatalyzedOrInhibitedThreshold());

            if (e.getInfo() == EdgeType.Reactant) {
                for (Edge f : v.inEdges()) {
                    if (f.getInfo() == EdgeType.Reactant && edge2count.get(f) < reactantThreshold)
                        return emptyList;
                }
                for (Edge f : v.inEdges()) {
                    if (f.getInfo() == EdgeType.Reactant)
                        edge2count.decrement(f, reactantThreshold);
                }
                final ArrayList<Triplet<Edge, Boolean, String>> result = new ArrayList<>();
                for (Edge f : v.outEdges()) {
                    if (f.getInfo() == EdgeType.Product)
                        result.add(new Triplet<>(f, false, ((MoleculeType) f.getTarget().getInfo()).getName()));
                }
                return result;
            } else if (e.getInfo() == EdgeType.ReactantReversible) {
                for (Edge f : v.inEdges()) {
                    if (f.getInfo() == EdgeType.ReactantReversible && edge2count.get(f) < reactantThreshold)
                        return emptyList;
                }
                for (Edge f : v.inEdges()) {
                    if (f.getInfo() == EdgeType.ReactantReversible)
                        edge2count.decrement(f, reactantThreshold);
                }
                final ArrayList<Triplet<Edge, Boolean, String>> result = new ArrayList<>();
                for (Edge f : v.outEdges()) {
                    if (f.getInfo() == EdgeType.ProductReversible)
                        result.add(new Triplet<>(f, false, ((MoleculeType) f.getTarget().getInfo()).getName()));
                }
                return result;
            } else if (e.getInfo() == EdgeType.ProductReversible) {
                for (Edge f : v.outEdges()) {
                    if (e.getInfo() == EdgeType.ProductReversible && edge2count.get(f) < reactantThreshold)
                        return emptyList;
                }
                for (Edge f : v.outEdges()) {
                    if (f.getInfo() == EdgeType.ProductReversible)
                        edge2count.decrement(f, reactantThreshold);
                }
                final ArrayList<Triplet<Edge, Boolean, String>> result = new ArrayList<>();
                for (Edge f : v.inEdges()) {
                    if (f.getInfo() == EdgeType.ReactantReversible)
                        result.add(new Triplet<>(f, true, ((MoleculeType) f.getSource().getInfo()).getName()));
                }
                return result;
            }
        } else if (v.getInfo() instanceof ReactionGraphView.AndNode) {
            for (Edge f : v.inEdges()) {
                if (edge2count.get(f) <= 0)
                    return emptyList;
            }
            final StringBuilder buf = new StringBuilder();
            for (Edge f : v.inEdges()) {
                edge2count.decrement(f);
                if (buf.length() > 0)
                    buf.append("&");
                buf.append(((MoleculeType) f.getSource().getInfo()).getName());
            }
            final ArrayList<Triplet<Edge, Boolean, String>> result = new ArrayList<>();
            for (Edge f : Basic.randomize(v.outEdges(), random)) {
                return new ArrayList<>(Collections.singletonList(new Triplet<>(f, false, buf.toString())));
            }
            return result;
        } else if (v.getInfo() instanceof MoleculeType) {
            for (Edge f : Basic.randomize(v.adjacentEdges(), random)) {
                if (f.getInfo() == EdgeType.Reactant || f.getInfo() == EdgeType.ReactantReversible || f.getInfo() == EdgeType.Catalyst || (isAnimateInhibitions() && f.getInfo() == EdgeType.Inhibitor)) {
                    return new ArrayList<>(Collections.singletonList(new Triplet<>(f, false, ((MoleculeType) f.getSource().getInfo()).getName())));
                } else if (f.getInfo() == EdgeType.ProductReversible) {
                    return new ArrayList<>(Collections.singletonList(new Triplet<>(f, true, ((MoleculeType) f.getTarget().getInfo()).getName())));
                }
            }
        }
        return emptyList;
    }

    public boolean isAnimateInhibitions() {
        return animateInhibitions.get();
    }

    public BooleanProperty animateInhibitionsProperty() {
        return animateInhibitions;
    }

    public void setAnimateInhibitions(boolean animateInhibitions) {
        this.animateInhibitions.set(animateInhibitions);
    }

    public static double nextGaussian(Random random, double mean, double stdDev, boolean nonNegative) {
        final double result = random.nextGaussian() * stdDev + mean;
        if (nonNegative && result < 0)
            return 0;
        else
            return result;
    }
}
