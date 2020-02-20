/*
 * ControlBindings.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.window;

import catlynet.action.*;
import catlynet.algorithm.*;
import catlynet.format.FormatWindow;
import catlynet.io.ModelIO;
import catlynet.io.Save;
import catlynet.io.SaveBeforeClosingDialog;
import catlynet.model.ReactionSystem;
import catlynet.view.MoleculeFlowAnimation;
import catlynet.view.ReactionGraphView;
import catlynet.view.SelectionBindings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.util.BasicFX;
import jloda.fx.util.Print;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

import java.time.Duration;
import java.util.Arrays;

/**
 * setup all control bindings
 * Daniel Huson, 7.2019
 */
public class ControlBindings {

    public static void setup(MainWindow window) {
        final ObjectProperty<javafx.scene.Node> printableNode = new SimpleObjectProperty<>();

        final MainWindowController controller = window.getController();
        final ReactionGraphView graphView = window.getReactionGraphView();

        final IntegerProperty algorithmsRunning = new SimpleIntegerProperty(0);
        final ChangeListener<Boolean> runningListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> c, Boolean o, Boolean n) {
                if (n)
                    algorithmsRunning.set(algorithmsRunning.get() + 1);
                else
                    algorithmsRunning.set(algorithmsRunning.get() - 1);
            }
        };

        controller.getInputTextArea().undoableProperty().addListener((c, o, n) -> {
            if (n)
                window.getDocument().setDirty(true);
        });
        controller.getInputFoodTextArea().undoableProperty().addListener((c, o, n) -> {
            if (n)
                window.getDocument().setDirty(true);
        });

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getRecentFilesMenu());

        window.getStage().setOnCloseRequest((e) -> {
            controller.getCloseMenuItem().getOnAction().handle(null);
            e.consume();
        });

        controller.getNewMenuItem().setOnAction((e) -> NewWindow.apply());

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        controller.getImportMenuItem().setOnAction(c -> ImportWimsFormat.apply(window.getStage()));

        controller.getSaveMenItem().setOnAction(e -> Save.showSaveDialog(window));

        controller.getCloseMenuItem().setOnAction(e -> {
            if (MainWindowManager.getInstance().size() > 1 && algorithmsRunning.get() > 0)
                NotificationManager.showWarning(algorithmsRunning.get() + " computation(s) running, please cancel before closing");
            else {
                graphView.getMoleculeFlowAnimation().setPlaying(false);
                if (SaveBeforeClosingDialog.apply(window) != SaveBeforeClosingDialog.Result.cancel) {
                    ProgramProperties.put("WindowGeometry", (new WindowGeometry(window.getStage())).toString());
                    MainWindowManager.getInstance().closeMainWindow(window);
                }
            }
        });
        controller.getCloseMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0));


        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(window.getStage()));

        controller.getPrintMenuItem().setOnAction((e) -> {
            javafx.scene.Node node = printableNode.get();

            Print.print(window.getStage(), node);

        });
        controller.getPrintMenuItem().disableProperty().bind(printableNode.isNull());

        controller.getQuitMenuItem().setOnAction((e) -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final MainWindow aWindow = (MainWindow) MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                if (SaveBeforeClosingDialog.apply(aWindow) == SaveBeforeClosingDialog.Result.cancel)
                    break;
            }
        });

        controller.getInputFoodTextArea().setWrapText(true);

        // cut, copy, paste and undo/redo all implemented by TextArea controls

        controller.getCutMenuItem().setOnAction((e) -> {
        });
        controller.getCutMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());

        controller.getCopyMenuItem().setOnAction((e) -> {
            if (controller.getVisualizationTab().isSelected()) {
                final ClipboardContent content = new ClipboardContent();
                if (graphView.getNodeSelection().size() > 0)
                    content.putString(Basic.toString(graphView.getSelectedLabels(), "\n"));
                content.putImage(controller.getVisualizationScrollPane().getContent().snapshot(null, null));
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        controller.getCopyMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())
                .or(controller.getVisualizationTab().selectedProperty()).not()));

        controller.getPasteMenuItem().setOnAction((e) -> {
        });
        controller.getPasteMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());


        controller.getUndoMenuItem().setOnAction((e) -> {
        });
        controller.getUndoMenuItem().disableProperty().bind(
                ((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().undoableProperty()))
                        .or(controller.getInputFoodTextArea().undoableProperty())).not());

        controller.getRedoMenuItem().setOnAction((e) -> {
            if (false) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().redo();
            }
        });
        controller.getRedoMenuItem().disableProperty().bind(
                ((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().redoableProperty()))
                        .or(controller.getInputFoodTextArea().undoableProperty())).not());

        controller.getClearLogMenuItem().setOnAction(e -> controller.getLogTextArea().clear());
        controller.getClearLogMenuItem().disableProperty().bind(controller.getLogTextArea().textProperty().isEmpty());

        controller.getExpandInputMenuItem().setOnAction((e) -> {
            if (VerifyInput.verify(window)) {
                controller.getExpandedReactionsTab().getTabPane().getSelectionModel().select(controller.getExpandedReactionsTab());
                controller.getExpandedReactionsTextArea().setText(ModelIO.toString(window.getInputReactionSystem().computeExpandedSystem(), true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
                final String message = String.format("Input is valid. Found %,d reactions and %,d food items", window.getInputReactionSystem().getReactions().size(), window.getInputReactionSystem().getFoods().size());
                NotificationManager.showInformation(message);
                window.getLogStream().println(message);
            }
        });
        controller.getExpandInputMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(controller.getInputFoodTextArea().textProperty().isEmpty()));

        controller.getComputeVisualizationMenuItem().setOnAction(c -> {
            controller.getVisualizationTab().setDisable(false);
            controller.getVisualizationTab().getTabPane().getSelectionModel().select(controller.getVisualizationTab());
            ComputeGraph.apply(window, controller);
        });
        controller.getComputeVisualizationMenuItem().disableProperty().bind(controller.getExpandInputMenuItem().disableProperty());

        controller.getVisualizationTab().disableProperty().addListener((c, o, n) -> {
            if (n)
                graphView.getMoleculeFlowAnimation().setPlaying(false);
        });
        graphView.emptyProperty().addListener((c, o, n) -> controller.getVisualizationTab().setDisable(n));
        controller.getVisualizationTab().setDisable(true);


        controller.getFormatMenuItem().setOnAction((e) -> {
            Stage stage = null;
            for (Stage auxStage : MainWindowManager.getInstance().getAuxiliaryWindows(window)) {
                if (auxStage.getTitle().contains("ReactionNotation")) {
                    stage = auxStage;
                    break;
                }
            }
            if (stage == null) {
                final FormatWindow formatWindow = new FormatWindow(window);
                stage = formatWindow.getStage();
            }
            stage.setIconified(false);
            stage.toFront();
        });

        controller.getRunRAFMenuItem().setOnAction((e) -> {
            if (VerifyInput.verify(window)) {
                controller.getOutputTabPane().getSelectionModel().select(controller.getRafTab());
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxRAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.maxRAF), controller.getRafTextArea(), runningListener);
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunCAFMenuItem().setOnAction((e) -> {
            if (VerifyInput.verify(window)) {
                controller.getOutputTabPane().getSelectionModel().select(controller.getCafTab());
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxCAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.maxCAF), controller.getCafTextArea(), runningListener);
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunPseudoRAFMenuItem().setOnAction((e) -> {
            if (VerifyInput.verify(window)) {
                controller.getOutputTabPane().getSelectionModel().select(controller.getPseudoRafTab());
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxPseudoRAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.maxPseudoRAF), controller.getPseudoRAFTextArea(), runningListener);
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));


        controller.getRunMuCAFMenuItem().setOnAction((e) -> {
            if (VerifyInput.verify(window)) {
                if (window.getInputReactionSystem().isInhibitorsPresent()) {
                    controller.getOutputTabPane().getSelectionModel().select(controller.getMuCafTab());
                    RunAlgorithm.apply(window, window.getInputReactionSystem(), new MuCAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.muCAF), controller.getMuCafTextArea(), runningListener);
                } else
                    NotificationManager.showWarning("Won't run MU CAF algorithm, no inhibitions present");
            }
        });

        controller.getRunMuCAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunURAFMenuItem().setOnAction((e) -> {
            if (VerifyInput.verify(window)) {
                if (window.getInputReactionSystem().isInhibitorsPresent()) {
                    controller.getOutputTabPane().getSelectionModel().select(controller.getuRAFTab());
                    RunAlgorithm.apply(window, window.getInputReactionSystem(), new URAFAlgorithm(), window.getReactionSystem(ReactionSystem.Type.uRAF), controller.getMuCafTextArea(), runningListener);
                } else
                    NotificationManager.showWarning("Won't run U RAF algorithm, no inhibitions present");
            }
        });
        controller.getRunURAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunMuCAFMultipleTimesMenuItem().setOnAction((e) -> RunMuCAFMultipleTimes.apply(window, controller, runningListener));
        controller.getRunMuCAFMultipleTimesMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunMenuItem().setOnAction((e) -> {
            RunAll.apply(window, controller, runningListener);
            ComputeGraph.apply(window, controller);
        });

        controller.getRunMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunButton().setOnAction(controller.getRunMenuItem().getOnAction());
        controller.getRunButton().disableProperty().bind(controller.getRunMenuItem().disableProperty());

        controller.getExpandedReactionsTab().disableProperty().bind(controller.getExpandedReactionsTextArea().textProperty().isEmpty());
        controller.getRafTab().disableProperty().bind(controller.getRafTextArea().textProperty().isEmpty());
        controller.getCafTab().disableProperty().bind(controller.getCafTextArea().textProperty().isEmpty());
        controller.getMuCafTab().disableProperty().bind(controller.getMuCafTextArea().textProperty().isEmpty().or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));
        controller.getuRAFTab().disableProperty().bind(controller.getuRAFTextArea().textProperty().isEmpty().or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));
        controller.getPseudoRafTab().disableProperty().bind(controller.getPseudoRAFTextArea().textProperty().isEmpty());

        controller.getAboutMenuItem().setOnAction((e) -> SplashScreen.showSplash(Duration.ofMinutes(2)));

        controller.getCheckForUpdatesMenuItem().setOnAction((e) -> CheckForUpdate.apply());
        MainWindowManager.getInstance().changedProperty().addListener((c, o, n) -> controller.getCheckForUpdatesMenuItem().disableProperty().set(MainWindowManager.getInstance().size() > 1
                || (MainWindowManager.getInstance().size() == 1 && !MainWindowManager.getInstance().getMainWindow(0).isEmpty())));

        window.getStage().widthProperty().addListener((c, o, n) -> {
            if (!Double.isNaN(o.doubleValue()) && n.doubleValue() > 0)
                controller.getMainSplitPane().setDividerPosition(0, controller.getMainSplitPane().getDividerPositions()[0] * o.doubleValue() / n.doubleValue());
        });
        if (window.getStage().getWidth() > 0)
            controller.getMainSplitPane().setDividerPosition(0, 200.0 / window.getStage().getWidth());

        final DoubleProperty fontSize = new SimpleDoubleProperty(controller.getInputTextArea().getFont().getSize());
        setupFontSizeBindings(controller, graphView, fontSize);
        controller.getIncreaseFontSizeMenuItem().setOnAction(e -> fontSize.set(fontSize.get() + 2));

        controller.getDecreaseFontSizeMenuItem().setOnAction(e -> fontSize.set(fontSize.get() - 2));
        controller.getDecreaseFontSizeMenuItem().disableProperty().bind(fontSize.lessThanOrEqualTo(4));

        controller.getLogTextArea().appendText(Basic.stopCollectingStdErr());

        for (TextArea textArea : Arrays.asList(controller.getInputTextArea(), controller.getExpandedReactionsTextArea(), controller.getLogTextArea(), controller.getCafTextArea(), controller.getRafTextArea(), controller.getPseudoRAFTextArea(), controller.getMuCafTextArea(), controller.getuRAFTextArea())) {
            textArea.focusedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getWrapTextMenuItem().setDisable(false);
                    controller.getWrapTextMenuItem().selectedProperty().bindBidirectional(textArea.wrapTextProperty());
                    printableNode.set(textArea);

                } else {
                    controller.getWrapTextMenuItem().setDisable(true);
                    controller.getWrapTextMenuItem().selectedProperty().unbindBidirectional(textArea.wrapTextProperty());
                }
            });
        }

        {
            final ZoomableScrollPane scrollPane = controller.getVisualizationScrollPane();
            scrollPane.setMouseScrollZoomFactor(1.05);
            scrollPane.setRequireShiftOrControlToZoom(true);
            scrollPane.setLockAspectRatio(true);
            scrollPane.setPannable(false);

            scrollPane.setUpdateScaleMethod(() -> {
                final double zoomX = scrollPane.getZoomFactorX();
                final double zoomY = scrollPane.getZoomFactorY();
                for (javafx.scene.Node node : BasicFX.getAllChildrenRecursively(graphView.getWorld().getChildren())) {
                    if (!node.translateXProperty().isBound())
                        node.setTranslateX(node.getTranslateX() * zoomX);
                    if (!node.translateYProperty().isBound())
                        node.setTranslateY(node.getTranslateY() * zoomY);
                }
            });

            final Pane visualizationContentPane = new StackPane(graphView.getWorld());
            visualizationContentPane.setStyle("-fx-background-color: white;");
            scrollPane.setContent(visualizationContentPane);

            visualizationContentPane.setOnContextMenuRequested((e) -> controller.getVisualizationTabContextMenu().show(visualizationContentPane, e.getScreenX(), e.getScreenY()));

            visualizationContentPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
            visualizationContentPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

            controller.getZoomInMenuItem().setOnAction(c -> scrollPane.zoomBy(1.1, 1.1));
            controller.getZoomInMenuItem().disableProperty().bind(controller.getOutputSplittableTabPane().getSelectionModel().selectedItemProperty().isNotEqualTo(controller.getVisualizationTab()));
            controller.getZoomOutMenuItem().setOnAction(c -> scrollPane.zoomBy(1 / 1.1, 1 / 1.1));
            controller.getZoomOutMenuItem().disableProperty().bind(controller.getZoomInMenuItem().disableProperty());

            controller.getZoomToFitMenuItem().setOnAction(c -> {
                scrollPane.resetZoom();

                Platform.runLater(() -> {
                    final Rectangle2D bbox = graphView.getBBox();
                    final double zoomX = Math.max(100.0, (controller.getVisualizationBorderPane().getWidth() - 100.0)) / bbox.getWidth();
                    final double zoomY = Math.max(100.0, (controller.getVisualizationBorderPane().getHeight() - 100.0)) / bbox.getHeight();
                    System.err.println("zoomX: " + zoomX + " zoomY: " + zoomY);
                    final double zoom = Math.min(zoomX, zoomY);

                    scrollPane.zoomBy(zoom, zoom);
                });
            });
            controller.getZoomToFitMenuItem().disableProperty().bind(controller.getZoomInMenuItem().disableProperty());

            // controller.getStatusFlowPane().prefHeightProperty().addListener((c,o,n)->System.err.println("PH changed: "+o+" -> "+n));
            // controller.getStatusFlowPane().heightProperty().addListener((c,o,n)->System.err.println("H changed: "+o+" -> "+n));

            visualizationContentPane.focusedProperty().addListener((c, o, n) -> {
                if (n)
                    printableNode.set(scrollPane.getContent());
            });
            controller.getVisualizationTab().selectedProperty().addListener((c, o, n) -> {
                if (n)
                    printableNode.set(scrollPane.getContent());
            });

            visualizationContentPane.setOnMousePressed((e) -> {
                if (e.getClickCount() == 2) {
                    graphView.getNodeSelection().clearSelection();
                    graphView.getEdgeSelection().clearSelection();
                }
            });

            controller.getAnimateCAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateRAFCheckMenuItem().setSelected(false);
                    controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
                    graphView.getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.CAF);
                    graphView.getMoleculeFlowAnimation().setPlaying(true);
                } else {
                    graphView.getMoleculeFlowAnimation().setPlaying(false);
                }
            });
            controller.getAnimateCAFCheckMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty().or(graphView.getMoleculeFlowAnimation().playingProperty()));

            controller.getAnimateCAFContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateCAFCheckMenuItem().selectedProperty());
            controller.getAnimateCAFContextMenuItem().disableProperty().bind(controller.getAnimateCAFCheckMenuItem().disableProperty());

            controller.getAnimateRAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateCAFCheckMenuItem().setSelected(false);
                    controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
                    graphView.getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.RAF);
                    graphView.getMoleculeFlowAnimation().setPlaying(true);
                } else {
                    graphView.getMoleculeFlowAnimation().setPlaying(false);
                }
            });
            controller.getAnimateRAFCheckMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty().or(graphView.getMoleculeFlowAnimation().playingProperty()));

            controller.getAnimateRAFContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateRAFCheckMenuItem().selectedProperty());
            controller.getAnimateRAFContextMenuItem().disableProperty().bind(controller.getAnimateRAFCheckMenuItem().disableProperty());

            controller.getAnimateMaxRAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateCAFCheckMenuItem().setSelected(false);
                    controller.getAnimateRAFCheckMenuItem().setSelected(false);
                    graphView.getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.PseudoRAF);
                    graphView.getMoleculeFlowAnimation().setPlaying(true);
                } else {
                    graphView.getMoleculeFlowAnimation().setPlaying(false);
                }
            });
            controller.getAnimateMaxRAFCheckMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty().or(graphView.getMoleculeFlowAnimation().playingProperty()));

            controller.getAnimatePseudoRAFContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateMaxRAFCheckMenuItem().selectedProperty());
            controller.getAnimatePseudoRAFContextMenuItem().disableProperty().bind(controller.getAnimateMaxRAFCheckMenuItem().disableProperty());

            controller.getStopAnimationMenuItem().setOnAction(e -> {
                graphView.getMoleculeFlowAnimation().setPlaying(false);
                controller.getSelectAllMenuItem().getOnAction().handle(null);
                controller.getSelectNoneMenuItem().getOnAction().handle(null);
                controller.getAnimateCAFCheckMenuItem().setSelected(false);
                controller.getAnimateRAFCheckMenuItem().setSelected(false);
                controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
            });
            controller.getStopAnimationMenuItem().disableProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty().not());

            controller.getStopAnimationContextMenuItem().setOnAction(controller.getStopAnimationMenuItem().getOnAction());
            controller.getStopAnimationContextMenuItem().disableProperty().bind(controller.getStopAnimationMenuItem().disableProperty());

            controller.getStopAnimationButton().setVisible(false);
            controller.getStopAnimationButton().setOnAction(controller.getStopAnimationMenuItem().getOnAction());
            graphView.getMoleculeFlowAnimation().playingProperty().addListener((c, o, n) -> controller.getStopAnimationButton().setVisible(n));
            controller.getStopAnimationButton().textProperty().bind(graphView.getMoleculeFlowAnimation().modelProperty().asString().concat(" animation"));

            graphView.getMoleculeFlowAnimation().animateInhibitionsProperty().bind(controller.getAnimateInhibitionsMenuItem().selectedProperty());
            controller.getAnimateInhibitionsContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateInhibitionsMenuItem().selectedProperty());
            controller.getAnimateInhibitionsMenuItem().disableProperty().bind(window.getInputReactionSystem().inhibitorsPresentProperty().not());
            controller.getAnimateInhibitionsContextMenuItem().disableProperty().bind(controller.getAnimateInhibitionsMenuItem().disableProperty());
        }
        SelectionBindings.setup(window, controller);

        controller.getShowNodeLabels().setOnAction(e -> ShowHideNodeLabels.apply(graphView));
        BasicFX.setupFullScreenMenuSupport(window.getStage(), controller.getFullScreenMenuItem());

        controller.getSuppressCatalystEdgesMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setSuppressCatalystEdges(n);
            graphView.update();
        });
        controller.getSuppressCatalystEdgesMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty());

        controller.getUseMultiCopyFoodNodesMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setUseMultiCopyFoodNodes(n);
            graphView.update();
        });
        controller.getUseMultiCopyFoodNodesMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty());

        SetupFind.apply(window);

        Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
    }

    private static void setupFontSizeBindings(MainWindowController controller, ReactionGraphView graphView, DoubleProperty fontSize) {
        fontSize.addListener((c, o, n) -> {
            final String style = String.format("-fx-font-size: %.1f", n.doubleValue());

            if (controller.getOutputSplittableTabPane().getSelectionModel().getSelectedItem().equals(controller.getVisualizationTab())) {
                graphView.setNodeStyle(style);
            } else {
                controller.getInputTextArea().setStyle(style);
                controller.getInputFoodTextArea().setStyle(style);
                controller.getExpandedReactionsTextArea().setStyle(style);
                controller.getLogTextArea().setStyle(style);
                controller.getCafTextArea().setStyle(style);
                controller.getRafTextArea().setStyle(style);
                controller.getPseudoRAFTextArea().setStyle(style);
                controller.getMuCafTextArea().setStyle(style);
            }
        });
    }

}
