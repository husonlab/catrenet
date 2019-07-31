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

import catlynet.action.CheckForUpdate;
import catlynet.action.NewWindow;
import catlynet.action.ParseInput;
import catlynet.action.RunAlgorithm;
import catlynet.algorithm.MaxCAFAlgorithm;
import catlynet.algorithm.MaxPseudoRAFAlgorithm;
import catlynet.algorithm.MaxRAFAlgorithm;
import catlynet.format.FormatWindow;
import catlynet.io.ModelIO;
import catlynet.io.Save;
import catlynet.io.SaveChangesDialog;
import catlynet.view.MoleculeFlowAnimation;
import catlynet.view.SelectionBindings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.TextAreaSearcher;
import jloda.fx.util.NotificationManager;
import jloda.fx.util.Print;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;

/**
 * setup all control bindings
 * Daniel Huson, 7.2019
 */
public class ControlBindings {

    public static void setup(MainWindow window) {
        final ObjectProperty<javafx.scene.Node> printableNode = new SimpleObjectProperty<>();

        final MainWindowController controller = window.getController();

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getRecentFilesMenu());

        window.getStage().setOnCloseRequest((e) -> {
            controller.getCloseMenuItem().getOnAction().handle(null);
            e.consume();
        });

        controller.getNewMenuItem().setOnAction((e) -> {
            NewWindow.apply();
        });

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        controller.getSaveMenItem().setOnAction(e -> Save.showSaveDialog(window));

