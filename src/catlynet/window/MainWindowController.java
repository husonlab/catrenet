package catlynet.window;

import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jloda.fx.control.SplittableTabPane;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.util.ProgramProperties;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private Menu exportMenu;

    @FXML
    private MenuItem exportSelectedNodesMenuItem;

    @FXML
    private Menu recentFilesMenu;

    @FXML
    private MenuItem saveMenItem;

    @FXML
    private MenuItem pageSetupMenuItem;

    @FXML
    private MenuItem printMenuItem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem quitMenuItem;

    @FXML
    private Menu editMenu;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem cutMenuItem;

    @FXML
    private MenuItem copyMenuItem;

    @FXML
    private MenuItem pasteMenuItem;

    @FXML
    private MenuItem clearMenuItem;

    @FXML
    private MenuItem findMenuItem;

    @FXML
    private MenuItem findAgainMenuItem;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private MenuItem selectInvertedMenuItem;

    @FXML
    private MenuItem selectNodesMenuItem;

    @FXML
    private MenuItem selectEdgesMenuItem;

    @FXML
    private MenuItem selectMaxCAFMenuItem;

    @FXML
    private MenuItem selectMaxRAFMenuItem;

    @FXML
    private MenuItem selectMaxPseudoRAFMenuItem;

    @FXML
    private MenuItem selectMuCAFMenuItem;

    @FXML
    private MenuItem selectURAFMenuItem;

    @FXML
    private MenuItem selectMinIrrRAFMenuItem;

    @FXML
    private MenuItem selectFoodMenuItem;

    @FXML
    private MenuItem selectMoleculesMenuItem;

    @FXML
    private MenuItem selectReactionsMenuItem;

    @FXML
    private MenuItem selectConnectedComponentMenuItem;

    @FXML
    private Menu algorithmMenu;

    @FXML
    private MenuItem expandInputMenuItem;

    @FXML
    private MenuItem computeVisualizationMenuItem;

    @FXML
    private MenuItem runCAFMenuItem;

    @FXML
    private MenuItem runRAFMenuItem;

    @FXML
    private MenuItem runPseudoRAFMenuItem;

    @FXML
    private MenuItem runMenuItem;

    @FXML
    private MenuItem runMuCAFMenuItem;

    @FXML
    private MenuItem runURAFMenuItem;

    @FXML
    private MenuItem runMinIrrRAFMenuItem;

    @FXML
    private MenuItem runMuCAFMultipleTimesMenuItem;


    @FXML
    private MenuItem spontaneousInRafMenuItem;

    @FXML
    private MenuItem greedyGrowMenuItem;

    @FXML
    private CheckMenuItem computeImportanceCheckMenuItem;

    @FXML
    private MenuItem graphEmbedderIterationsMenuItem;

    @FXML
    private CheckMenuItem animateCAFCheckMenuItem;

    @FXML
    private CheckMenuItem animateRAFCheckMenuItem;

    @FXML
    private CheckMenuItem animateMaxRAFCheckMenuItem;

    @FXML
    private CheckMenuItem animateInhibitionsMenuItem;

    @FXML
    private MenuItem stopAnimationMenuItem;

    @FXML
    private MenuItem increaseFontSizeMenuItem;

    @FXML
    private MenuItem decreaseFontSizeMenuItem;


    @FXML
    private MenuItem zoomInMenuItem;

    @FXML
    private MenuItem zoomOutMenuItem;

    @FXML
    private MenuItem zoomToFitMenuItem;

    @FXML
    private CheckMenuItem wrapTextMenuItem;

    @FXML
    private MenuItem formatMenuItem;

    @FXML
    private MenuItem showNodeLabels;

    @FXML
    private RadioMenuItem fullGraphRadioMenuItem;

    @FXML
    private RadioMenuItem dependencyGraphRadioMenuItem;

    @FXML
    private RadioMenuItem reactantDependencyRadioMenuItem;

    @FXML
    private CheckMenuItem suppressCatalystEdgesMenuItem;

    @FXML
    private CheckMenuItem useMultiCopyFoodNodesMenuItem;


    @FXML
    private MenuItem fullScreenMenuItem;

    @FXML
    private Menu windowMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private MenuItem checkForUpdatesMenuItem;

    @FXML
    private ToolBar mainToolBar;

    @FXML
    private Button runButton;

    @FXML
    private Button stopAnimationButton;

    @FXML
    private FlowPane statusFlowPane;

    @FXML
    private Label memoryUsageLabel;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private VBox foodInputVBox;
    @FXML
    private TextArea inputFoodTextArea;

    @FXML
    private VBox reactionsInputVBox;

    @FXML
    private TextArea inputTextArea;

    @FXML
    private StackPane outputPane;

    @FXML
    private TabPane outputTabPane;

    @FXML
    private Tab expandedReactionsTab;

    @FXML
    private TextArea expandedReactionsTextArea;

    @FXML
    private VBox expandedReactionsVBox;

    @FXML
    private Tab visualizationTab;

    @FXML
    private Label graphTypeLabel;

    @FXML
    private BorderPane visualizationBorderPane;

    @FXML
    private VBox visualizationVBox;

    @FXML
    private ScrollPane visualizationScrollPane;

    @FXML
    private MenuItem selectCAFContextMenuItem;

    @FXML
    private MenuItem selectRAFContextMenuItem;

    @FXML
    private MenuItem selectPseudoRAFContextMenuItem;

    @FXML
    private MenuItem selectMinIrrRAFContextMenuItem;

    @FXML
    private ContextMenu visualizationContextMenu;

    @FXML
    private MenuItem selectMuCAFContextMenuItem;

    @FXML
    private MenuItem selectURAFContextMenuItem;

    @FXML
    private MenuItem selectNoneContextMenuItem;

    @FXML
    private MenuItem selectConnectedComponentContextMenuItem;

    @FXML
    private Tab cafTab;

    @FXML
    private TextArea cafTextArea;

    @FXML
    private VBox cafVBox;

    @FXML
    private Tab rafTab;

    @FXML
    private TextArea rafTextArea;

    @FXML
    private VBox rafVBox;

    @FXML
    private Tab pseudoRafTab;

    @FXML
    private TextArea pseudoRAFTextArea;

    @FXML
    private VBox pseudoRafVBox;

    @FXML
    private Tab muCafTab;

    @FXML
    private TextArea muCafTextArea;

    @FXML
    private VBox muCafVBox;

    @FXML
    private Tab uRAFTab;

    @FXML
    private VBox uRAFVBox;

    @FXML
    private TextArea uRAFTextArea;

    @FXML
    private Tab irrRAFTab;

    @FXML
    private TextArea irrRAFTextArea;


    @FXML
    private VBox irrRAFVBox;

    @FXML
    private Tab logTab;

    @FXML
    private TextArea logTextArea;

    @FXML
    private MenuItem clearLogMenuItem;

    @FXML
    private VBox logVBox;

    private ZoomableScrollPane zoomableScrollPane;

    @FXML
    void initialize() {

        increaseFontSizeMenuItem.setAccelerator(new KeyCharacterCombination("+", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_ANY));
        decreaseFontSizeMenuItem.setAccelerator(new KeyCharacterCombination("-", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_ANY));

        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            fileMenu.getItems().remove(getQuitMenuItem());
            // windowMenu.getItems().remove(getAboutMenuItem());
            //editMenu.getItems().remove(getPreferencesMenuItem());
        }

        visualizationTabContextMenu = visualizationTab.getContextMenu();
        visualizationTab.setContextMenu(null);

        // replace tabbed pane by splittable one

        final ArrayList<Tab> tabs = new ArrayList<>(outputTabPane.getTabs());
        outputTabPane.getTabs().clear();
        outputSplittableTabPane = new SplittableTabPane();
        outputSplittableTabPane.getTabs().addAll(tabs);
        outputPane.getChildren().setAll(outputSplittableTabPane);
        if (outputSplittableTabPane.getTabs().size() > 0)
            outputSplittableTabPane.getSelectionModel().select(0);

        final ArrayList<MenuItem> originalWindowMenuItems = new ArrayList<>(windowMenu.getItems());

        final InvalidationListener invalidationListener = observable -> {
            windowMenu.getItems().setAll(originalWindowMenuItems);
            int count = 0;
            for (IMainWindow mainWindow : MainWindowManager.getInstance().getMainWindows()) {
                if (mainWindow.getStage() != null) {
                    final String title = mainWindow.getStage().getTitle();
                    if (title != null) {
                        final MenuItem menuItem = new MenuItem(title.replaceAll("- " + ProgramProperties.getProgramName(), ""));
                        menuItem.setOnAction((e) -> mainWindow.getStage().toFront());
                        menuItem.setAccelerator(new KeyCharacterCombination("" + (++count), KeyCombination.SHORTCUT_DOWN));
                        windowMenu.getItems().add(menuItem);
                    }
                }
                if (MainWindowManager.getInstance().getAuxiliaryWindows(mainWindow) != null) {
                    for (Stage auxStage : MainWindowManager.getInstance().getAuxiliaryWindows(mainWindow)) {
                        final String title = auxStage.getTitle();
                        if (title != null) {
                            final MenuItem menuItem = new MenuItem(title.replaceAll("- " + ProgramProperties.getProgramName(), ""));
                            menuItem.setOnAction((e) -> auxStage.toFront());
                            windowMenu.getItems().add(menuItem);
                        }
                    }
                }
            }
        };
        MainWindowManager.getInstance().changedProperty().addListener(invalidationListener);
        invalidationListener.invalidated(null);

        zoomableScrollPane = new ZoomableScrollPane(null);
        visualizationBorderPane.setCenter(zoomableScrollPane);
    }

    private SplittableTabPane outputSplittableTabPane;

    private ContextMenu visualizationTabContextMenu;

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getFileMenu() {
        return fileMenu;
    }

    public MenuItem getNewMenuItem() {
        return newMenuItem;
    }

    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public MenuItem getImportMenuItem() {
        return importMenuItem;
    }

    public Menu getExportMenu() {
        return exportMenu;
    }

    public MenuItem getExportSelectedNodesMenuItem() {
        return exportSelectedNodesMenuItem;
    }

    public Menu getRecentFilesMenu() {
        return recentFilesMenu;
    }

    public MenuItem getSaveMenItem() {
        return saveMenItem;
    }

    public MenuItem getPageSetupMenuItem() {
        return pageSetupMenuItem;
    }

    public MenuItem getPrintMenuItem() {
        return printMenuItem;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public MenuItem getQuitMenuItem() {
        return quitMenuItem;
    }

    public Menu getEditMenu() {
        return editMenu;
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public MenuItem getCutMenuItem() {
        return cutMenuItem;
    }

    public MenuItem getCopyMenuItem() {
        return copyMenuItem;
    }

    public MenuItem getPasteMenuItem() {
        return pasteMenuItem;
    }

    public MenuItem getClearMenuItem() {
        return clearMenuItem;
    }

    public MenuItem getFindMenuItem() {
        return findMenuItem;
    }

    public MenuItem getFindAgainMenuItem() {
        return findAgainMenuItem;
    }

    public MenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public MenuItem getSelectNoneMenuItem() {
        return selectNoneMenuItem;
    }

    public MenuItem getSelectInvertedMenuItem() {
        return selectInvertedMenuItem;
    }

    public MenuItem getSelectNodesMenuItem() {
        return selectNodesMenuItem;
    }

    public MenuItem getSelectEdgesMenuItem() {
        return selectEdgesMenuItem;
    }

    public MenuItem getSelectMaxCAFMenuItem() {
        return selectMaxCAFMenuItem;
    }

    public MenuItem getSelectMaxRAFMenuItem() {
        return selectMaxRAFMenuItem;
    }

    public MenuItem getSelectMaxPseudoRAFMenuItem() {
        return selectMaxPseudoRAFMenuItem;
    }

    public MenuItem getSelectMuCAFMenuItem() {
        return selectMuCAFMenuItem;
    }

    public MenuItem getSelectURAFMenuItem() {
        return selectURAFMenuItem;
    }

    public MenuItem getSelectMinIrrRAFMenuItem() {
        return selectMinIrrRAFMenuItem;
    }

    public MenuItem getSelectFoodMenuItem() {
        return selectFoodMenuItem;
    }

    public MenuItem getSelectMoleculesMenuItem() {
        return selectMoleculesMenuItem;
    }

    public MenuItem getSelectReactionsMenuItem() {
        return selectReactionsMenuItem;
    }

    public MenuItem getSelectConnectedComponentMenuItem() {
        return selectConnectedComponentMenuItem;
    }

    public Menu getAlgorithmMenu() {
        return algorithmMenu;
    }

    public MenuItem getExpandInputMenuItem() {
        return expandInputMenuItem;
    }

    public MenuItem getComputeVisualizationMenuItem() {
        return computeVisualizationMenuItem;
    }

    public MenuItem getRunCAFMenuItem() {
        return runCAFMenuItem;
    }

    public MenuItem getRunRAFMenuItem() {
        return runRAFMenuItem;
    }

    public MenuItem getRunPseudoRAFMenuItem() {
        return runPseudoRAFMenuItem;
    }

    public MenuItem getRunMenuItem() {
        return runMenuItem;
    }

    public MenuItem getRunMuCAFMenuItem() {
        return runMuCAFMenuItem;
    }

    public MenuItem getRunURAFMenuItem() {
        return runURAFMenuItem;
    }

    public MenuItem getRunMinIrrRAFMenuItem() {
        return runMinIrrRAFMenuItem;
    }

    public MenuItem getRunMuCAFMultipleTimesMenuItem() {
        return runMuCAFMultipleTimesMenuItem;
    }

    public MenuItem getSpontaneousInRafMenuItem() {
        return spontaneousInRafMenuItem;
    }

    public MenuItem getGreedyGrowMenuItem() {
        return greedyGrowMenuItem;
    }

    public CheckMenuItem getComputeImportanceCheckMenuItem() {
        return computeImportanceCheckMenuItem;
    }

    public MenuItem getGraphEmbedderIterationsMenuItem() {
        return graphEmbedderIterationsMenuItem;
    }

    public CheckMenuItem getAnimateCAFCheckMenuItem() {
        return animateCAFCheckMenuItem;
    }

    public CheckMenuItem getAnimateRAFCheckMenuItem() {
        return animateRAFCheckMenuItem;
    }

    public CheckMenuItem getAnimateMaxRAFCheckMenuItem() {
        return animateMaxRAFCheckMenuItem;
    }

    public CheckMenuItem getAnimateInhibitionsMenuItem() {
        return animateInhibitionsMenuItem;
    }

    public MenuItem getStopAnimationMenuItem() {
        return stopAnimationMenuItem;
    }

    public MenuItem getIncreaseFontSizeMenuItem() {
        return increaseFontSizeMenuItem;
    }

    public MenuItem getDecreaseFontSizeMenuItem() {
        return decreaseFontSizeMenuItem;
    }

    public MenuItem getZoomInMenuItem() {
        return zoomInMenuItem;
    }

    public MenuItem getZoomOutMenuItem() {
        return zoomOutMenuItem;
    }

    public MenuItem getZoomToFitMenuItem() {
        return zoomToFitMenuItem;
    }

    public CheckMenuItem getWrapTextMenuItem() {
        return wrapTextMenuItem;
    }

    public MenuItem getFormatMenuItem() {
        return formatMenuItem;
    }

    public MenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public MenuItem getShowNodeLabels() {
        return showNodeLabels;
    }

    public RadioMenuItem getFullGraphRadioMenuItem() {
        return fullGraphRadioMenuItem;
    }

    public RadioMenuItem getDependencyGraphRadioMenuItem() {
        return dependencyGraphRadioMenuItem;
    }

    public RadioMenuItem getReactantDependencyGraphRadioMenuItem() {
        return reactantDependencyRadioMenuItem;
    }

    public CheckMenuItem getSuppressCatalystEdgesMenuItem() {
        return suppressCatalystEdgesMenuItem;
    }

    public CheckMenuItem getUseMultiCopyFoodNodesMenuItem() {
        return useMultiCopyFoodNodesMenuItem;
    }

    public Menu getWindowMenu() {
        return windowMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public MenuItem getCheckForUpdatesMenuItem() {
        return checkForUpdatesMenuItem;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
    }

    public Button getRunButton() {
        return runButton;
    }

    public Button getStopAnimationButton() {
        return stopAnimationButton;
    }

    public FlowPane getStatusFlowPane() {
        return statusFlowPane;
    }

    public Label getMemoryUsageLabel() {
        return memoryUsageLabel;
    }

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    public VBox getFoodInputVBox() {
        return foodInputVBox;
    }

    public TextArea getInputFoodTextArea() {
        return inputFoodTextArea;
    }

    public VBox getReactionsInputVBox() {
        return reactionsInputVBox;
    }

    public TextArea getInputTextArea() {
        return inputTextArea;
    }

    public StackPane getOutputPane() {
        return outputPane;
    }

    public TabPane getOutputTabPane() {
        return outputTabPane;
    }

    public Tab getExpandedReactionsTab() {
        return expandedReactionsTab;
    }

    public TextArea getExpandedReactionsTextArea() {
        return expandedReactionsTextArea;
    }

    public VBox getExpandedReactionsVBox() {
        return expandedReactionsVBox;
    }

    public Tab getVisualizationTab() {
        return visualizationTab;
    }

    public Label getGraphTypeLabel() {
        return graphTypeLabel;
    }

    public BorderPane getVisualizationBorderPane() {
        return visualizationBorderPane;
    }

    public VBox getVisualizationVBox() {
        return visualizationVBox;
    }

    public ContextMenu getVisualizationContextMenu() {
        return visualizationContextMenu;
    }

    public MenuItem getSelectMuCAFContextMenuItem() {
        return selectMuCAFContextMenuItem;
    }

    public MenuItem getSelectURAFContextMenuItem() {
        return selectURAFContextMenuItem;
    }

    public MenuItem getSelectCAFContextMenuItem() {
        return selectCAFContextMenuItem;
    }

    public MenuItem getSelectRAFContextMenuItem() {
        return selectRAFContextMenuItem;
    }

    public MenuItem getSelectPseudoRAFContextMenuItem() {
        return selectPseudoRAFContextMenuItem;
    }

    public MenuItem getSelectMinIrrRAFContextMenuItem() {
        return selectMinIrrRAFContextMenuItem;
    }

    public MenuItem getSelectNoneContextMenuItem() {
        return selectNoneContextMenuItem;
    }

    public MenuItem getSelectConnectedComponentContextMenuItem() {
        return selectConnectedComponentContextMenuItem;
    }

    public Tab getCafTab() {
        return cafTab;
    }

    public TextArea getCafTextArea() {
        return cafTextArea;
    }

    public VBox getCafVBox() {
        return cafVBox;
    }

    public Tab getRafTab() {
        return rafTab;
    }

    public TextArea getRafTextArea() {
        return rafTextArea;
    }

    public VBox getRafVBox() {
        return rafVBox;
    }

    public Tab getPseudoRafTab() {
        return pseudoRafTab;
    }

    public TextArea getPseudoRAFTextArea() {
        return pseudoRAFTextArea;
    }

    public VBox getPseudoRafVBox() {
        return pseudoRafVBox;
    }

    public Tab getMuCafTab() {
        return muCafTab;
    }

    public TextArea getMuCafTextArea() {
        return muCafTextArea;
    }

    public VBox getMuCafVBox() {
        return muCafVBox;
    }

    public Tab getuRAFTab() {
        return uRAFTab;
    }

    public TextArea getuRAFTextArea() {
        return uRAFTextArea;
    }

    public VBox getuRAFVBox() {
        return uRAFVBox;
    }

    public Tab getIrrRAFTab() {
        return irrRAFTab;
    }

    public TextArea getIrrRAFTextArea() {
        return irrRAFTextArea;
    }

    public VBox getIrrRAFVBox() {
        return irrRAFVBox;
    }

    public Tab getLogTab() {
        return logTab;
    }

    public TextArea getLogTextArea() {
        return logTextArea;
    }

    public MenuItem getClearLogMenuItem() {
        return clearLogMenuItem;
    }

    public VBox getLogVBox() {
        return logVBox;
    }

    public SplittableTabPane getOutputSplittableTabPane() {
        return outputSplittableTabPane;
    }

    public ContextMenu getVisualizationTabContextMenu() {
        return visualizationTabContextMenu;
    }

    public ZoomableScrollPane getVisualizationScrollPane() {
        return zoomableScrollPane;
    }
}