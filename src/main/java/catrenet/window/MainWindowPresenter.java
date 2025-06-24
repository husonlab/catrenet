/*
 *  MainWindowPresenter.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.window;

import catrenet.action.*;
import catrenet.algorithm.*;
import catrenet.dialog.ExportReactionsForSelectedNodesDialog;
import catrenet.dialog.PolymerModelDialog;
import catrenet.dialog.exportlist.ExportList;
import catrenet.dialog.targets.TargetsDialog;
import catrenet.io.ModelIO;
import catrenet.io.NetworkIO;
import catrenet.io.Save;
import catrenet.io.SaveBeforeClosingDialog;
import catrenet.main.CheckForUpdate;
import catrenet.model.MoleculeType;
import catrenet.model.ReactionSystem;
import catrenet.settings.SettingsView;
import catrenet.tab.TabManager;
import catrenet.tab.TextTab;
import catrenet.view.*;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.util.*;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.FileUtils;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;

import static catrenet.io.ModelIO.FORMAL_FOOD;
import static catrenet.io.ModelIO.parseFood;

/**
 * setup all control bindings
 * Daniel Huson, 7.2019
 */
public class MainWindowPresenter {
    private boolean computeImportance = false;

    public MainWindowPresenter(MainWindow mainWindow) {
        final ObjectProperty<javafx.scene.Node> printableNode = new SimpleObjectProperty<>();

        final var controller = mainWindow.getController();
        final var tabManager = mainWindow.getTabManager();
        final var graphView = mainWindow.getReactionGraphView();

        final var disableGraphItems = new SimpleBooleanProperty(true);
        final var disableFullGraphItems = new SimpleBooleanProperty(true);
        disableFullGraphItems.bind(disableGraphItems.or(graphView.graphTypeProperty().isEqualTo(ReactionGraphView.Type.associationNetwork)
                        .or(graphView.graphTypeProperty().isEqualTo(ReactionGraphView.Type.reactantAssociationNetwork)))
                .or(mainWindow.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()));

        mainWindow.getInputReactionSystem().getReactions().addListener((InvalidationListener) e -> disableGraphItems.set(mainWindow.getInputReactionSystem().size() > 250));

        final var algorithmsRunning = new SimpleIntegerProperty(0);
        final ChangeListener<Boolean> runningListener = (c, o, n) -> {
            if (n)
                algorithmsRunning.set(algorithmsRunning.get() + 1);
            else
                algorithmsRunning.set(algorithmsRunning.get() - 1);
        };

        controller.getInputTextArea().undoableProperty().addListener((c, o, n) -> {
            if (n)
                mainWindow.getDocument().setDirty(true);
        });
        controller.getInputTextArea().focusedProperty().addListener((v, o, n) -> {
            if (!n && controller.getInputFoodTextArea().isUndoable()) {
                try {
                    ModelIO.read(mainWindow.getDocument().getInputReactionSystem(), new StringReader(controller.getInputTextArea().getText()), mainWindow.getDocument().getReactionNotation());
                } catch (IOException e) {
                    NotificationManager.showWarning("Failed to read reaction notation");
                }
            }
        });

        controller.getInputFoodTextArea().undoableProperty().addListener((c, o, n) -> {
            if (n)
                mainWindow.getDocument().setDirty(true);
        });
        controller.getInputFoodTextArea().focusedProperty().addListener((v, o, n) -> {
            if (!n && controller.getInputFoodTextArea().isUndoable()) {
                mainWindow.getInputReactionSystem().getFoods().setAll(parseFood(controller.getInputFoodTextArea().getText()));
            }
        });

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getRecentFilesMenu());

        if (mainWindow.getStage() != null) {
            mainWindow.getStage().setOnCloseRequest(e -> {
                controller.getCloseMenuItem().getOnAction().handle(null);
                e.consume();
            });
        }

