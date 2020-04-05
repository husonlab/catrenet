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
    private MenuItem selectQuotientRAFMenuItem;

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
    private MenuItem runQuotientRAFMenuItem;

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
    private MenuItem selectQuotientRAFContextMenuItem;

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
    private Tab quotientRAFTab;

    @FXML
    private TextArea quotientRAFTextArea;


    @FXML
    private VBox quotientRAFVBox;

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
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert newMenuItem != null : "fx:id=\"newMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert recentFilesMenu != null : "fx:id=\"recentFilesMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert importMenuItem != null : "fx:id=\"importMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportMenu != null : "fx:id=\"exportMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert exportSelectedNodesMenuItem != null : "fx:id=\"exportSelectedNodesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
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
        assert selectMinIrrRAFMenuItem != null : "fx:id=\"selectMinIrrRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectQuotientRAFMenuItem != null : "fx:id=\"selectQuotientRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMuCAFMenuItem != null : "fx:id=\"selectMuCAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectURAFMenuItem != null : "fx:id=\"selectURAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectFoodMenuItem != null : "fx:id=\"selectFoodMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMoleculesMenuItem != null : "fx:id=\"selectMoleculesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectReactionsMenuItem != null : "fx:id=\"selectReactionsMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectConnectedComponentMenuItem != null : "fx:id=\"selectConnectedComponentMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert increaseFontSizeMenuItem != null : "fx:id=\"increaseFontSizeMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert decreaseFontSizeMenuItem != null : "fx:id=\"decreaseFontSizeMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomInMenuItem != null : "fx:id=\"zoomInMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomOutMenuItem != null : "fx:id=\"zoomOutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert zoomToFitMenuItem != null : "fx:id=\"zoomToFitMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert formatMenuItem != null : "fx:id=\"formatMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert wrapTextMenuItem != null : "fx:id=\"wrapTextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showNodeLabels != null : "fx:id=\"showNodeLabels\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fullGraphRadioMenuItem != null : "fx:id=\"fullGraphRadioMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert dependencyGraphRadioMenuItem != null : "fx:id=\"dependencyGraphRadioMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert reactantDependencyRadioMenuItem != null : "fx:id=\"reactantDependencyRadioMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert suppressCatalystEdgesMenuItem != null : "fx:id=\"suppressCatalystEdgesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert useMultiCopyFoodNodesMenuItem != null : "fx:id=\"useMultiCopyFoodNodesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fullScreenMenuItem != null : "fx:id=\"fullScreenMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert algorithmMenu != null : "fx:id=\"algorithmMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert expandInputMenuItem != null : "fx:id=\"expandInputMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert computeVisualizationMenuItem != null : "fx:id=\"computeVisualizationMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runCAFMenuItem != null : "fx:id=\"runCAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runRAFMenuItem != null : "fx:id=\"runRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runPseudoRAFMenuItem != null : "fx:id=\"runPseudoRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runMinIrrRAFMenuItem != null : "fx:id=\"runMinIrrRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runQuotientRAFMenuItem != null : "fx:id=\"runQuotientRAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runMenuItem != null : "fx:id=\"runMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runMuCAFMenuItem != null : "fx:id=\"runMuCAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runURAFMenuItem != null : "fx:id=\"runURAFMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runMuCAFMultipleTimesMenuItem != null : "fx:id=\"runMuCAFMultipleTimesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert spontaneousInRafMenuItem != null : "fx:id=\"spontaneousInRafMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert greedyGrowMenuItem != null : "fx:id=\"greedyGrowMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert computeImportanceCheckMenuItem != null : "fx:id=\"computeImportanceCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert graphEmbedderIterationsMenuItem != null : "fx:id=\"graphEmbedderIterationsMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert animateCAFCheckMenuItem != null : "fx:id=\"animateCAFCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert animateRAFCheckMenuItem != null : "fx:id=\"animateRAFCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert animateMaxRAFCheckMenuItem != null : "fx:id=\"animateMaxRAFCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert animateInhibitionsMenuItem != null : "fx:id=\"animateInhibitionsMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert stopAnimationMenuItem != null : "fx:id=\"stopAnimationMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert windowMenu != null : "fx:id=\"windowMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert checkForUpdatesMenuItem != null : "fx:id=\"checkForUpdatesMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert mainToolBar != null : "fx:id=\"mainToolBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert runButton != null : "fx:id=\"runButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert stopAnimationButton != null : "fx:id=\"stopAnimationButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusFlowPane != null : "fx:id=\"statusFlowPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert memoryUsageLabel != null : "fx:id=\"memoryUsageLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert mainSplitPane != null : "fx:id=\"mainSplitPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert foodInputVBox != null : "fx:id=\"foodInputVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert inputFoodTextArea != null : "fx:id=\"inputFoodTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert reactionsInputVBox != null : "fx:id=\"reactionsInputVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert inputTextArea != null : "fx:id=\"inputTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert outputPane != null : "fx:id=\"outputPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert outputTabPane != null : "fx:id=\"outputTabPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert expandedReactionsTab != null : "fx:id=\"expandedReactionsTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert expandedReactionsTextArea != null : "fx:id=\"expandedReactionsTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert expandedReactionsVBox != null : "fx:id=\"expandedReactionsVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationTab != null : "fx:id=\"visualizationTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationBorderPane != null : "fx:id=\"visualizationBorderPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationVBox != null : "fx:id=\"visualizationVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationScrollPane != null : "fx:id=\"visualizationScrollPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert graphTypeLabel != null : "fx:id=\"graphTypeLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert visualizationContextMenu != null : "fx:id=\"visualizationContextMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectCAFContextMenuItem != null : "fx:id=\"selectCAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectRAFContextMenuItem != null : "fx:id=\"selectRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectPseudoRAFContextMenuItem != null : "fx:id=\"selectPseudoRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMinIrrRAFContextMenuItem != null : "fx:id=\"selectMinIrrRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectQuotientRAFContextMenuItem != null : "fx:id=\"selectionQuotientRAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectMuCAFContextMenuItem != null : "fx:id=\"selectMuCAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectURAFContextMenuItem != null : "fx:id=\"selectURAFContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectNoneContextMenuItem != null : "fx:id=\"selectNoneContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectConnectedComponentContextMenuItem != null : "fx:id=\"selectConnectedComponentContextMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cafTab != null : "fx:id=\"cafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cafTextArea != null : "fx:id=\"cafTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert cafVBox != null : "fx:id=\"cafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rafTab != null : "fx:id=\"rafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rafTextArea != null : "fx:id=\"rafTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rafVBox != null : "fx:id=\"rafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pseudoRafTab != null : "fx:id=\"pseudoRafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pseudoRAFTextArea != null : "fx:id=\"pseudoRAFTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert pseudoRafVBox != null : "fx:id=\"pseudoRafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert muCafTab != null : "fx:id=\"muCafTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert muCafTextArea != null : "fx:id=\"muCafTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert muCafVBox != null : "fx:id=\"muCafVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert uRAFTab != null : "fx:id=\"uRAFTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert uRAFTextArea != null : "fx:id=\"uRAFTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert uRAFVBox != null : "fx:id=\"uRAFVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert irrRAFTab != null : "fx:id=\"irrRAFTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert irrRAFTextArea != null : "fx:id=\"irrRAFTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert irrRAFVBox != null : "fx:id=\"irrRAFVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert quotientRAFTab != null : "fx:id=\"quotientRAFTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert quotientRAFTextArea != null : "fx:id=\"quotientRAFTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert quotientRAFVBox != null : "fx:id=\"quotientRAFVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert logTab != null : "fx:id=\"logTab\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert logTextArea != null : "fx:id=\"logTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert clearLogMenuItem != null : "fx:id=\"clearLogMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert logVBox != null : "fx:id=\"logVBox\" was not injected: check your FXML file 'MainWindow.fxml'.";


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

    public MenuItem getSelectQuotientRAFMenuItem() {
        return selectQuotientRAFMenuItem;
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

    public MenuItem getRunQuotientRAFMenuItem() {
        return runQuotientRAFMenuItem;
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

    public MenuItem getSelectQuotientRAFContextMenuItem() {
        return selectQuotientRAFContextMenuItem;
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

    public Tab getQuotientRAFTab() {
        return quotientRAFTab;
    }

    public TextArea getQuotientRAFTextArea() {
        return quotientRAFTextArea;
    }

    public VBox getQuotientRAFVBox() {
        return quotientRAFVBox;
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