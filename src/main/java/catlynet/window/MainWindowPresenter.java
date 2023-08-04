/*
 * MainWindowPresenter.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.window;

import catlynet.action.*;
import catlynet.algorithm.*;
import catlynet.dialog.exportlist.ExportList;
import catlynet.format.FormatWindow;
import catlynet.io.Save;
import catlynet.io.SaveBeforeClosingDialog;
import catlynet.main.CheckForUpdate;
import catlynet.model.MoleculeType;
import catlynet.model.ReactionSystem;
import catlynet.tab.TabManager;
import catlynet.tab.TextTab;
import catlynet.vformat.VFormatWindow;
import catlynet.view.MoleculeFlowAnimation;
import catlynet.view.NodeView;
import catlynet.view.ReactionGraphView;
import catlynet.view.SelectionBindings;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
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
import javafx.stage.Stage;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.dialog.ExportImageDialog;
import jloda.fx.util.*;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeSet;

import static catlynet.io.ModelIO.FORMAL_FOOD;

/**
 * setup all control bindings
 * Daniel Huson, 7.2019
 */
public class MainWindowPresenter {
    private static boolean computeImportance = false;

    private static Stage vFormatWindowStage = null;

    public static void setup(MainWindow window) {
        final ObjectProperty<javafx.scene.Node> printableNode = new SimpleObjectProperty<>();

        final var controller = window.getController();
        final var tabManager = window.getTabManager();
        final var graphView = window.getReactionGraphView();

        final var disableGraphItems = new SimpleBooleanProperty(true);
        final var disableFullGraphItems = new SimpleBooleanProperty(true);
        disableFullGraphItems.bind(disableGraphItems.or(graphView.graphTypeProperty().isEqualTo(ReactionGraphView.Type.associationNetwork)
                        .or(graphView.graphTypeProperty().isEqualTo(ReactionGraphView.Type.reactantAssociationNetwork)))
                .or(window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()));

        final var algorithmsRunning = new SimpleIntegerProperty(0);
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

        controller.getWorkingReactionsTextArea().focusedProperty().addListener(textAreaFocusChangeListener(controller, printableNode, controller.getWorkingReactionsTextArea()));
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

        controller.getExportListOfReactionsMenuItem().setOnAction(c -> {
            final ExportList exportList = new ExportList(window);
            MainWindowManager.getInstance().addAuxiliaryWindow(window, exportList.getStage());
            exportList.getStage().show();
        });
        controller.getExportListOfReactionsMenuItem().disableProperty().bind(window.getInputReactionSystem().sizeProperty().isEqualTo(0));


        var recentFilesInvalidationListener = (InvalidationListener) e -> {
            controller.getRecentMenuButton().getItems().clear();
            for (var menuItem : controller.getRecentFilesMenu().getItems()) {
                var newMenuItem = new MenuItem(menuItem.getText());
                newMenuItem.setOnAction(menuItem.getOnAction());
                controller.getRecentMenuButton().getItems().add(newMenuItem);
            }
        };
        recentFilesInvalidationListener.invalidated(null);
        controller.getRecentFilesMenu().getItems().addListener(recentFilesInvalidationListener);
        controller.getRecentFilesMenu().disableProperty().bind(Bindings.isEmpty(controller.getRecentFilesMenu().getItems()));


        controller.getExportMenu().getItems().addListener((InvalidationListener) e -> {
            controller.getExportMenuButton().getItems().clear();
            var afterSeparator = false;
            for (var menuItem : controller.getExportMenu().getItems()) {
                if (menuItem instanceof SeparatorMenuItem)
                    afterSeparator = true;
                else if (afterSeparator) {
                    var newMenuItem = new MenuItem(menuItem.getText());
                    newMenuItem.setOnAction(menuItem.getOnAction());
                    controller.getExportMenuButton().getItems().add(newMenuItem);
                }
            }
        });
        controller.getExportMenuButton().disableProperty().bind(Bindings.isEmpty(controller.getExportMenuButton().getItems()));


        controller.getSaveMenuItem().setOnAction(e -> Save.showSaveDialog(window));

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
                if (SaveBeforeClosingDialog.apply(aWindow) == SaveBeforeClosingDialog.Result.cancel || !MainWindowManager.getInstance().closeMainWindow(aWindow))
                    break;
            }
        });

        controller.getExportNetworkMenuButton().disableProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty().or(graphView.emptyProperty()));

        // cut, copy, paste and undo/redo all implemented by TextArea controls

        controller.getCutMenuItem().setOnAction(e -> {
        });
        controller.getCutMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getInputFoodTextArea().focusedProperty())).not());

        controller.getCopyMenuItem().setOnAction(e -> {
            if (controller.getNetworkTab().getTabPane().isFocused() && controller.getNetworkTab().isSelected()) {
                final ClipboardContent content = new ClipboardContent();
                if (graphView.getNodeSelection().size() > 0)
                    content.putString(StringUtils.toString(graphView.getSelectedLabels(), "\n"));
                content.putImage(controller.getNetworkScrollPane().getContent().snapshot(null, null));
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

        controller.getComputeNetworkMenuItem().setOnAction(c -> {
            if (controller.getFullGraphRadioMenuItem().getToggleGroup().getSelectedToggle() == null) {
                controller.getFullGraphRadioMenuItem().setSelected(true);
            } else {
                disableGraphItems.set(false);
                ComputeGraph.apply(window, controller);
                controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
            }

        });
        controller.getComputeNetworkMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(controller.getInputFoodTextArea().textProperty().isEmpty()));

        disableGraphItems.addListener((c, o, n) -> {
            if (n)
                graphView.getMoleculeFlowAnimation().setPlaying(false);
        });


        controller.getNetworkTab().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());
        controller.getNetworkTab().selectedProperty().addListener((v, o, n) -> {
            if (n && graphView.isEmpty() && !controller.getComputeNetworkMenuItem().isDisable())
                controller.getComputeNetworkMenuItem().getOnAction().handle(null);
        });

        graphView.emptyProperty().addListener((c, o, n) -> disableGraphItems.set(n));

        controller.getFormatMenuItem().setOnAction(e -> {
            Stage stage = null;
            for (Stage auxStage : MainWindowManager.getInstance().getAuxiliaryWindows(window)) {
                if (auxStage.getTitle().contains(FormatWindow.title)) {
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

        controller.getShowNodeAndEdgeFormatMenuItem().setOnAction(e -> {

            if (vFormatWindowStage == null) {
                final VFormatWindow formatWindow = new VFormatWindow(window);
                vFormatWindowStage = formatWindow.getStage();
            }
            vFormatWindowStage.setIconified(false);
            vFormatWindowStage.show();
            vFormatWindowStage.toFront();
        });

        controller.getRunRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getRunStrictlyAutocatalyticRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new StrictlyAutocatalyticMaxRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunStrictlyAutocatalyticRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunCAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxCAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunPseudoRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MaxPseudoRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMinIrrRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new MinIrrRAFHeuristic(), runningListener, true);
            }
        });
        controller.getRunMinIrrRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunTrivialCAFsAlgorithmMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new TrivialCAFsAlgorithm(), runningListener, true);
            }
        });
        controller.getRunTrivialCAFsAlgorithmMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunTrivialRAFsAlgorithmMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new TrivialRAFsAlgorithm(), runningListener, true);
            }
        });
        controller.getRunTrivialRAFsAlgorithmMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunQuotientRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new QuotientRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunQuotientRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunCoreRAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new CoreRAFAlgorithm(), runningListener, true);
            }
        });
        controller.getRunCoreRAFMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRemoveTrivialRAFsAlgorithmMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                RunAlgorithm.apply(window, window.getInputReactionSystem(), new RemoveTrivialRAFsAlgorithm(), runningListener, true);
            }
        });
        controller.getRemoveTrivialRAFsAlgorithmMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getRunMuCAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                if (window.getInputReactionSystem().isInhibitorsPresent()) {
                    RunAlgorithm.apply(window, window.getInputReactionSystem(), new MuCAFAlgorithm(), runningListener, true);
                } else
                    NotificationManager.showWarning("Won't run MU CAF algorithm, no inhibitions present");
            }
        });

        controller.getRunMuCAFMenuItem().disableProperty().bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()).or(window.getInputReactionSystem().inhibitorsPresentProperty().not()));

        controller.getRunURAFMenuItem().setOnAction(e -> {
            if (VerifyInput.verify(window)) {
                if (window.getInputReactionSystem().isInhibitorsPresent()) {
                    RunAlgorithm.apply(window, window.getInputReactionSystem(), new URAFAlgorithm(), runningListener, true);
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

        controller.getReactionDependenciesMenuItem().setOnAction(e -> ComputeReactionDependencies.run(window));
        controller.getReactionDependenciesMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());

        controller.getMoleculeDependenciesMenuItem().setOnAction(e -> ComputeMoleculeDependencies.run(window));
        controller.getMoleculeDependenciesMenuItem().disableProperty().bind(controller.getRunRAFMenuItem().disableProperty());


        var disableRunProperty = new SimpleBooleanProperty(false);
        disableRunProperty.bind(algorithmsRunning.isNotEqualTo(0).or(controller.getInputTextArea().textProperty().isEmpty()));

        controller.getWorkingReactionsTab().disableProperty().bind(controller.getWorkingReactionsTextArea().textProperty().isEmpty());

        controller.getAboutMenuItem().setOnAction(e -> SplashScreen.showSplash(Duration.ofMinutes(2)));

        controller.getCheckForUpdatesMenuItem().setOnAction(e -> CheckForUpdate.apply());

        MainWindowManager.getInstance().changedProperty().addListener((c, o, n) -> controller.getCheckForUpdatesMenuItem().disableProperty().set(MainWindowManager.getInstance().size() > 1
                                                                                                                                                 || (MainWindowManager.getInstance().size() == 1 && !MainWindowManager.getInstance().getMainWindow(0).isEmpty())));

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

            controller.getExportImageNetworkMenuItem().setOnAction(e -> ExportImageDialog.show(window.getDocument().getFileName(), window.getStage(), controller.getNetworkScrollPane().getContent()));
            controller.getExportImageNetworkMenuItem().disableProperty().bind(disableGraphItems);

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
                if (e.getClickCount() == 2) {
                    graphView.getNodeSelection().clearSelection();
                    graphView.getEdgeSelection().clearSelection();
                }
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
            graphView.getMoleculeFlowAnimation().playingProperty().addListener((c, o, n) -> controller.getStopAnimationButton().setVisible(n));
            graphView.getMoleculeFlowAnimation().modelProperty().addListener((v, o, n) -> {
                controller.getStopAnimationButton().setText(n == null ? "Stop" : StringUtils.fromCamelCase(n.name()) + " animation");
            });
            {
                var model = graphView.getMoleculeFlowAnimation().getModel();
                controller.getStopAnimationButton().setText(model == null ? "Stop" : StringUtils.fromCamelCase(model.name()) + " animation");
            }

            controller.getAnimateNetworkMenuButton().disableProperty().bind(graphView.getMoleculeFlowAnimation().playingProperty());

            graphView.getMoleculeFlowAnimation().animateInhibitionsProperty().bind(controller.getAnimateInhibitionsMenuItem().selectedProperty());
            controller.getAnimateInhibitionsMenuItem().disableProperty().bind(window.getInputReactionSystem().inhibitorsPresentProperty().not());

            controller.getMoveLabelsMenuItem().setSelected(graphView.getMoleculeFlowAnimation().isMoveLabels());
            controller.getMoveLabelsMenuItem().selectedProperty().bindBidirectional(graphView.getMoleculeFlowAnimation().moveLabelsProperty());
            controller.getMoveLabelsMenuItem().disableProperty().bind(disableFullGraphItems);

            controller.getUseColorsMenuItem().setSelected(graphView.getMoleculeFlowAnimation().isMultiColorMovingParts());
            controller.getUseColorsMenuItem().selectedProperty().bindBidirectional(graphView.getMoleculeFlowAnimation().multiColorMovingPartsProperty());
            controller.getUseColorsMenuItem().disableProperty().bind(disableFullGraphItems);
        }
        SelectionBindings.setup(window, controller);

        controller.getShowNodeLabels().setOnAction(e -> ShowHideNodeLabels.apply(graphView));
        BasicFX.setupFullScreenMenuSupport(window.getStage(), controller.getFullScreenMenuItem());

        final var noGraphTypeSet = new RadioMenuItem();
        final var graphTypeButtonGroup = new ToggleGroup();
        graphTypeButtonGroup.getToggles().addAll(controller.getFullGraphRadioMenuItem(), controller.getReactantAssociationRadioMenuItem(), controller.getAssociationGraphRadioMenuItem(), controller.getReactantAssociationRadioMenuItem(), noGraphTypeSet);

        graphTypeButtonGroup.selectToggle(new RadioMenuItem());

        controller.getGraphTypeLabel().setText("");
        graphView.graphTypeProperty().addListener((c, o, n) -> controller.getGraphTypeLabel().setText(StringUtils.capitalizeFirstLetter(StringUtils.fromCamelCase(n.name()))));

        controller.getFullGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setGraphType(ReactionGraphView.Type.fullNetwork);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
                }
        );
        controller.getFullGraphRadioMenuItem().disableProperty().bind(disableRunProperty.or(window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()));

        controller.getReactionDependencyGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setGraphType(ReactionGraphView.Type.reactionDependencyNetwork);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getReactionDependencyGraphRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty().or(window.getDocument().reactionDependencyNetworkProperty().isNull()));

        controller.getMoleculeDependencyGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setGraphType(ReactionGraphView.Type.moleculeDependencyNetwork);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getMoleculeDependencyGraphRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty().or(window.getDocument().moleculeDependencyNetworkProperty().isNull()));


        controller.getAssociationGraphRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setGraphType(ReactionGraphView.Type.associationNetwork);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getAssociationGraphRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty());

        controller.getReactantAssociationRadioMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setGraphType(ReactionGraphView.Type.reactantAssociationNetwork);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getReactantAssociationRadioMenuItem().disableProperty().bind(controller.getFullGraphRadioMenuItem().disableProperty());

        controller.getSuppressCatalystEdgesMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setSuppressCatalystEdges(n);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getSuppressCatalystEdgesMenuItem().disableProperty().bind(disableFullGraphItems);

        controller.getSuppressFormalFoodMenuItem().selectedProperty().addListener((c, o, n) -> {
            graphView.setSuppressFormalFood(n);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getSuppressFormalFoodMenuItem().disableProperty().bind(controller.getSuppressCatalystEdgesMenuItem().selectedProperty().or(Bindings.createBooleanBinding(() -> !window.getInputReactionSystem().getFoods().contains(FORMAL_FOOD), window.getInputReactionSystem().getFoods())));


        controller.getUseMultiCopyFoodNodesMenuItem().selectedProperty().addListener((c, o, n) ->
        {
            graphView.setUseMultiCopyFoodNodes(n);
            controller.getNetworkTab().getTabPane().getSelectionModel().select(controller.getNetworkTab());
        });
        controller.getUseMultiCopyFoodNodesMenuItem().disableProperty().bind(disableFullGraphItems);

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

        controller.getComputeImportanceCheckMenuItem().setSelected(computeImportance); // this is a program-run parameter
        controller.getComputeImportanceCheckMenuItem().selectedProperty().addListener((c, o, n) -> computeImportance = n);

        window.getInputReactionSystem().sizeProperty().addListener((c, o, n) -> controller.getInputReactionsSizeLabel().setText(String.format("%,d", n.intValue())));
        window.getInputReactionSystem().foodSizeProperty().addListener((c, o, n) -> {
            if (window.getInputReactionSystem().getFoods().contains(MoleculeType.valueOf("$")))
                controller.getInputFoodSizeLabel().setText(String.format("%,d (plus the formal item '$')", n.intValue() - 1));
            else
                controller.getInputFoodSizeLabel().setText(String.format("%,d", n.intValue()));
        });

        controller.getUseDarkThemeCheckMenuItem().selectedProperty().bindBidirectional(MainWindowManager.useDarkThemeProperty());

        SetupFind.apply(window);

        selectLogTab(controller);


        controller.getListFoodMenuItem().setOnAction(e -> reportList(window.getInputReactionSystem(), "Food", controller));
        controller.getListFoodMenuItem().disableProperty().bind(disableRunProperty);

        controller.getListReactionsMenuItem().setOnAction(e -> reportList(window.getInputReactionSystem(), "Reactions", controller));
        controller.getListReactionsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListReactantsMenuItem().setOnAction(e -> reportList(window.getInputReactionSystem(), "Reactants", controller));
        controller.getListReactantsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListProductsMenuItem().setOnAction(e -> reportList(window.getInputReactionSystem(), "Products", controller));
        controller.getListProductsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListCatalystsMenuItem().setOnAction(e -> reportList(window.getInputReactionSystem(), "Catalysts", controller));
        controller.getListCatalystsMenuItem().disableProperty().bind(disableRunProperty);
        controller.getListInhibitorsMenuItem().setOnAction(e -> reportList(window.getInputReactionSystem(), "Inhibitors", controller));
        controller.getListInhibitorsMenuItem().disableProperty().bind(disableRunProperty);

    }

    public static void selectLogTab(MainWindowController controller) {
        Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
    }

    private static void setupFontSizeBindings(MainWindowController controller, TabManager tabManager, ReactionGraphView graphView, DoubleProperty fontSize) {
        fontSize.addListener((c, o, n) -> {
            final String style = String.format("-fx-font-size: %.1f", n.doubleValue());

            graphView.setNodeLabelStyle(style);

            controller.getInputTextArea().setStyle(style);
            controller.getInputFoodTextArea().setStyle(style);
            controller.getWorkingReactionsTextArea().setStyle(style);
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
}