        controller.getCloseMenuItem().setOnAction(e -> {
            window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(false);
            if (SaveChangesDialog.apply(window)) {
                ProgramProperties.put("WindowGeometry", (new WindowGeometry(window.getStage())).toString());
                MainWindowManager.getInstance().closeMainWindow(window);
            }
        });

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(window.getStage()));

        controller.getPrintMenuItem().setOnAction((e) -> {
            javafx.scene.Node node = printableNode.get();

            Print.print(window.getStage(), node);

        });
        controller.getPrintMenuItem().disableProperty().bind(printableNode.isNull());

        // cut, copy, paste and undo/redo all implemented by TextArea controls

        controller.getCutMenuItem().setOnAction((e) -> {
        });
        controller.getCutMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getFoodSetComboBox().focusedProperty())).not());


        controller.getCopyMenuItem().setOnAction((e) -> {
        });
        controller.getCopyMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getFoodSetComboBox().focusedProperty())).not());

        controller.getPasteMenuItem().setOnAction((e) -> {
        });
        controller.getPasteMenuItem().disableProperty().bind((controller.getInputTextArea().focusedProperty().or(controller.getFoodSetComboBox().focusedProperty())).not());


        controller.getUndoMenuItem().setOnAction((e) -> {
        });
        controller.getUndoMenuItem().disableProperty().bind(
                ((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().undoableProperty()))
                        .or(controller.getFoodSetComboBox().focusedProperty())).not());

        controller.getRedoMenuItem().setOnAction((e) -> {
            if (false) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().redo();
            }
        });
        controller.getRedoMenuItem().disableProperty().bind(
                ((controller.getInputTextArea().focusedProperty().and(controller.getInputTextArea().redoableProperty()))
                        .or(controller.getFoodSetComboBox().focusedProperty())).not());

        controller.getClearLogMenuItem().setOnAction(e -> controller.getLogTextArea().clear());
        controller.getClearLogMenuItem().disableProperty().bind(controller.getLogTextArea().textProperty().isEmpty());

        controller.getVerifyInputMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getReactionsTab().getTabPane().getSelectionModel().select(controller.getReactionsTab());
                controller.getReactionsTextArea().setText(ModelIO.toString(window.getInputModel(), true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
                final String message = String.format("Input is valid. Found %,d reactions and %,d food items", window.getInputModel().getReactions().size(), window.getInputModel().getFoods().size());
                NotificationManager.showInformation(message);
                window.getLogStream().println(message);

            }
        });

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
            if (ParseInput.apply(window)) {
                controller.getRafTab().getTabPane().getSelectionModel().select(controller.getRafTab());
                RunAlgorithm.apply(window, window.getInputModel(), new MaxRAFAlgorithm(), window.getMaxRAF(), controller.getRafTextArea());
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunCAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getCafTab().getTabPane().getSelectionModel().select(controller.getCafTab());
                RunAlgorithm.apply(window, window.getInputModel(), new MaxCAFAlgorithm(), window.getMaxCAF(), controller.getCafTextArea());
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunPseudoRAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getPseudoRafTab().getTabPane().getSelectionModel().select(controller.getPseudoRafTab());
                RunAlgorithm.apply(window, window.getInputModel(), new MaxPseudoRAFAlgorithm(), window.getMaxPseudoRAF(), controller.getPseudoRAFTextArea());
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunMenuItem().setOnAction((e) -> {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            window.getLogStream().println("\nRun +++++ " + simpleDateFormat.format(new Date()) + " +++++:");

            controller.getReactionsTextArea().clear();
            controller.getCafTextArea().clear();
            controller.getRafTextArea().clear();
            controller.getPseudoRAFTextArea().clear();

            if (ParseInput.apply(window)) {
                controller.getReactionsTextArea().setText("Expanded reactions:\n\n" + ModelIO.toString(window.getInputModel().getExpandedSystem(), true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
                RunAlgorithm.apply(window, window.getInputModel(), new MaxCAFAlgorithm(), window.getMaxCAF(), controller.getCafTextArea());
                RunAlgorithm.apply(window, window.getInputModel(), new MaxRAFAlgorithm(), window.getMaxRAF(), controller.getRafTextArea());
                RunAlgorithm.apply(window, window.getInputModel(), new MaxPseudoRAFAlgorithm(), window.getMaxPseudoRAF(), controller.getPseudoRAFTextArea());
            }
        });
        controller.getRunMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunButton().setOnAction(controller.getRunMenuItem().getOnAction());
        controller.getRunButton().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getReactionsTab().disableProperty().bind(controller.getReactionsTextArea().textProperty().isEmpty());
        controller.getVisualizationTab().disableProperty().bind(controller.getReactionsTab().disableProperty());
        controller.getRafTab().disableProperty().bind(controller.getRafTextArea().textProperty().isEmpty());
        controller.getCafTab().disableProperty().bind(controller.getCafTextArea().textProperty().isEmpty());
        controller.getPseudoRafTab().disableProperty().bind(controller.getPseudoRAFTextArea().textProperty().isEmpty());


        controller.getAboutMenuItem().setOnAction((e) -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(2)));

        controller.getCheckForUpdatesMenuItem().setOnAction((e) -> CheckForUpdate.apply());
        MainWindowManager.getInstance().changedProperty().addListener((c, o, n) -> {
            controller.getCheckForUpdatesMenuItem().disableProperty().set(MainWindowManager.getInstance().size() > 1
                    || (MainWindowManager.getInstance().size() == 1 && !MainWindowManager.getInstance().getMainWindow(0).isEmpty()));
        });


        window.getStage().widthProperty().addListener((c, o, n) -> {
            if (!Double.isNaN(o.doubleValue()) && n.doubleValue() > 0)
                controller.getMainSplitPane().setDividerPosition(0, controller.getMainSplitPane().getDividerPositions()[0] * o.doubleValue() / n.doubleValue());
        });
        if (window.getStage().getWidth() > 0)
            controller.getMainSplitPane().setDividerPosition(0, 200.0 / window.getStage().getWidth());

        setupFind(controller);

        controller.getLogTextArea().appendText(Basic.stopCollectingStdErr());

        for (TextArea textArea : Arrays.asList(controller.getInputTextArea(), controller.getLogTextArea(), controller.getCafTextArea(), controller.getRafTextArea(), controller.getPseudoRAFTextArea())) {
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
            final Pane centerPane = new StackPane();
            centerPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

            centerPane.setOnContextMenuRequested((e) -> {
                controller.getVisualizationTabContextMenu().show(centerPane, e.getScreenX(), e.getScreenY());
            });

            centerPane.getChildren().add(window.getReactionGraphView().getWorld());

            final ZoomableScrollPane scrollPane = new ZoomableScrollPane(centerPane) {
                @Override // override node scaling to use coordinate scaling
                public void updateScale() {
                    final double zoomX = getZoomFactorX();
                    final double zoomY = getZoomFactorY();
                    for (javafx.scene.Node node : window.getReactionGraphView().getWorld().getChildren()) {
                        if (!node.translateXProperty().isBound())
                            node.setTranslateX(node.getTranslateX() * zoomX);
                        if (!node.translateYProperty().isBound())
                            node.setTranslateY(node.getTranslateY() * zoomY);
                    }
                }
            };
            centerPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
            centerPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                    scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

            scrollPane.setLockAspectRatio(true);

            controller.getVisualizationBorderPane().setCenter(scrollPane);

            centerPane.focusedProperty().addListener((c, o, n) -> {
                if (n)
                    printableNode.set(scrollPane.getContent());
            });
            controller.getVisualizationTab().getTabPane().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> printableNode.set(scrollPane.getContent()));

            centerPane.setOnMousePressed((e) -> {
                if (e.getClickCount() == 2) {
                    window.getReactionGraphView().getNodeSelection().clearSelection();
                    window.getReactionGraphView().getEdgeSelection().clearSelection();
                }
            });

            controller.getAnimateCAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateRAFCheckMenuItem().setSelected(false);
                    controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
                    window.getReactionGraphView().getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.CAF);
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(true);
                } else {
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(false);
                }
            });
            controller.getAnimateCAFCheckMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty().or(window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()));

            controller.getAnimateCAFContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateCAFCheckMenuItem().selectedProperty());
            controller.getAnimateCAFContextMenuItem().disableProperty().bind(controller.getAnimateCAFCheckMenuItem().disableProperty());

            controller.getAnimateRAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateCAFCheckMenuItem().setSelected(false);
                    controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
                    window.getReactionGraphView().getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.RAF);
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(true);
                } else {
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(false);
                }
            });
            controller.getAnimateRAFCheckMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty().or(window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()));

            controller.getAnimateRAFContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateRAFCheckMenuItem().selectedProperty());
            controller.getAnimateRAFContextMenuItem().disableProperty().bind(controller.getAnimateRAFCheckMenuItem().disableProperty());

            controller.getAnimateMaxRAFCheckMenuItem().selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    controller.getAnimateCAFCheckMenuItem().setSelected(false);
                    controller.getAnimateRAFCheckMenuItem().setSelected(false);
                    window.getReactionGraphView().getMoleculeFlowAnimation().setModel(MoleculeFlowAnimation.Model.PseudoRAF);
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(true);
                } else {
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(false);
                }
            });
            controller.getAnimateMaxRAFCheckMenuItem().disableProperty().bind(controller.getVisualizationTab().disableProperty().or(window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty()));

            controller.getAminatePseudoRAFContextMenuItem().selectedProperty().bindBidirectional(controller.getAnimateMaxRAFCheckMenuItem().selectedProperty());
            controller.getAminatePseudoRAFContextMenuItem().disableProperty().bind(controller.getAnimateMaxRAFCheckMenuItem().disableProperty());

            controller.getStopAnimationMenuItem().setOnAction(e -> {
                window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(false);
                controller.getSelectAllMenuItem().getOnAction().handle(null);
                controller.getSelectNoneMenuItem().getOnAction().handle(null);
                controller.getAnimateCAFCheckMenuItem().setSelected(false);
                controller.getAnimateRAFCheckMenuItem().setSelected(false);
                controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);
            });
            controller.getStopAnimationMenuItem().disableProperty().bind(window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty().not());

            controller.getStopAnimationContextMenuItem().setOnAction(controller.getStopAnimationMenuItem().getOnAction());
            controller.getStopAnimationContextMenuItem().disableProperty().bind(controller.getStopAnimationMenuItem().disableProperty());

            controller.getStopAnimationButton().setVisible(false);
            controller.getStopAnimationButton().setOnAction(controller.getStopAnimationMenuItem().getOnAction());
            window.getReactionGraphView().getMoleculeFlowAnimation().playingProperty().addListener((c, o, n) -> controller.getStopAnimationButton().setVisible(n));
            controller.getStopAnimationButton().textProperty().bind(window.getReactionGraphView().getMoleculeFlowAnimation().modelProperty().asString().concat(" animation"));

            controller.getVisualizationTab().disableProperty().addListener((c, o, n) -> {
                if (n)
                    window.getReactionGraphView().getMoleculeFlowAnimation().setPlaying(false);
                else {
                    controller.getAnimateCAFCheckMenuItem().setSelected(false);
                    controller.getAnimateRAFCheckMenuItem().setSelected(false);
                    controller.getAnimateMaxRAFCheckMenuItem().setSelected(false);

                    window.getReactionGraphView().clear();
                    window.getReactionGraphView().update();
                }
            });
        }
        SelectionBindings.setup(window, controller);

        //controller.getFoodSetComboBox().setStyle("-fx-font: 13px \"Courier New\";");


    }

    /**
     * setup the find dialog
     *
     * @param controller
     */
    private static void setupFind(MainWindowController controller) {
        final FindToolBar inputFindToolBar = new FindToolBar(new TextAreaSearcher("Input", controller.getInputTextArea()));
        controller.getReactionsInputVBox().getChildren().add(inputFindToolBar);

        final FindToolBar logFindToolBar = new FindToolBar(new TextAreaSearcher("Log", controller.getLogTextArea()));
        controller.getLogVBox().getChildren().add(logFindToolBar);


        final FindToolBar cafFindToolBar = new FindToolBar(new TextAreaSearcher("CAF", controller.getCafTextArea()));
        controller.getCafVBox().getChildren().add(cafFindToolBar);


        final FindToolBar rafFindToolBar = new FindToolBar(new TextAreaSearcher("RAF", controller.getRafTextArea()));
        controller.getRafVBox().getChildren().add(rafFindToolBar);


        final FindToolBar pseudoRafFindToolBar = new FindToolBar(new TextAreaSearcher("Pseudo-RAF", controller.getPseudoRAFTextArea()));
        controller.getPseudoRafVBox().getChildren().add(pseudoRafFindToolBar);

        controller.getFindMenuItem().setOnAction((e) -> {
            if (controller.getInputTextArea().isFocused())
                inputFindToolBar.setShowFindToolBar(true);
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.setShowFindToolBar(true);
            else if (controller.getCafTab().isSelected() || controller.getCafTextArea().isFocused())
                cafFindToolBar.setShowFindToolBar(true);
            else if (controller.getRafTab().isSelected() || controller.getRafTextArea().isFocused())
                rafFindToolBar.setShowFindToolBar(true);
            else if (controller.getPseudoRafTab().isSelected() || controller.getPseudoRAFTextArea().isFocused())
                pseudoRafFindToolBar.setShowFindToolBar(true);
        });

        controller.getFindAgainMenuItem().setOnAction((e) -> {
            if (controller.getInputTextArea().isFocused())
                inputFindToolBar.findAgain();
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.findAgain();
            else if (controller.getCafTab().isSelected() || controller.getCafTextArea().isFocused())
                cafFindToolBar.findAgain();
            else if (controller.getRafTab().isSelected() || controller.getRafTextArea().isFocused())
                rafFindToolBar.findAgain();
            else if (controller.getPseudoRafTab().isSelected() || controller.getPseudoRAFTextArea().isFocused())
                pseudoRafFindToolBar.findAgain();
        });

        Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
    }

}
