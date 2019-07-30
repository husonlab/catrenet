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
    private MenuItem selectFoodMenuItem;

    @FXML
    private MenuItem selectMoleculesMenuItem;

    @FXML
    private MenuItem selectReactionsMenuItem;

    @FXML
    private CheckMenuItem wrapTextMenuItem;

    @FXML
    private MenuItem formatMenuItem;

    @FXML
    private Menu algorithmMenu;

    @FXML
    private MenuItem parseInputMenuItem;

    @FXML
    private MenuItem runRAFMenuItem;

    @FXML
    private MenuItem runCAFMenuItem;

    @FXML
    private MenuItem runPseudoRAFMenuItem;

    @FXML
    private MenuItem runMenuItem;

    @FXML
    private CheckMenuItem simulateCAFCheckMenuItem;

    @FXML
    private CheckMenuItem simulateRAFCheckMenuItem;

    @FXML
    private CheckMenuItem simulateMaxRAFCheckMenuItem;

    @FXML
    private MenuItem stopSimulationMenuItem;

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
    private FlowPane statusFlowPane;

    @FXML
    private Label memoryUsageLabel;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private ComboBox<String> foodSetComboBox;

    @FXML
    private VBox reactionsInputVBox;

    @FXML
    private TextArea inputTextArea;

    @FXML
    private StackPane outputPane;

    @FXML
    private TabPane outputTabPane;

    @FXML
    private Tab visualizationTab;

    @FXML
    private BorderPane visualizationBorderPane;

    @FXML
    private VBox visualizationVBox;

    @FXML
    private MenuItem selectCAFContextMenuItem;

    @FXML
    private MenuItem selectRAFContextMenuItem;

    @FXML
    private MenuItem selectPseudoRAFContextMenuItem;

    @FXML
    private MenuItem selectNoneContextMenuItem;

    @FXML
    private CheckMenuItem simulateCAFContextMenuItem;

    @FXML
    private CheckMenuItem simulateRAFContextMenuItem;

    @FXML
    private CheckMenuItem simulatePseudoRAFContextMenuItem;

    @FXML
    private MenuItem stopSimulationContextMenuItem;

    @FXML
    private Tab parsedInputTab;

    @FXML
    private TextArea reactionsTextArea;

    @FXML
    private VBox reactionsVBox;

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
    private Tab logTab;

    @FXML
    private TextArea logTextArea;

    @FXML
    private MenuItem clearLogMenuItem;

    @FXML
    private VBox logVBox;

    @FXML
    void initialize() {
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert newMenuItem != null : "fx:id=\"newMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert recentFilesMenu != null : "fx:id=\"recentFilesMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert saveMenItem != null : "fx:id=\"saveMenItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pageSetupMenuItem != null : "fx:id=\"pageSetupMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert printMenuItem != null : "fx:id=\"printMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert quitMenuItem != null : "fx:id=\"quitMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert editMenu != null : "fx:id=\"editMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cutMenuItem != null : "fx:id=\"cutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert copyMenuItem != null : "fx:id=\"copyMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pasteMenuItem != null : "fx:id=\"pasteMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert clearMenuItem != null : "fx:id=\"clearMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert findMenuItem != null : "fx:id=\"findMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert findAgainMenuItem != null : "fx:id=\"findAgainMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectNoneMenuItem != null : "fx:id=\"selectNoneMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectInvertedMenuItem != null : "fx:id=\"selectInvertedMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectNodesMenuItem != null : "fx:id=\"selectNodesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectEdgesMenuItem != null : "fx:id=\"selectEdgesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMaxCAFMenuItem != null : "fx:id=\"selectMaxCAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMaxRAFMenuItem != null : "fx:id=\"selectMaxRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMaxPseudoRAFMenuItem != null : "fx:id=\"selectMaxPseudoRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectFoodMenuItem != null : "fx:id=\"selectFoodMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMoleculesMenuItem != null : "fx:id=\"selectMoleculesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectReactionsMenuItem != null : "fx:id=\"selectReactionsMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert wrapTextMenuItem != null : "fx:id=\"wrapTextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert formatMenuItem != null : "fx:id=\"formatMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert algorithmMenu != null : "fx:id=\"algorithmMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert parseInputMenuItem != null : "fx:id=\"verifyInputMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runRAFMenuItem != null : "fx:id=\"runRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runCAFMenuItem != null : "fx:id=\"runCAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runPseudoRAFMenuItem != null : "fx:id=\"runPseudoRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runMenuItem != null : "fx:id=\"runMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert simulateCAFCheckMenuItem != null : "fx:id=\"simualateCAFCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert simulateRAFCheckMenuItem != null : "fx:id=\"simualateRAFCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert simulateMaxRAFCheckMenuItem != null : "fx:id=\"simualatePseudoRAFCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert stopSimulationMenuItem != null : "fx:id=\"stopSImulationMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert windowMenu != null : "fx:id=\"windowMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert checkForUpdatesMenuItem != null : "fx:id=\"checkForUpdatesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runButton != null : "fx:id=\"runButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusFlowPane != null : "fx:id=\"statusFlowPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert memoryUsageLabel != null : "fx:id=\"memoryUsageLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert mainSplitPane != null : "fx:id=\"mainSplitPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert foodSetComboBox != null : "fx:id=\"foodSetComboBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert reactionsInputVBox != null : "fx:id=\"reactionsInputVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert inputTextArea != null : "fx:id=\"inputTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert outputPane != null : "fx:id=\"outputPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert outputTabPane != null : "fx:id=\"outputTabPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationTab != null : "fx:id=\"visualizationTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationBorderPane != null : "fx:id=\"visualizationBorderPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationVBox != null : "fx:id=\"visualizationVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectCAFContextMenuItem != null : "fx:id=\"selectCAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectRAFContextMenuItem != null : "fx:id=\"selectRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectPseudoRAFContextMenuItem != null : "fx:id=\"selectPseudoRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectNoneContextMenuItem != null : "fx:id=\"selectNoneContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert simulateCAFContextMenuItem != null : "fx:id=\"simulateCAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert simulateRAFContextMenuItem != null : "fx:id=\"simulateRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert simulatePseudoRAFContextMenuItem != null : "fx:id=\"simulatePseudoRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert stopSimulationContextMenuItem != null : "fx:id=\"stopSimulationContextMenuItem11\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert parsedInputTab != null : "fx:id=\"reactionsTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert reactionsTextArea != null : "fx:id=\"reactionsTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert reactionsVBox != null : "fx:id=\"reactionsVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cafTab != null : "fx:id=\"cafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cafTextArea != null : "fx:id=\"cafTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cafVBox != null : "fx:id=\"cafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rafTab != null : "fx:id=\"rafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rafTextArea != null : "fx:id=\"rafTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rafVBox != null : "fx:id=\"rafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pseudoRafTab != null : "fx:id=\"pseudoRafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pseudoRAFTextArea != null : "fx:id=\"pseudoRAFTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pseudoRafVBox != null : "fx:id=\"pseudoRafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert logTab != null : "fx:id=\"logTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert logTextArea != null : "fx:id=\"logTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert clearLogMenuItem != null : "fx:id=\"clearLogMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert logVBox != null : "fx:id=\"logVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";


        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            fileMenu.getItems().remove(getQuitMenuItem());
            windowMenu.getItems().remove(getAboutMenuItem());
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

        final InvalidationListener invalidationListener = observable -> {
            windowMenu.getItems().clear();
            if (true || !ProgramProperties.isMacOS()) {
                windowMenu.getItems().add(getAboutMenuItem());
                windowMenu.getItems().add(new SeparatorMenuItem());
                windowMenu.getItems().add(getCheckForUpdatesMenuItem());
                windowMenu.getItems().add(new SeparatorMenuItem());
            }
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
    }

    private SplittableTabPane outputSplittableTabPane;

    private ContextMenu visualizationTabContextMenu;

    public SplittableTabPane getOutputSplittableTabPane() {
        return outputSplittableTabPane;
    }

    public ContextMenu getVisualizationTabContextMenu() {
        return visualizationTabContextMenu;
    }

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

    public MenuItem getSelectFoodMenuItem() {
        return selectFoodMenuItem;
    }

    public MenuItem getSelectMoleculesMenuItem() {
        return selectMoleculesMenuItem;
    }

    public MenuItem getSelectReactionsMenuItem() {
        return selectReactionsMenuItem;
    }

    public CheckMenuItem getWrapTextMenuItem() {
        return wrapTextMenuItem;
    }

    public MenuItem getFormatMenuItem() {
        return formatMenuItem;
    }

    public Menu getAlgorithmMenu() {
        return algorithmMenu;
    }

    public MenuItem getVerifyInputMenuItem() {
        return parseInputMenuItem;
    }

    public MenuItem getRunRAFMenuItem() {
        return runRAFMenuItem;
    }

    public MenuItem getRunCAFMenuItem() {
        return runCAFMenuItem;
    }

    public MenuItem getRunPseudoRAFMenuItem() {
        return runPseudoRAFMenuItem;
    }

    public MenuItem getRunMenuItem() {
        return runMenuItem;
    }

    public CheckMenuItem getSimualateCAFCheckMenuItem() {
        return simulateCAFCheckMenuItem;
    }

    public CheckMenuItem getSimualateRAFCheckMenuItem() {
        return simulateRAFCheckMenuItem;
    }

    public CheckMenuItem getSimualatePseudoRAFCheckMenuItem() {
        return simulateMaxRAFCheckMenuItem;
    }

    public MenuItem getStopSImulationMenuItem() {
        return stopSimulationMenuItem;
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

    public Button getRunButton() {
        return runButton;
    }

    public ToolBar getMainToolBar() {
        return mainToolBar;
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

    public ComboBox<String> getFoodSetComboBox() {
        return foodSetComboBox;
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

    public Tab getVisualizationTab() {
        return visualizationTab;
    }

    public BorderPane getVisualizationBorderPane() {
        return visualizationBorderPane;
    }

    public VBox getVisualizationVBox() {
        return visualizationVBox;
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

    public MenuItem getSelectNoneContextMenuItem() {
        return selectNoneContextMenuItem;
    }

    public CheckMenuItem getSimulateCAFContextMenuItem() {
        return simulateCAFContextMenuItem;
    }

    public CheckMenuItem getSimulateRAFContextMenuItem() {
        return simulateRAFContextMenuItem;
    }

    public CheckMenuItem getSimulatePseudoRAFContextMenuItem() {
        return simulatePseudoRAFContextMenuItem;
    }

    public MenuItem getStopSimulationContextMenuItem11() {
        return stopSimulationContextMenuItem;
    }

    public Tab getReactionsTab() {
        return parsedInputTab;
    }

    public TextArea getReactionsTextArea() {
        return reactionsTextArea;
    }

    public VBox getReactionsVBox() {
        return reactionsVBox;
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
}