        controller.getParsedReactionsTextArea().focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, controller.getParsedReactionsTextArea()));
        controller.getLogTextArea().focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, controller.getLogTextArea()));

        controller.getOutputTabPane().getTabs().addListener((ListChangeListener<Tab>) z -> {
            while (z.next()) {
                for (Tab tab : z.getAddedSubList()) {
                    if (tab.getUserData() instanceof TextTab textTab) {
                        final TextArea textArea = textTab.getTextArea();
                        textArea.focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, textArea));
                    }
                }
            }
        });

        controller.getNewMenuItem().setOnAction(e -> NewWindow.apply());

        controller.getNewPolymerModelMenuItem().setOnAction(e -> PolymerModelDialog.show(mainWindow));

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(mainWindow.getStage()));

        controller.getImportMenuItem().setOnAction(c -> ImportWimsFormat.apply(mainWindow.getStage()));

        controller.getExportSelectedNodesMenuItem().setOnAction(c -> ExportReactionsForSelectedNodesDialog.apply(mainWindow));
        controller.getExportSelectedNodesMenuItem().disableProperty().bind(graphView.getNodeSelection().emptyProperty());

        controller.getExportListOfReactionsMenuItem().setOnAction(c -> {
            final ExportList exportList = new ExportList(mainWindow);
            MainWindowManager.getInstance().addAuxiliaryWindow(mainWindow, exportList.getStage());
            exportList.getStage().show();
        });
        controller.getExportListOfReactionsMenuItem().disableProperty().bind(mainWindow.getInputReactionSystem().sizeProperty().isEqualTo(0));

        controller.getExportGraphGMLMenuItem().setOnAction(c -> {
            final var fileChooser = new FileChooser();
            fileChooser.setTitle("Export GML - " + ProgramProperties.getProgramVersion());
            var dir = new File(ProgramProperties.get("GMLDir", ""));
            if (dir.isDirectory())
                fileChooser.setInitialDirectory(dir);
            fileChooser.setInitialFileName(FileUtils.replaceFileSuffix(FileUtils.getFileNameWithoutPath(mainWindow.getDocument().getFileName()), ".gml"));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GML file", "*.gml"), TextFileFilter.getInstance());

            var file = fileChooser.showSaveDialog(mainWindow.getStage());
            if (file != null) {
                ProgramProperties.put("GMLDir", file.getParentFile());
                try (var w = FileUtils.getOutputWriterPossiblyZIPorGZIP(file.getPath())) {
                    NetworkIO.write(w, mainWindow.getDocument(), graphView);
                } catch (IOException ex) {
                    NotificationManager.showError("Save to GML failed: " + ex.getMessage());
                }
            }

        });
        controller.getExportGraphGMLMenuItem().disableProperty().bind(disableGraphItems);

        controller.getSaveMenuItem().setOnAction(e -> Save.showSaveDialog(mainWindow));

        controller.getCloseMenuItem().setOnAction(e -> {
            if (MainWindowManager.getInstance().size() > 1 && algorithmsRunning.get() > 0)
                NotificationManager.showWarning(algorithmsRunning.get() + " computation(s) running, please cancel before closing");
            else {
                graphView.getMoleculeFlowAnimation().setPlaying(false);
                if (SaveBeforeClosingDialog.apply(mainWindow) != SaveBeforeClosingDialog.Result.cancel) {
                    ProgramProperties.put("WindowGeometry", (new WindowGeometry(mainWindow.getStage())).toString());
                    MainWindowManager.getInstance().closeMainWindow(mainWindow);
                }
            }
        });
        controller.getCloseMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0));

        controller.getPageSetupMenuItem().setOnAction(e -> Print.showPageLayout(mainWindow.getStage()));

        controller.getPrintMenuItem().setOnAction(e -> {
            javafx.scene.Node node = printableNode.get();
            Print.print(mainWindow.getStage(), node);
        });
        controller.getPrintMenuItem().disableProperty().bind(printableNode.isNull());

        controller.getQuitMenuItem().setOnAction(e -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final MainWindow aWindow = (MainWindow) MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                if (SaveBeforeClosingDialog.apply(aWindow) == SaveBeforeClosingDialog.Result.cancel || !MainWindowManager.getInstance().closeMainWindow(aWindow))
                    break;
            }
        });

        // cut, copy, paste and undo/redo all implemented by TextArea controls

        controller.getCutMenuItem().setOnAction(e -> {
        });
        controller.getCutMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());

        controller.getCopyMenuItem().setOnAction(e -> {
            var tab = controller.getOutputTabPane().getSelectionModel().getSelectedItem();
            if (tab == controller.getNetworkTab()) {
                ClipboardUtils.put((!graphView.getNodeSelection().isEmpty() ? StringUtils.toString(graphView.getSelectedLabels(), "\n") : null),
                        controller.getNetworkScrollPane().getContent().snapshot(null, null), null);
            }
            if (tab instanceof TextTab textTab) {
                var text = textTab.getTextArea().getSelectedText();
                if (text == null || text.isBlank()) {
                    var system = mainWindow.getReactionSystem(textTab.getName());
                    if (system != null) {
                        var w = new StringWriter();
                        try {
                            ModelIO.write(system, w, true, mainWindow.getDocument().getReactionNotation(), mainWindow.getDocument().getArrowNotation());
                            text = w.toString();
                        } catch (IOException ignored) {
                            text = textTab.getTextArea().getText();
                        }
                    }
                }
                ClipboardUtils.putString(text);
            }
        });
        controller.getCopyMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())
                .or(disableGraphItems.not()).not()));

        controller.getPasteMenuItem().setOnAction(e -> {
        });
        controller.getPasteMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());

        if (false) {
            controller.getUndoMenuItem().setOnAction(e -> {
            });
            controller.getUndoMenuItem().disableProperty().bind(((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().undoableProperty()))
                    .or(controller.getInputFoodTextArea().undoableProperty())).not());

            controller.getRedoMenuItem().setOnAction(e -> {
                if (false) {
                    controller.getInputTextArea().requestFocus();
                    controller.getInputTextArea().redo();
                }
            });
            controller.getRedoMenuItem().disableProperty().bind(((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().redoableProperty()))
                    .or(controller.getInputFoodTextArea().undoableProperty())).not());
        } else {
            controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));
            controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));
        }

        controller.getComputeNetworkMenuItem().setOnAction(c -> {
            if (controller.getFullGraphRadioMenuItem().getToggleGroup().getSelectedToggle() == null) {
                controller.getFullGraphRadioMenuItem().setSelected(true);
            } else {
                disableGraphItems.set(mainWindow.getInputReactionSystem().size() >= 250);
                ComputeGraph.apply(mainWindow, controller);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
        });
        controller.getComputeNetworkMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0)
                .or(controller.getInputTextArea().textProperty().isEmpty())
                .or(controller.getInputFoodTextArea().textProperty().isEmpty())
                .or(disableGraphItems));

        disableGraphItems.addListener((c, o, n) -> {
            if (n)
                graphView.getMoleculeFlowAnimation().setPlaying(false);
        });

        controller.getNetworkTab().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty().or(disableGraphItems));
        controller.getNetworkTab().selectedProperty().addListener((v, o, n) -> {
            if (n && graphView.isEmpty() && !controller.getComputeNetworkMenuItem().isDisable())
                controller.getComputeNetworkMenuItem().getOnAction().handle(null);
        });

        graphView.emptyProperty().addListener((c, o, n) -> disableGraphItems.set(n && mainWindow.getInputReactionSystem().size() >= 250));

        controller.getRunRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new MaxRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunStrictlyAutocatalyticRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new StrictlyAutocatalyticMaxRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunStrictlyAutocatalyticRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMinRAFGeneratingElementMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                var molecules = new TreeSet<MoleculeType>();
                for (var r : mainWindow.getInputReactionSystem().getReactions()) {
                    molecules.addAll(r.getProducts());
                    molecules.addAll(r.getReactants());
                    molecules.addAll(r.getCatalystElements());
                }
                mainWindow.getInputReactionSystem().getFoods().forEach(molecules::remove);

                if (molecules.isEmpty()) {
                    NotificationManager.showWarning("No molecule types that are not contained in the food set");
                } else {
                    var moleculeNames = molecules.stream().map(MoleculeType::getName).toList();
                    var targetsDialog = new TargetsDialog(mainWindow.getStage(), moleculeNames);
                    var targetNames = targetsDialog.show();
                    if (!targetNames.isEmpty()) {
                        var algorithm = new MinRAFGeneratingElement();
                        algorithm.numberOfRandomInsertionOrdersProperty().set(targetsDialog.getRandomOrders());
                        for (var name : targetNames) {
                            algorithm.getTargets().add(MoleculeType.valueOf(name));
                        }
                        RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), algorithm, runningListener, true);
                    }
                }
            }
        });
        controller.getRunMinRAFGeneratingElementMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunCAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new MaxCAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunPseudoRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new MaxPseudoRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMinIRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new MinIRAFHeuristic(), runningListener, true);
            }
        });
        controller.getRunMinIRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunTrivialCAFsAlgorithmMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new TrivialCAFsAlgorithm(), runningListener, true);
            }
        });
        controller.getRunTrivialCAFsAlgorithmMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunTrivialRAFsAlgorithmMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new TrivialRAFsAlgorithm(), runningListener, true);
            }
        });
        controller.getRunTrivialRAFsAlgorithmMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunQuotientRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new QuotientRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunQuotientRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunCoreRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new CoreRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunCoreRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRemoveTrivialRAFsAlgorithmMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new RemoveTrivialRAFsAlgorithm(), runningListener, true);
            }
        });
        controller.getRemoveTrivialRAFsAlgorithmMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMuCAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                if (mainWindow.getInputReactionSystem().isInhibitorsPresent()) {
                    RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new MuCAFAlgorithm(), runningListener, true);
                } else
                    NotificationManager.showWarning("Won't run MU CAF algorithm, no inhibitions present");
            }
        });

        controller.getRunMuCAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(mainWindow.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunURAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(mainWindow)) {
                if (mainWindow.getInputReactionSystem().isInhibitorsPresent()) {
                    RunAlgorithm.apply(mainWindow, mainWindow.getInputReactionSystem(), new URAFAlgorithm(), runningListener, true);
                } else
                    NotificationManager.showWarning("Won't run U RAF algorithm, no inhibitions present");
            }
        });
        controller.getRunURAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(mainWindow.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunMuCAFMultipleTimesMenuItem().setOnAction(e -> RunMuCAFMultipleTimes.apply(mainWindow, controller, runningListener));
        controller.getRunMuCAFMultipleTimesMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(mainWindow.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getSpontaneousInRafMenuItem().setOnAction(e -> ComputeNecessarilySpontaneousInRAF.apply(mainWindow, mainWindow.getInputReactionSystem(), controller, runningListener));
        controller.getSpontaneousInRafMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getGreedyGrowMenuItem().setOnAction(e -> GreedilyGrowMaxCAF2MaxRAF.apply(mainWindow, mainWindow.getInputReactionSystem(), runningListener));
        controller.getGreedyGrowMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getReactionDependenciesMenuItem().setOnAction(e -> ComputeReactionDependencies.run(mainWindow));
        controller.getReactionDependenciesMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getMoleculeDependenciesMenuItem().setOnAction(e -> ComputeMoleculeDependencies.run(mainWindow));
        controller.getMoleculeDependenciesMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        var disableRunProperty = new SimpleBooleanProperty(false);
        disableRunProperty.bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getParsedReactionsTab().disableProperty().bind(controller.getParsedReactionsTextArea().textProperty().isEmpty());

        controller.getAboutMenuItem().setOnAction(e -> SplashScreen.showSplash(Duration.ofMinutes(2)));

        controller.getCheckForUpdatesMenuItem().setOnAction(e -> CheckForUpdate.apply("https://software-ab.cs.uni-tuebingen.de/download/catrenet"));
        CheckForUpdate.setupDisableProperty(controller.getCheckForUpdatesMenuItem().disableProperty());

        final DoubleProperty fontSize = new SimpleDoubleProperty(controller.getInputTextArea().getFont().getSize());
        setupFontSizeBindings(controller, tabManager, graphView, fontSize);
        controller.getIncreaseFontSizeMenuItem().setOnAction(e -> fontSize.set(fontSize.get() + 2));

        controller.getDecreaseFontSizeMenuItem().setOnAction(e -> fontSize.set(fontSize.get() - 2));
        controller.getDecreaseFontSizeMenuItem().disableProperty().bind(fontSize.lessThanOrEqualTo(4));

        controller.getLogTextArea().appendText(Basic.stopCollectingStdErr());

        {
            final ZoomableScrollPane scrollPane = controller.getNetworkScrollPane();
            scrollPane.setMouseScrollZoomFactor(1.05);
            scrollPane.setRequireShiftOrControlToZoom(true);
            scrollPane.setLockAspectRatio(true);
            scrollPane.setPannable(false);

            scrollPane.setUpdateScaleMethod(() -> {
                final double zoomX = scrollPane.getZoomFactorX();
                final double zoomY = scrollPane.getZoomFactorY();
                for (javafx.scene.Node node : BasicFX.getAllChildrenRecursively(graphView.getWorld().getChildren())) {
                    if (!node.translateXProperty().isBound() && node.getUserData() instanceof NodeView.NodeStyle)
                        node.setTranslateX(node.getTranslateX() * zoomX);
                    if (!node.translateYProperty().isBound() && node.getUserData() instanceof NodeView.NodeStyle)
                        node.setTranslateY(node.getTranslateY() * zoomY);
                }
            });

            final Pane networkPane = controller.getNetworkPane();
            networkPane.getChildren().add(graphView.getWorld());

            networkPane.setOnContextMenuRequested(e -> controller.getNetworkTabContextMenu().show(networkPane, e.getScreenX(), e.getScreenY()));

            networkPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
            networkPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

            controller.getZoomInMenuItem().setOnAction(c -> scrollPane.zoomBy(1.1, 1.1));
            controller.getZoomInMenuItem().disableProperty().bind(controller.getOutputTabPane().getSelectionModel().selectedItemProperty().isNotEqualTo(controller.getNetworkTab()));
            controller.getZoomOutMenuItem().setOnAction(c -> scrollPane.zoomBy(1 / 1.1, 1 / 1.1));
            controller.getZoomOutMenuItem().disableProperty().bind(controller.getZoomInMenuItem().disableProperty());

            controller.getZoomToFitMenuItem().setOnAction(c -> {
                scrollPane.resetZoom();

                Platform.runLater(() -> {
                    final Rectangle2D bbox = graphView.getBBox();
                    final double zoomX = Math.max(100.0, (controller.getNetworkBorderPane().getWidth())) / bbox.getWidth();
                    final double zoomY = Math.max(100.0, (controller.getNetworkBorderPane().getHeight())) / bbox.getHeight();
                    final double zoom = Math.min(zoomX, zoomY);

                    scrollPane.zoomBy(zoom, zoom);
                });
            });
            controller.getZoomToFitMenuItem().disableProperty().bind(controller.getZoomInMenuItem().disableProperty());

            // controller.getStatusFlowPane().prefHeightProperty().addListener((c,o,n)->System.err.println("PH changed: "+o+" -> "+n));
            // controller.getStatusFlowPane().heightProperty().addListener((c,o,n)->System.err.println("H changed: "+o+" -> "+n));

            networkPane.focusedProperty().addListener((c, o, n) -> {
                if (n)
                    printableNode.set(scrollPane.getContent());
            });
            controller.getNetworkTab().selectedProperty().addListener((c, o, n) -> {
                if (n)
                    printableNode.set(scrollPane.getContent());
            });

            networkPane.setOnMousePressed(e -> {
                    graphView.getNodeSelection().clearSelection();
                    graphView.getEdgeSelection().clearSelection();
            });

            controller.getAnimateCAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateRAFCheckMenuItem().setSelected(false);
                    controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
                    graphView.getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.MaxCAF);
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
                    graphView.getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.MaxRAF);
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
                    graphView.getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.MaxPseudoRAF);
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
            controller.getStopAnimationButton().disableProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty().not());
            controller.getStopAnimationButton().visibleProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty());

            controller.getAnimateNetworkMenuButton().disableProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty());
            graphView.getMoleculeFlowAnimation().animateInhibitionsProperty().bind(controller.getAnimateInhibitionsMenuItem().selectedProperty());
            controller.getAnimateInhibitionsMenuItem().disableProperty().bind(mainWindow.getInputReactionSystem().inhibitorsPresentProperty().not());

            controller.getMoveLabelsMenuItem().setSelected(graphView.getMoleculeFlowAnimation().isMoveLabels());
            controller.getMoveLabelsMenuItem().selectedProperty().bindBidirectional(graphView.getMoleculeFlowAnimation().moveLabelsProperty());
            controller.getMoveLabelsMenuItem().disableProperty().bind(disableFullGraphItems);

            controller.getUseColorsMenuItem().setSelected(graphView.getMoleculeFlowAnimation().isMultiColorMovingParts());
            controller.getUseColorsMenuItem().selectedProperty().bindBidirectional(graphView.getMoleculeFlowAnimation().multiColorMovingPartsProperty());
            controller.getUseColorsMenuItem().disableProperty().bind(disableFullGraphItems);
        }
        SelectionBindings.setup(mainWindow, controller);

        controller.getShowNodeLabels().setOnAction(e -> ShowHideNodeLabels.apply(graphView));
        controller.getShowNodeLabels().disableProperty().bind(disableGraphItems);
        if (mainWindow.getStage() != null)
            BasicFX.setupFullScreenMenuSupport(mainWindow.getStage(), controller.getFullScreenMenuItem());

        final var noGraphTypeSet = new RadioMenuItem();
        final var graphTypeButtonGroup = new ToggleGroup();
        graphTypeButtonGroup.getToggles().addAll(controller.getFullGraphRadioMenuItem(), controller.getReactantAssociationRadioMenuItem(), controller.getAssociationGraphRadioMenuItem(), controller.getReactantAssociationRadioMenuItem(), controller.getPrecedenceReactionDagMenuItem(), noGraphTypeSet);

        graphTypeButtonGroup.selectToggle(new RadioMenuItem());

        controller.getGraphTypeLabel().setText("");
        graphView.graphTypeProperty().addListener((c, o, n) -> controller.getGraphTypeLabel().setText(StringUtils.capitalizeFirstLetter(StringUtils.fromCamelCase(n.name()))));

        controller.getFullGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            if (n) {
                graphView.setGraphType(ReactionGraphView.Type.fullNetwork);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
                }
        );
        controller.getFullGraphRadioMenuItem().disableProperty().bind(disableRunProperty
                .or(mainWindow.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()).or(disableGraphItems));

        controller.getReactionDependencyGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            if (n) {
                graphView.setGraphType(ReactionGraphView.Type.reactionDependencyNetwork);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
        });
        controller.getReactionDependencyGraphRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty()
                .or(mainWindow.getDocument().reactionDependencyNetworkProperty().isNull()).or(disableGraphItems));

        controller.getMoleculeDependencyGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            if (n) {
                graphView.setGraphType(ReactionGraphView.Type.moleculeDependencyNetwork);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
        });
        controller.getMoleculeDependencyGraphRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty()
                .or(mainWindow.getDocument().moleculeDependencyNetworkProperty().isNull()).or(disableGraphItems));

        controller.getAssociationGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            if (n) {
                graphView.setGraphType(ReactionGraphView.Type.associationNetwork);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
        });
        controller.getAssociationGraphRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty().or(disableGraphItems));

        controller.getReactantAssociationRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            if (n) {
                graphView.setGraphType(ReactionGraphView.Type.reactantAssociationNetwork);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
        });
        controller.getReactantAssociationRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty().or(disableGraphItems));

        controller.getPrecedenceReactionDagMenuItem().selectedProperty().addListener((c, o, n) -> {
            if (n) {
                graphView.setGraphType(ReactionGraphView.Type.precedenceReactionNetwork);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }
        });
        controller.getPrecedenceReactionDagMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty().or(disableGraphItems));

        controller.getSuppressCatalystEdgesMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setSuppressCatalystEdges(n);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getSuppressCatalystEdgesMenuItem().disableProperty().bind(disableFullGraphItems.or(disableGraphItems));

        controller.getSuppressFormalFoodMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setSuppressFormalFood(n);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getSuppressFormalFoodMenuItem().disableProperty().bind(controller.getSuppressCatalystEdgesMenuItem().selectedProperty()
                .or(Bindings.createBooleanBinding(() -> !mainWindow.getInputReactionSystem().getFoods().contains(FORMAL_FOOD), mainWindow.getInputReactionSystem().getFoods())).or(disableGraphItems));


        controller.getUseMultiCopyFoodNodesMenuItem().selectedProperty().addListener((c, o, n) ->
        {
            graphView.setUseMultiCopyFoodNodes(n);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getUseMultiCopyFoodNodesMenuItem().disableProperty().bind(disableFullGraphItems.or(disableGraphItems));

        controller.getGraphEmbedderIterationsMenuItem().setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("" + graphView.getEmbeddingIterations());
            dialog.setTitle("Network Embedder Iterations Input");
            dialog.setHeaderText("Network Embedder Iterations Input");
            dialog.setContentText("Please enter number of iterations:");
            dialog.getEditor().textProperty().addListener((c, o, n) -> {
                if (!n.matches("\\d*"))
                    ((StringProperty) c).set(o);
            });

            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && NumberUtils.isInteger(result.get())) {
                graphView.setEmbeddingIterations(Math.max(10, NumberUtils.parseInt(result.get())));
            }
        });
        controller.getGraphEmbedderIterationsMenuItem().disableProperty().bind(disableGraphItems);

        controller.getComputeImportanceCheckMenuItem().setSelected(computeImportance); // this is a program-run parameter
        controller.getComputeImportanceCheckMenuItem().selectedProperty().addListener((c, o, n) -> computeImportance = n);
        controller.getComputeImportanceCheckMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        mainWindow.getInputReactionSystem().sizeProperty().addListener((c, o, n) -> controller.getInputReactionsSizeLabel().setText(String.format("%,d", n.intValue())));
        mainWindow.getInputReactionSystem().foodSizeProperty().addListener((c, o, n) -> {
            if (mainWindow.getInputReactionSystem().getFoods().contains(MoleculeType.valueOf("$")))
                controller.getInputFoodSizeLabel().setText(String.format("%,d (plus the formal item '$')", n.intValue() - 1));
            else
                controller.getInputFoodSizeLabel().setText(String.format("%,d", n.intValue()));
        });

        controller.getUseDarkThemeCheckMenuItem().selectedProperty().bindBidirectional(MainWindowManager.useDarkThemeProperty());

        SetupFind.apply(mainWindow);
        SetupExport.apply(mainWindow);

        selectLogTab(controller);

		controller.getClearLogMenuItem().setOnAction(e -> controller.getLogTextArea().clear());
		controller.getClearLogMenuItem().disableProperty().bind(controller.getLogTextArea().lengthProperty().isEqualTo(0));

        controller.getListFoodMenuItem().setOnAction(e -> reportList(mainWindow.getInputReactionSystem(), "Food", controller));
        controller.getListFoodMenuItem().disableProperty().bind(disableRunProperty);

        controller.getListReactionsMenuItem().setOnAction(e -> reportList(mainWindow.getInputReactionSystem(), "Reactions", controller));
        controller.getListReactionsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListReactantsMenuItem().setOnAction(e -> reportList(mainWindow.getInputReactionSystem(), "Reactants", controller));
        controller.getListReactantsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListProductsMenuItem().setOnAction(e -> reportList(mainWindow.getInputReactionSystem(), "Products", controller));
        controller.getListProductsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListCatalystsMenuItem().setOnAction(e -> reportList(mainWindow.getInputReactionSystem(), "Catalysts", controller));
        controller.getListCatalystsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListInhibitorsMenuItem().setOnAction(e -> reportList(mainWindow.getInputReactionSystem(), "Inhibitors", controller));
        controller.getListInhibitorsMenuItem().disableProperty().bind(disableRunProperty);

        controller.getStratifyReactionsAndMoleculesMenuItem().setOnAction(e -> {
            var text = Stratification.report(mainWindow.getInputReactionSystem());
            controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n" + text);
            selectLogTab(controller);
            Platform.runLater(() -> controller.getLogTextArea().setScrollTop(Double.MAX_VALUE));
        });

        controller.getUndoFoodButton().disableProperty().bind(controller.getInputFoodTextArea().undoableProperty().not());
        controller.getRedoFoodButton().disableProperty().bind(controller.getInputFoodTextArea().redoableProperty().not());
        controller.getUndoFoodButton().setOnAction(e -> controller.getInputFoodTextArea().undo());
        controller.getRedoFoodButton().setOnAction(e -> controller.getInputFoodTextArea().redo());

        controller.getUndoReactionsButton().disableProperty().bind(controller.getInputTextArea().undoableProperty().not());
        controller.getRedoReactionsButton().disableProperty().bind(controller.getInputTextArea().redoableProperty().not());
        controller.getUndoReactionsButton().setOnAction(e -> controller.getInputTextArea().undo());
        controller.getRedoReactionsButton().setOnAction(e -> controller.getInputTextArea().redo());

        {
            var settings = new SettingsView(mainWindow);
            var translate = new TranslateTransition(javafx.util.Duration.seconds(0.5), settings.getRoot());

            DraggableUtils.setupDragMouseTranslate(settings.getRoot());

            var translateX = new SimpleDoubleProperty(0.0);
            var translateY = new SimpleDoubleProperty(0.0);

            controller.getSettingsButton().selectedProperty().addListener((v, o, n) -> {
                if (n) {
                    AnchorPane.setLeftAnchor(settings.getRoot(), 5.0);
                    AnchorPane.setTopAnchor(settings.getRoot(), 45.0);
                    controller.getInnerAnchorPane().getChildren().add(settings.getRoot());
                    translate.setFromX(-450);
                    translate.setToX(translateX.get());
                    translate.setToY(translateY.get());
                    translate.setOnFinished(e -> {
                    });
                } else {
                    translateX.set(settings.getRoot().getTranslateX());
                    translateY.set(settings.getRoot().getTranslateY());
                    translate.setFromX(translateX.get());
                    translate.setToX(-450);
                    translate.setToY(0);
                    translate.setOnFinished(e -> controller.getInnerAnchorPane().getChildren().remove(settings.getRoot()));
                }
                translate.play();
            });
            controller.getSettingsButton().disableProperty().bind(translate.statusProperty().isEqualTo(Animation.Status.RUNNING));

            settings.getController().getTitledPane().setOnMouseClicked(e -> {
                if (e.getClickCount() > 1) {
                    controller.getSettingsButton().setSelected(false);
                    e.consume();
                }
            });
        }

        controller.getImportButton().setOnAction(e -> openString(ClipboardUtils.getTextFilesContentOrString()));
        controller.getImportButton().disableProperty().bind(ClipboardUtils.hasStringProperty().not().and(ClipboardUtils.hasFilesProperty().not()));

        SwipeUtils.setConsumeSwipes(controller.getRootPane());
        SwipeUtils.setConsumeSwipes(controller.getLogTab().getContent());
        SwipeUtils.setConsumeSwipes(controller.getNetworkTab().getContent());

        if (additionalCommonMenuSetup != null)
            additionalCommonMenuSetup.accept(mainWindow);
    }

    public static void selectLogTab(MainWindowController controller) {
        Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
    }

    private static void setupFontSizeBindings(MainWindowController controller, TabManager tabManager, ReactionGraphView graphView, DoubleProperty fontSize) {
        fontSize.addListener((c, o, n) -> {
            final var style = String.format("-fx-font-size: %.1f", n.doubleValue());

            graphView.setFontSize(n.doubleValue());
            graphView.setNodeLabelStyle(style);

            controller.getInputTextArea().setStyle(style);
            controller.getInputFoodTextArea().setStyle(style);
            controller.getParsedReactionsTextArea().setStyle(style);
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

    public static void reportList(ReactionSystem reactionSystem, String what, MainWindowController controller) {
        var textArea = controller.getLogTextArea();
        var lines = new ArrayList<String>();
        switch (what) {
            case "Food" -> {
                for (var molecule : reactionSystem.getFoods()) {
                    lines.add(molecule.getName());
                }
            }
            case "Reactions" -> {
                for (var reaction : reactionSystem.getReactions()) {
                    lines.add(reaction.getName());
                }
            }
            case "Reactants" -> {
                var set = new TreeSet<MoleculeType>();
                for (var reaction : reactionSystem.getReactions()) {
                    switch (reaction.getDirection()) {
                        case forward -> set.addAll(reaction.getReactants());
                        case reverse -> set.addAll(reaction.getProducts());
                        case both -> {
                            set.addAll(reaction.getReactants());
                            set.addAll(reaction.getProducts());
                        }
                    }
                }
                for (var molecule : set) {
                    lines.add(molecule.getName());
                }
            }
            case "Products" -> {
                var set = new TreeSet<MoleculeType>();
                for (var reaction : reactionSystem.getReactions()) {
                    switch (reaction.getDirection()) {
                        case forward -> set.addAll(reaction.getProducts());
                        case reverse -> set.addAll(reaction.getReactants());
                        case both -> {
                            set.addAll(reaction.getProducts());
                            set.addAll(reaction.getReactants());
                        }
                    }
                }
                for (var molecule : set) {
                    lines.add(molecule.getName());
                }
            }
            case "Catalysts" -> {
                var set = new TreeSet<MoleculeType>();
                for (var reaction : reactionSystem.getReactions()) {
                    set.addAll(reaction.getCatalystElements());
                }
                for (var molecule : set) {
                    lines.add(molecule.getName());
                }
            }
            case "Inhibitors" -> {
                var set = new TreeSet<MoleculeType>();
                for (var molecule : reactionSystem.getReactions()) {
                    set.addAll(molecule.getInhibitions());
                }
                for (var molecule : set) {
                    lines.add(molecule.getName());
                }
            }
        }
        textArea.setText(textArea.getText() + "%n%nList %s (%,d):%n".formatted(what, lines.size()) + StringUtils.toString(lines, "\n"));
        selectLogTab(controller);
        Platform.runLater(() -> textArea.setScrollTop(Double.MAX_VALUE));

    }

	public static void openString(String string) {
		if (string != null && !string.isBlank()) {
			try {
				var file = FileUtils.getUniqueFileName(ProgramProperties.get("SaveFileDir", System.getProperty("user.dir")), "Untitled", ".crs");
				FileUtils.writeLinesToFile(List.of(string), file.getPath(), false);
				FileOpenManager.getFileOpener().accept(file.getPath());
			} catch (IOException ignored) {
			}
		}
	}

    private Consumer<MainWindow> additionalCommonMenuSetup = null;

    public void setAdditionalCommonMenuSetup(Consumer<MainWindow> additionalCommonMenuSetup) {
        this.additionalCommonMenuSetup = additionalCommonMenuSetup;
    }
}
