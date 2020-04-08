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
import catlynet.main.CheckForUpdate;
import catlynet.tab.TabManager;
import catlynet.tab.TextTab;
import catlynet.view.MoleculeFlowAnimation;
import catlynet.view.ReactionGraphView;
import catlynet.view.SelectionBindings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import java.util.Optional;

/**
 * setup all control bindings
 * Daniel Huson, 7.2019
 */
public class ControlBindings {

    public static void setup(MainWindow window) {
        final ObjectProperty<javafx.scene.Node> printableNode = new SimpleObjectProperty<>();

        final MainWindowController controller = window.getController();
        final TabManager tabManager = window.getTabManager();
        final ReactionGraphView graphView = window.getReactionGraphView();

        final BooleanProperty disableGraphItems = new SimpleBooleanProperty(true);
        final BooleanProperty disableFullGraphItems = new SimpleBooleanProperty(true);
        disableFullGraphItems.bind(disableGraphItems.or(graphView.graphTypeProperty().isEqualTo(ReactionGraphView.Type.dependencyGraph).or(graphView.graphTypeProperty().isEqualTo(ReactionGraphView.Type.reactantDependencyGraph))));

        final IntegerProperty algorithmsRunning = new SimpleIntegerProperty(0);
        final ChangeListener<Boolean> runningListener = (c, o, n) -> {
            if (n)
                algorithmsRunning.set(algorithmsRunning.get() + 1);
            else
                algorithmsRunning.set(algorithmsRunning.get() - 1);
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

        window.getStage().setOnCloseRequest(e -> {
            controller.getCloseMenuItem().getOnAction().handle(null);
            e.consume();
        });

        controller.getExpandedReactionsTextArea().focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, controller.getExpandedReactionsTextArea()));
        controller.getLogTextArea().focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, controller.getLogTextArea()));

        controller.getOutputTabPane().getTabs().addListener((ListChangeListener<Tab>) z -> {
            while (z.next()) {
                for (Tab tab : z.getAddedSubList()) {
                    if (tab.getUserData() instanceof TextTab) {
                        final TextArea textArea = ((TextTab) tab.getUserData()).getTextArea();
                        textArea.focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, textArea));
                    }
                }
            }
        });

        controller.getNewMenuItem().setOnAction(e -> NewWindow.apply());

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        controller.getImportMenuItem().setOnAction(c -> ImportWimsFormat.apply(window.getStage()));

        controller.getExportSelectedNodesMenuItem().setOnAction(c -> ExportManager.exportNodes(window));
        controller.getExportSelectedNodesMenuItem().disableProperty().bind(graphView.getNodeSelection().emptyProperty());

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


        controller.getPageSetupMenuItem().setOnAction(e -> Print.showPageLayout(window.getStage()));

        controller.getPrintMenuItem().setOnAction(e -> {
            javafx.scene.Node node = printableNode.get();

            Print.print(window.getStage(), node);

        });
        controller.getPrintMenuItem().disableProperty().bind(printableNode.isNull());

        controller.getQuitMenuItem().setOnAction(e -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final MainWindow aWindow = (MainWindow) MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                if (SaveBeforeClosingDialog.apply(aWindow) == SaveBeforeClosingDialog.Result.cancel)
                    break;
            }
        });

        controller.getInputFoodTextArea().setWrapText(true);

        // cut, copy, paste and undo/redo all implemented by TextArea controls

        controller.getCutMenuItem().setOnAction(e -> {
        });
        controller.getCutMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());

        controller.getCopyMenuItem().setOnAction(e -> {
            if (controller.getVisualizationTab().getTabPane().isFocused() && controller.getVisualizationTab().isSelected()) {
                final ClipboardContent content = new ClipboardContent();
                if (graphView.getNodeSelection().size() > 0)
                    content.putString(Basic.toString(graphView.getSelectedLabels(), "\n"));
                content.putImage(controller.getVisualizationScrollPane().getContent().snapshot(null, null));
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        controller.getCopyMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())
                .or(disableGraphItems.not()).not()));

        controller.getPasteMenuItem().setOnAction(e -> {
        });
        controller.getPasteMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());


        controller.getUndoMenuItem().setOnAction(e -> {
        });
        controller.getUndoMenuItem().disableProperty().bind(
                ((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().undoableProperty()))
                        .or(controller.getInputFoodTextArea().undoableProperty())).not());

        controller.getRedoMenuItem().setOnAction(e -> {
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

        controller.getExpandInputMenuItem().setOnAction(e -> {
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
            disableGraphItems.set(false);
            controller.getVisualizationTab().getTabPane().getSelectionModel().select(controller.getVisualizationTab());
            ComputeGraph.apply(window, controller);
        });
        controller.getComputeVisualizationMenuItem().disableProperty().bind(controller.getExpandInputMenuItem().disableProperty());

        disableGraphItems.addListener((c, o, n) -> {
            if (n)
                graphView.getMoleculeFlowAnimation().setPlaying(false);
        });
        graphView.emptyProperty().addListener((c, o, n) -> disableGraphItems.set(n));
        controller.getVisualizationTab().disableProperty().bind(disableGraphItems);

        controller.getFormatMenuItem().setOnAction(e -> {
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

        controller.getRunRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxRAFAlgorithm(), runningListener);
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunCAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxCAFAlgorithm(), runningListener);
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunPseudoRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxPseudoRAFAlgorithm(), runningListener);
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMinIrrRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MinIrrRAFHeuristic(), runningListener);
            }
        });
        controller.getRunMinIrrRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunQuotientRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new QuotientRAFAlgorithm(), runningListener);
            }
        });
        controller.getRunQuotientRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMuCAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                if (window.getInputReactionSystem().isInhibitorsPresent()) {
                    RunAlgorithm.apply(window, window.getInputReactionSystem(), new MuCAFAlgorithm(), runningListener);
                } else
                    NotificationManager.showWarning("Won't run MU CAF algorithm, no inhibitions present");
            }
        });

        controller.getRunMuCAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunURAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                if (window.getInputReactionSystem().isInhibitorsPresent()) {
                    RunAlgorithm.apply(window, window.getInputReactionSystem(), new URAFAlgorithm(), runningListener);
                } else
                    NotificationManager.showWarning("Won't run U RAF algorithm, no inhibitions present");
            }
        });
        controller.getRunURAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));


        controller.getRunMuCAFMultipleTimesMenuItem().setOnAction(e -> RunMuCAFMultipleTimes.apply(window, controller, runningListener));
        controller.getRunMuCAFMultipleTimesMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));


        controller.getSpontaneousInRafMenuItem().setOnAction(e -> ComputeNecessarilySpontaneousInRAF.apply(window, window.getInputReactionSystem(), controller, runningListener));
        controller.getSpontaneousInRafMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getGreedyGrowMenuItem().setOnAction(e -> GreedilyGrowMaxCAF2MaxRAF.apply(window, window.getInputReactionSystem(), runningListener));
        controller.getGreedyGrowMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMenuItem().setOnAction(e -> {
            RunAll.apply(window, runningListener);
            ComputeGraph.apply(window, controller);
        });

        controller.getRunMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunButton().setOnAction(controller.getRunMenuItem().getOnAction());
        controller.getRunButton().disableProperty().bind(controller.getRunMenuItem().disableProperty());

        controller.getExpandedReactionsTab().disableProperty().bind(controller.getExpandedReactionsTextArea().textProperty().isEmpty());

        controller.getAboutMenuItem().setOnAction(e -> SplashScreen.showSplash(Duration.ofMinutes(2)));

        controller.getCheckForUpdatesMenuItem().setOnAction(e -> CheckForUpdate.apply());

        MainWindowManager.getInstance().changedProperty().addListener((c, o, n) -> controller.getCheckForUpdatesMenuItem().disableProperty().set(MainWindowManager.getInstance().size() > 1
                || (MainWindowManager.getInstance().size() == 1 && !MainWindowManager.getInstance().getMainWindow(0).isEmpty())));

        window.getStage().widthProperty().addListener((c, o, n) -> {
            if (!Double.isNaN(o.doubleValue()) && n.doubleValue() > 0)
                controller.getMainSplitPane().setDividerPosition(0, controller.getMainSplitPane().getDividerPositions()[0] * o.doubleValue() / n.doubleValue());
        });
        if (window.getStage().getWidth() > 0)
            controller.getMainSplitPane().setDividerPosition(0, 200.0 / window.getStage().getWidth());

        final DoubleProperty fontSize = new SimpleDoubleProperty(controller.getInputTextArea().getFont().getSize());
        setupFontSizeBindings(controller, tabManager, graphView, fontSize);
        controller.getIncreaseFontSizeMenuItem().setOnAction(e -> fontSize.set(fontSize.get() + 2));

        controller.getDecreaseFontSizeMenuItem().setOnAction(e -> fontSize.set(fontSize.get() - 2));
        controller.getDecreaseFontSizeMenuItem().disableProperty().bind(fontSize.lessThanOrEqualTo(4));

        controller.getLogTextArea().appendText(Basic.stopCollectingStdErr());


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

            visualizationContentPane.setOnContextMenuRequested(e -> controller.getVisualizationTabContextMenu().show(visualizationContentPane, e.getScreenX(), e.getScreenY()));

            visualizationContentPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
            visualizationContentPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

            controller.getZoomInMenuItem().setOnAction(c -> scrollPane.zoomBy(1.1, 1.1));
            controller.getZoomInMenuItem().disableProperty().bind(controller.getOutputTabPane().getSelectionModel().selectedItemProperty().isNotEqualTo(controller.getVisualizationTab()));
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

            visualizationContentPane.setOnMousePressed(e -> {
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
            controller.getAnimateCAFCheckMenuItem().disableProperty().bind(disableFullGraphItems.or(graphView.getMoleculeFlowAnimation().playingProperty()));

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
            controller.getAnimateRAFCheckMenuItem().disableProperty().bind(disableFullGraphItems.or(graphView.getMoleculeFlowAnimation().playingProperty()));

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
            controller.getAnimateMaxRAFCheckMenuItem().disableProperty().bind(disableFullGraphItems.or(graphView.getMoleculeFlowAnimation().playingProperty()));

            controller.getStopAnimationMenuItem().setOnAction(e -> {
                graphView.getMoleculeFlowAnimation().setPlaying(false);
                controller.getSelectAllMenuItem().getOnAction().handle(null);
                controller.getSelectNoneMenuItem().getOnAction().handle(null);
                controller.getAnimateCAFCheckMenuItem().setSelected(false);
                controller.getAnimateRAFCheckMenuItem().setSelected(false);
                controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
            });
            controller.getStopAnimationMenuItem().disableProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty().not());

            controller.getStopAnimationButton().setVisible(false);
            controller.getStopAnimationButton().setOnAction(controller.getStopAnimationMenuItem().getOnAction());
            graphView.getMoleculeFlowAnimation().playingProperty().addListener((c, o, n) -> controller.getStopAnimationButton().setVisible(n));
            controller.getStopAnimationButton().textProperty().bind(graphView.getMoleculeFlowAnimation().modelProperty().asString().concat(" animation"));

            graphView.getMoleculeFlowAnimation().animateInhibitionsProperty().bind(controller.getAnimateInhibitionsMenuItem().selectedProperty());
            controller.getAnimateInhibitionsMenuItem().disableProperty().bind(window.getInputReactionSystem().inhibitorsPresentProperty().not());
        }
        SelectionBindings.setup(window, controller);

        controller.getShowNodeLabels().setOnAction(e -> ShowHideNodeLabels.apply(graphView));
        BasicFX.setupFullScreenMenuSupport(window.getStage(), controller.getFullScreenMenuItem());

        final RadioMenuItem noGraphTypeSet = new RadioMenuItem();
        final ToggleGroup graphTypeButtonGroup = new ToggleGroup();
        graphTypeButtonGroup.getToggles().addAll(controller.getFullGraphRadioMenuItem(), controller.getDependencyGraphRadioMenuItem(), controller.getReactantDependencyGraphRadioMenuItem(), noGraphTypeSet);

        graphTypeButtonGroup.selectToggle(new RadioMenuItem());

        controller.getGraphTypeLabel().setText("");
        graphView.graphTypeProperty().addListener((c, o, n) -> controller.getGraphTypeLabel().setText(Basic.capitalizeFirstLetter(Basic.fromCamelCase(n.name()))));

        controller.getFullGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> graphView.setGraphType(ReactionGraphView.Type.fullGraph));
        controller.getFullGraphRadioMenuItem().disableProperty().bind(controller.getRunMenuItem().disableProperty());

        controller.getDependencyGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> graphView.setGraphType(ReactionGraphView.Type.dependencyGraph));
        controller.getDependencyGraphRadioMenuItem().disableProperty().bind(controller.getRunMenuItem().disableProperty());

        controller.getReactantDependencyGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> graphView.setGraphType(ReactionGraphView.Type.reactantDependencyGraph));
        controller.getReactantDependencyGraphRadioMenuItem().disableProperty().bind(controller.getRunMenuItem().disableProperty());

        controller.getSuppressCatalystEdgesMenuItem().selectedProperty().addListener((c, o, n) -> graphView.setSuppressCatalystEdges(n));
        controller.getSuppressCatalystEdgesMenuItem().disableProperty().bind(disableFullGraphItems);

        controller.getUseMultiCopyFoodNodesMenuItem().selectedProperty().addListener((c, o, n) -> graphView.setUseMultiCopyFoodNodes(n));
        controller.getUseMultiCopyFoodNodesMenuItem().disableProperty().bind(disableFullGraphItems);

        controller.getGraphEmbedderIterationsMenuItem().setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("" + graphView.getEmbeddingIterations());
            dialog.setTitle("Graph Embedder Iterations Input");
            dialog.setHeaderText("Graph Embedder Iterations Input");
            dialog.setContentText("Please enter number of iterations:");
            dialog.getEditor().textProperty().addListener((c, o, n) -> {
                if (!n.matches("\\d*"))
                    ((StringProperty) c).set(o);
            });

            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && Basic.isInteger(result.get())) {
                graphView.setEmbeddingIterations(Math.max(10, Basic.parseInt(result.get())));
            }
        });

        SetupFind.apply(window);

        Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
    }

    private static void setupFontSizeBindings(MainWindowController controller, TabManager tabManager, ReactionGraphView graphView, DoubleProperty fontSize) {
        fontSize.addListener((c, o, n) -> {
            final String style = String.format("-fx-font-size: %.1f", n.doubleValue());

            graphView.setNodeLabelStyle(style);

            controller.getInputTextArea().setStyle(style);
            controller.getInputFoodTextArea().setStyle(style);
            controller.getExpandedReactionsTextArea().setStyle(style);
            controller.getLogTextArea().setStyle(style);

            for (TextTab textTab : tabManager.textTabs()) {
                textTab.getTextArea().setStyle(style);
            }
        });
    }

    private static ChangeListener<Boolean> textAreaFocusChangeListener(MainWindowController controller, ObjectProperty<Node> printableNode, TextArea textArea) {
        return (c, o, n) -> {
            if (n) {
                controller.getWrapTextMenuItem().setDisable(false);
                controller.getWrapTextMenuItem().selectedProperty().bindBidirectional(textArea.wrapTextProperty());
                printableNode.set(textArea);

            } else {
                controller.getWrapTextMenuItem().setDisable(true);
                controller.getWrapTextMenuItem().selectedProperty().unbindBidirectional(textArea.wrapTextProperty());
            }
        };
    }
}
