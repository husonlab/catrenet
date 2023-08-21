/*
 * MoleculeFlowAnimation.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.view;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;
import jloda.fx.util.ColorSchemeManager;
import jloda.fx.util.SelectionEffect;
import jloda.graph.*;
import jloda.util.CollectionUtils;
import jloda.util.IteratorUtils;
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

    public enum Model {MaxRAF, MaxCAF, MaxPseudoRAF}

    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final ObjectProperty<Model> model = new SimpleObjectProperty<>(Model.MaxRAF);

    private final IntegerProperty uncatalyzedOrInhibitedThreshold = new SimpleIntegerProperty(20);

    private final BooleanProperty animateInhibitions = new SimpleBooleanProperty(false);

    private final BooleanProperty moveLabels = new SimpleBooleanProperty(false);
    private final BooleanProperty multiColorMovingParts = new SimpleBooleanProperty(true);

    private final ObservableList<Color> colorScheme = FXCollections.observableArrayList(ColorSchemeManager.getInstance().getColorScheme("Retro29"));

    /**
     * setup molecule flow simulation
     *
     */
    public MoleculeFlowAnimation(Graph graph, NodeSet foodNodes, EdgeArray<EdgeView> edge2Group, Group world) {
        final EdgeIntArray edge2totalCount = new EdgeIntArray(graph);
        final EdgeIntArray edge2currentCount = new EdgeIntArray(graph);

        // this service pumps molecules into the system
        final Service<Boolean> service = new Service<>() {
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        int count = 0;

                        while (!isCancelled()) {
                            Thread.sleep(Math.round(nextGaussian(random, 200, 20, true)));
                            count++;
                            for (var v : CollectionUtils.randomize(foodNodes, random)) {
                                for (var e : IteratorUtils.randomize(v.adjacentEdges(), random)) {
                                    if (e.getInfo() == EdgeType.Reactant || e.getInfo() == EdgeType.ReactantReversible || e.getInfo() == EdgeType.Catalyst) {
                                        final var label = ((MoleculeType) e.getSource().getInfo()).getName();
                                        Platform.runLater(() -> animateEdge(e, false, label, edge2totalCount, edge2currentCount, edge2Group, world));
                                        break;
                                    } else if (e.getInfo() == EdgeType.ProductReversible) {
                                        final var label = ((MoleculeType) e.getTarget().getInfo()).getName();
                                        Platform.runLater(() -> animateEdge(e, true, label, edge2totalCount, edge2currentCount, edge2Group, world));
                                        break;
                                    }
                                }
                            }
                            // in a pseudo RAF, need to pump molecules into none-food nodes, as well, to get things going
                            if (getModel() == Model.MaxPseudoRAF && count == getUncatalyzedOrInhibitedThreshold()) {
                                count = 0;

                                loop:
                                for (var v : IteratorUtils.randomize(graph.nodes(), random)) {
                                    if (!foodNodes.contains(v) && v.getInfo() instanceof MoleculeType) {
                                        for (var e : IteratorUtils.randomize(v.adjacentEdges(), random)) {
                                            if (e.getInfo() == EdgeType.Reactant || e.getInfo() == EdgeType.ReactantReversible || e.getInfo() == EdgeType.Catalyst) {
                                                final var label = ((MoleculeType) e.getSource().getInfo()).getName();
                                                Platform.runLater(() -> animateEdge(e, false, label, edge2totalCount, edge2currentCount, edge2Group, world));
                                                break loop;
                                            } else if (e.getInfo() == EdgeType.ProductReversible) {
                                                final var label = ((MoleculeType) e.getTarget().getInfo()).getName();
                                                Platform.runLater(() -> animateEdge(e, true, label, edge2totalCount, edge2currentCount, edge2Group, world));
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

        service.setOnFailed(e ->
                System.err.println("Failed: " + service.getException()));

        playing.addListener((c, o, n) -> {
            if (n)
                service.restart();
            else {
                service.cancel();
                edge2totalCount.clear();
            }
        });

        multiColorMovingParts.addListener((c, o, n) -> {
            if (n)
                colorScheme.setAll(ColorSchemeManager.getInstance().getColorScheme("Retro29"));
            else
                colorScheme.setAll(Color.GRAY);
        });
    }

    /**
     * animate an edge
     *
     */
    private void animateEdge(Edge edge, boolean reverse, String label, EdgeIntArray edge2totalCount, EdgeIntArray edge2currentCount, EdgeArray<EdgeView> edge2view, Group world) {
        if (edge.getOwner() == null || edge2currentCount.getInt(edge) >= 10)
            return;

        final Path path = ReactionGraphView.getPath(edge2view.get(edge));

        if (path != null) {
            final Shape movingPart;
            if (isMoveLabels()) {
                var text = new Text(label);
                text.setFont(ReactionGraphView.getFont());
                text.setFill(colorScheme.get(Math.abs(label.hashCode()) % colorScheme.size()));
                movingPart = text;
            } else {
                var shape = new Rectangle(7, 5);
                var color = colorScheme.get(Math.abs(label.hashCode()) % colorScheme.size());
                shape.setFill(color);
                shape.setStroke(color);
                shape.setMouseTransparent(true);
                movingPart = shape;
            }

            var pathTransition = new PathTransition(Duration.seconds(2), path, movingPart);
            if (!isMoveLabels())
                pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);

            edge2currentCount.increment(edge);

            pathTransition.setOnFinished(e -> {
                world.getChildren().remove(movingPart);
                if (edge.getOwner() != null) {
                    edge2totalCount.increment(edge);
                    edge2currentCount.decrement(edge);
                }
                if (edge.getOwner() != null && playing.get()) {
                    path.setEffect(SelectionEffect.create(((Color) movingPart.getFill()).deriveColor(1, 1, 1, 0.2)));

                    for (Triplet<Edge, Boolean, String> triplet : computeEdgesReadyToFire(edge, reverse ? edge.getSource() : edge.getTarget(), edge2totalCount)) {
                        animateEdge(triplet.getFirst(), triplet.getSecond(), triplet.getThird(), edge2totalCount, edge2currentCount, edge2view, world);
                    }
                } else
                    path.setEffect(null);

            });

            if (reverse) {
                pathTransition.setRate(-pathTransition.getRate());
                pathTransition.jumpTo(pathTransition.getDuration());
            }

            final PauseTransition pauseTransition = new PauseTransition(Duration.millis(200));
            pauseTransition.setOnFinished(z -> {
                pathTransition.play();
                Platform.runLater(() -> world.getChildren().add(movingPart));
            });
            pauseTransition.play();
        }
    }

    /**
     * compute all edges ready to fire
     *
     * @return edges ready to fire
     */
    private ArrayList<Triplet<Edge, Boolean, String>> computeEdgesReadyToFire(Edge e, Node v, EdgeIntArray edge2count) {
        final ArrayList<Triplet<Edge, Boolean, String>> emptyList = new ArrayList<>();

        if (v.getInfo() instanceof Reaction) {
            boolean hasCatalyst = false;
            boolean hasInhibitor = false;

            for (Edge f : v.inEdges()) {
                if (f.getInfo() == EdgeType.Catalyst && edge2count.getInt(f) > 0) {
                    hasCatalyst = true;
                } else if (isAnimateInhibitions() && f.getInfo() == EdgeType.Inhibitor && edge2count.getInt(f) > 0) {
                    hasInhibitor = true;
                }
            }
            // in a CAF, reaction always requires a catalyst and can never run with inhibition
            // in other cases, run at lower rate
            final int reactantThreshold = (hasCatalyst && !hasInhibitor ? 1 : getModel() == Model.MaxCAF ? Integer.MAX_VALUE : getUncatalyzedOrInhibitedThreshold());

            if (e.getInfo() == EdgeType.Reactant) {
                for (Edge f : v.inEdges()) {
                    if (f.getInfo() == EdgeType.Reactant && edge2count.getInt(f) < reactantThreshold)
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
                    if (f.getInfo() == EdgeType.ReactantReversible) {
                        if (edge2count.getInt(f) < reactantThreshold)
                            return emptyList;
                    }
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
                    if (e.getInfo() == EdgeType.ProductReversible && edge2count.getInt(f) < reactantThreshold)
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
                if (edge2count.getInt(f) <= 0)
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
			for (Edge f : IteratorUtils.randomize(v.outEdges(), random)) {
				return new ArrayList<>(Collections.singletonList(new Triplet<>(f, false, buf.toString())));
			}
            return result;
        } else if (v.getInfo() instanceof MoleculeType) {
			for (Edge f : IteratorUtils.randomize(v.adjacentEdges(), random)) {
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

    public boolean isMoveLabels() {
        return moveLabels.get();
    }

    public BooleanProperty moveLabelsProperty() {
        return moveLabels;
    }

    public void setMoveLabels(boolean moveLabels) {
        this.moveLabels.set(moveLabels);
    }

    public boolean isMultiColorMovingParts() {
        return multiColorMovingParts.get();
    }

    public BooleanProperty multiColorMovingPartsProperty() {
        return multiColorMovingParts;
    }

    public void setMultiColorMovingParts(boolean multiColorMovingParts) {
        this.multiColorMovingParts.set(multiColorMovingParts);
    }
}
