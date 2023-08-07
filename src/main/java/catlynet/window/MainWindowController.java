/*
 * MainWindowController.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.icons.MaterialIcons;
import catlynet.util.MenuUtils;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import jloda.fx.control.SplittableTabPane;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.MainWindowManager;

import java.util.ArrayList;

public class MainWindowController {

	@FXML
	private ToggleButton wrapFoodTextToggle;

	@FXML
	private ToggleButton wrapReactionsTextToggle;

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
	private MenuItem exportListOfReactionsMenuItem;

	@FXML
	private Menu recentFilesMenu;

	@FXML
	private MenuItem saveMenuItem;

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
	private Menu selectReactionSystemMenu;

	@FXML
	private MenuItem selectFoodMenuItem;

	@FXML
	private MenuItem selectMoleculesMenuItem;

	@FXML
	private MenuItem selectReactionsMenuItem;

	@FXML
	private MenuItem selectConnectedComponentMenuItem;

	@FXML
	private MenuItem selectFromPreviousWindowMenuItem;


	@FXML
	private MenuButton exportNetworkMenuButton;

	@FXML
	private MenuItem copyNetworkMenuItem;

	@FXML
	private MenuItem exportImageNetworkMenuItem;


	@FXML
	private Menu algorithmMenu;


	@FXML
	private MenuItem stopAnimationMenuItem;

	@FXML
	private MenuItem increaseFontSizeMenuItem;

	@FXML
	private MenuItem decreaseFontSizeMenuItem;

	@FXML
	private MenuItem listCatalystsMenuItem;

	@FXML
	private MenuItem listInhibitorsMenuItem;

	@FXML
	private MenuItem listProductsMenuItem;

	@FXML
	private MenuItem listReactantsMenuItem;

	@FXML
	private MenuItem listReactionsMenuItem;

	@FXML
	private MenuItem listFoodMenuItem;

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
	private MenuItem showNodeAndEdgeFormatMenuItem;

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
	private MenuButton recentMenuButton;

	@FXML
	private MenuItem openNewMenuItem;

	@FXML
	private Button stopAnimationButton;

	@FXML
	private FlowPane statusFlowPane;

	@FXML
	private Label memoryUsageLabel;

	@FXML
	private SplitPane mainSplitPane;

	@FXML
	private SplitPane inputSplitPane;

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
	private Tab parsedReactionsTab;

	@FXML
	private TextArea parsedReactionsTextArea;

	@FXML
	private VBox parsedReactionsVBox;

	@FXML
	private Tab networkTab;

	@FXML
	private Label graphTypeLabel;

	@FXML
	private BorderPane networkBorderPane;

	@FXML
	private VBox networkVBox;

	@FXML
	private StackPane networkPane;

	@FXML
	private ContextMenu networkContextMenu;

	@FXML
	private MenuItem selectNoneContextMenuItem;

	@FXML
	private MenuItem selectConnectedComponentContextMenuItem;

	@FXML
	private Tab logTab;

	@FXML
	private TextArea logTextArea;

	@FXML
	private MenuItem clearLogMenuItem;

	@FXML
	private VBox logVBox;

	@FXML
	private Label inputFoodSizeLabel;

	@FXML
	private Label inputReactionsSizeLabel;

	@FXML
	private CheckMenuItem useDarkThemeCheckMenuItem;

	@FXML
	private MenuItem computeNetworkMenuItem;

	@FXML
	private RadioMenuItem fullGraphRadioMenuItem;
	@FXML
	private RadioMenuItem associationGraphRadioMenuItem;
	@FXML
	private RadioMenuItem reactantAssociationRadioMenuItem;
	@FXML
	private CheckMenuItem suppressFormalFoodMenuItem;
	@FXML
	private CheckMenuItem suppressCatalystEdgesMenuItem;
	@FXML
	private CheckMenuItem useMultiCopyFoodNodesMenuItem;
	@FXML
	private RadioMenuItem reactionDependencyGraphRadioMenuItem;
	@FXML
	private RadioMenuItem moleculeDependencyGraphRadioMenuItem;
	@FXML
	private MenuItem showNodeLabels;

	@FXML
	private MenuButton computeNetworkMenuButton;

	@FXML
	private Button zoomInNetworkButton;

	@FXML
	private Button zoomOutNetworkButton;

	@FXML
	private ToggleButton findNetworkToggleButton;

	@FXML
	private MenuButton runMenuButton;

	@FXML
	private MenuItem runCAFMenuItem;

	@FXML
	private MenuItem runRAFMenuItem;

	@FXML
	private MenuItem runStrictlyAutocatalyticRAFMenuItem;

	@FXML
	private MenuItem runMinRAFGeneratingElementMenuItem;

	@FXML
	private MenuItem runPseudoRAFMenuItem;

	@FXML
	private MenuItem runMuCAFMenuItem;

	@FXML
	private MenuItem runURAFMenuItem;

	@FXML
	private MenuItem runMinIRAFMenuItem;

	@FXML
	private MenuItem runTrivialCAFsAlgorithmMenuItem;

	@FXML
	private MenuItem runTrivialRAFsAlgorithmMenuItem;

	@FXML
	private MenuItem runQuotientRAFMenuItem;

	@FXML
	private MenuItem runCoreRAFMenuItem;

	@FXML
	private MenuItem removeTrivialRAFsAlgorithmMenuItem;

	@FXML
	private MenuItem runMuCAFMultipleTimesMenuItem;

	@FXML
	private MenuItem spontaneousInRafMenuItem;

	@FXML
	private MenuItem greedyGrowMenuItem;

	@FXML
	private MenuItem reactionDependenciesMenuItem;

	@FXML
	private MenuItem moleculeDependenciesMenuItem;

	@FXML
	private CheckMenuItem computeImportanceCheckMenuItem;

	@FXML
	private MenuButton animateNetworkMenuButton;

	@FXML
	private CheckMenuItem animateCAFCheckMenuItem;

	@FXML
	private CheckMenuItem animateRAFCheckMenuItem;

	@FXML
	private CheckMenuItem animateMaxRAFCheckMenuItem;

	@FXML
	private CheckMenuItem animateInhibitionsMenuItem;

	@FXML
	private CheckMenuItem moveLabelsMenuItem;

	@FXML
	private CheckMenuItem useColorsMenuItem;

	@FXML
	private MenuItem graphEmbedderIterationsMenuItem;

	@FXML
	private ToggleButton findParsedReactionsToggleButton;

	@FXML
	private ToggleButton wrapParsedReactionsToggleButton;

	@FXML
	private MenuButton exportParsedMenuButton;

	@FXML
	private MenuItem copyParsedMenuItem;

	@FXML
	private ToggleButton findLogToggleButton;

	@FXML
	private ToggleButton wrapLogToggleButton;

	@FXML
	private MenuButton exportLogMenuButton;

	@FXML
	private MenuItem copyLogMenuItem;

	@FXML
	private MenuButton selectNetworkMenuButton;

	@FXML
	private Menu networkMenu;

	@FXML
	private Menu animateMenu;


	@FXML
	private Menu selectMenu;


	@FXML
	private Menu listMenu;

	@FXML
	private MenuButton listLogButton;


	private ZoomableScrollPane zoomableScrollPane;

	@FXML
	void initialize() {
		{
			MaterialIcons.setIcon(recentMenuButton, "launch");
			MaterialIcons.setIcon(runMenuButton, "play_circle_filled");

			MaterialIcons.setIcon(wrapFoodTextToggle, "wrap_text");
			MaterialIcons.setIcon(wrapReactionsTextToggle, "wrap_text");
			MaterialIcons.setIcon(findNetworkToggleButton, "search");

			MaterialIcons.setIcon(computeNetworkMenuButton, "tune");
			MaterialIcons.setIcon(animateNetworkMenuButton, "play_circle");
			MaterialIcons.setIcon(stopAnimationButton, "close", null, false);
			MaterialIcons.setIcon(zoomInNetworkButton, "zoom_in");
			MaterialIcons.setIcon(zoomOutNetworkButton, "zoom_out");
			MaterialIcons.setIcon(exportNetworkMenuButton, "ios_share");

			MaterialIcons.setIcon(findParsedReactionsToggleButton, "search");
			MaterialIcons.setIcon(exportParsedMenuButton, "ios_share");
			MaterialIcons.setIcon(wrapParsedReactionsToggleButton, "wrap_text");

			MaterialIcons.setIcon(findLogToggleButton, "search");
			MaterialIcons.setIcon(exportLogMenuButton, "ios_share");
			MaterialIcons.setIcon(wrapLogToggleButton, "wrap_text");

			MaterialIcons.setIcon(selectNetworkMenuButton, "checklist_rtl");

			MaterialIcons.setIcon(listLogButton, "list");

		}

		{
			wrapParsedReactionsToggleButton.selectedProperty().bindBidirectional(parsedReactionsTextArea.wrapTextProperty());
			copyParsedMenuItem.setOnAction(e -> {
				var content = new ClipboardContent();
				content.putString(parsedReactionsTextArea.getText());
				Clipboard.getSystemClipboard().setContent(content);
			});

			wrapLogToggleButton.selectedProperty().bindBidirectional(logTextArea.wrapTextProperty());
			copyLogMenuItem.setOnAction(e -> {
				var content = new ClipboardContent();
				content.putString(logTextArea.getText());
				Clipboard.getSystemClipboard().setContent(content);
			});

			copyNetworkMenuItem.setOnAction(e -> copyMenuItem.getOnAction().handle(e));
		}

		wrapFoodTextToggle.selectedProperty().bindBidirectional(inputFoodTextArea.wrapTextProperty());
		wrapFoodTextToggle.setSelected(true);
		wrapReactionsTextToggle.selectedProperty().bindBidirectional(inputTextArea.wrapTextProperty());

		increaseFontSizeMenuItem.setAccelerator(new KeyCharacterCombination("+", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_ANY));
		decreaseFontSizeMenuItem.setAccelerator(new KeyCharacterCombination("/", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_ANY));

		// if we are running on MacOS, put the specific menu items in the right places
		if (ProgramProperties.isMacOS()) {
			getMenuBar().setUseSystemMenuBar(true);
			fileMenu.getItems().remove(getQuitMenuItem());
			// windowMenu.getItems().remove(getAboutMenuItem());
			//editMenu.getItems().remove(getPreferencesMenuItem());
		}

		networkTabContextMenu = networkTab.getContextMenu();
		networkTab.setContextMenu(null);

		// replace tabbed pane by splittable one

		final var tabs = new ArrayList<>(outputTabPane.getTabs());
		outputTabPane.getTabs().clear();

		outputSplittableTabPane = new SplittableTabPane();
		outputSplittableTabPane.getTabs().addAll(tabs);
		outputPane.getChildren().setAll(outputSplittableTabPane);
		if (outputSplittableTabPane.getTabs().size() > 0)
			outputSplittableTabPane.getSelectionModel().select(0);

		final var originalWindowMenuItems = new ArrayList<>(windowMenu.getItems());

		final InvalidationListener invalidationListener = observable -> {
			windowMenu.getItems().setAll(originalWindowMenuItems);
			int count = 0;
			for (var mainWindow : MainWindowManager.getInstance().getMainWindows()) {
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
					for (var auxStage : MainWindowManager.getInstance().getAuxiliaryWindows(mainWindow)) {
						final var title = auxStage.getTitle();
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

		inputSplitPane.heightProperty().addListener((v, o, n) -> {
			if (o.doubleValue() > 0.0 && n.doubleValue() > 0.0) {
				var newPos = inputSplitPane.getDividers().get(0).getPosition() / n.doubleValue() * o.doubleValue();
				inputSplitPane.getDividers().get(0).setPosition(newPos);
			}
		});
		inputSplitPane.getDividers().get(0).setPosition(0.3);

		mainSplitPane.widthProperty().addListener((c, o, n) -> {
			if (o.doubleValue() > 0 && n.doubleValue() > 0) {
				var newPos = mainSplitPane.getDividers().get(0).getPosition() / n.doubleValue() * o.doubleValue();
				mainSplitPane.setDividerPosition(0, newPos);
			}
		});
		Platform.runLater(() -> mainSplitPane.setDividerPosition(0, 200.0 / mainSplitPane.getWidth()));


		{
			var stackPane = new StackPane();
			zoomableScrollPane = new ZoomableScrollPane(stackPane);
			networkPane.getChildren().add(zoomableScrollPane);
			networkPane = stackPane;
			if (false) networkPane.setPadding(new javafx.geometry.Insets(100));
			networkPane.getStyleClass().add("viewer-background");

			openNewMenuItem.setOnAction(e -> newMenuItem.getOnAction().handle(e));
			recentFilesMenu.getItems().addListener((InvalidationListener) e -> {
				recentMenuButton.getItems().setAll(openNewMenuItem, new SeparatorMenuItem());
				recentMenuButton.getItems().addAll(MenuUtils.copy(recentFilesMenu.getItems()));
			});
		}

		{
			computeNetworkMenuButton.getItems().addAll(MenuUtils.copy(networkMenu.getItems()));
			runMenuButton.getItems().addAll(MenuUtils.copy(algorithmMenu.getItems()));
			animateNetworkMenuButton.getItems().addAll(MenuUtils.copy(animateMenu.getItems()));
			selectNetworkMenuButton.getItems().addAll(MenuUtils.copy(selectMenu.getItems()));

			zoomInNetworkButton.setOnAction(e -> zoomInMenuItem.getOnAction().handle(e));
			zoomInNetworkButton.disableProperty().bind(zoomInMenuItem.disableProperty());
			zoomOutNetworkButton.setOnAction(e -> zoomOutMenuItem.getOnAction().handle(e));
			zoomOutNetworkButton.disableProperty().bind(zoomOutMenuItem.disableProperty());

			selectReactionSystemMenu.disableProperty().bind(Bindings.isEmpty(selectReactionSystemMenu.getItems()));

			listLogButton.getItems().addAll(MenuUtils.copy(listMenu.getItems()));
		}
	}

	private SplittableTabPane outputSplittableTabPane;

	private ContextMenu networkTabContextMenu;

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

	public MenuItem getExportListOfReactionsMenuItem() {
		return exportListOfReactionsMenuItem;
	}

	public Menu getRecentFilesMenu() {
		return recentFilesMenu;
	}

	public MenuItem getSaveMenuItem() {
		return saveMenuItem;
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

	public Menu getSelectReactionSystemMenu() {
		return selectReactionSystemMenu;
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

	public MenuItem getSelectFromPreviousWindowMenuItem() {
		return selectFromPreviousWindowMenuItem;
	}

	public Menu getAlgorithmMenu() {
		return algorithmMenu;
	}

	public MenuItem getComputeNetworkMenuItem() {
		return computeNetworkMenuItem;
	}

	public MenuItem getRunCAFMenuItem() {
		return runCAFMenuItem;
	}

	public MenuItem getRunRAFMenuItem() {
		return runRAFMenuItem;
	}

	public MenuItem getRunStrictlyAutocatalyticRAFMenuItem() {
		return runStrictlyAutocatalyticRAFMenuItem;
	}

	public MenuItem getRunMinRAFGeneratingElementMenuItem() {
		return runMinRAFGeneratingElementMenuItem;
	}

	public MenuItem getRunPseudoRAFMenuItem() {
		return runPseudoRAFMenuItem;
	}

	public MenuItem getRunMuCAFMenuItem() {
		return runMuCAFMenuItem;
	}

	public MenuItem getRunURAFMenuItem() {
		return runURAFMenuItem;
	}

	public MenuItem getRunMinIRAFMenuItem() {
		return runMinIRAFMenuItem;
	}

	public MenuItem getRunTrivialCAFsAlgorithmMenuItem() {
		return runTrivialCAFsAlgorithmMenuItem;
	}

	public MenuItem getRunTrivialRAFsAlgorithmMenuItem() {
		return runTrivialRAFsAlgorithmMenuItem;
	}

	public MenuItem getRunQuotientRAFMenuItem() {
		return runQuotientRAFMenuItem;
	}

	public MenuItem getRunCoreRAFMenuItem() {
		return runCoreRAFMenuItem;
	}

	public MenuItem getRemoveTrivialRAFsAlgorithmMenuItem() {
		return removeTrivialRAFsAlgorithmMenuItem;
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

	public MenuItem getReactionDependenciesMenuItem() {
		return reactionDependenciesMenuItem;
	}

	public MenuItem getMoleculeDependenciesMenuItem() {
		return moleculeDependenciesMenuItem;
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

	public MenuItem getShowNodeAndEdgeFormatMenuItem() {
		return showNodeAndEdgeFormatMenuItem;
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

	public RadioMenuItem getReactionDependencyGraphRadioMenuItem() {
		return reactionDependencyGraphRadioMenuItem;
	}

	public RadioMenuItem getMoleculeDependencyGraphRadioMenuItem() {
		return moleculeDependencyGraphRadioMenuItem;
	}

	public RadioMenuItem getAssociationGraphRadioMenuItem() {
		return associationGraphRadioMenuItem;
	}

	public RadioMenuItem getReactantAssociationRadioMenuItem() {
		return reactantAssociationRadioMenuItem;
	}

	public CheckMenuItem getSuppressCatalystEdgesMenuItem() {
		return suppressCatalystEdgesMenuItem;
	}

	public CheckMenuItem getSuppressFormalFoodMenuItem() {
		return suppressFormalFoodMenuItem;
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

	public Tab getParsedReactionsTab() {
		return parsedReactionsTab;
	}

	public TextArea getParsedReactionsTextArea() {
		return parsedReactionsTextArea;
	}

	public VBox getParsedReactionsVBox() {
		return parsedReactionsVBox;
	}

	public Tab getNetworkTab() {
		return networkTab;
	}

	public Label getGraphTypeLabel() {
		return graphTypeLabel;
	}

	public BorderPane getNetworkBorderPane() {
		return networkBorderPane;
	}

	public VBox getNetworkVBox() {
		return networkVBox;
	}

	public ContextMenu getNetworkContextMenu() {
		return networkContextMenu;
	}

	public MenuItem getSelectNoneContextMenuItem() {
		return selectNoneContextMenuItem;
	}

	public MenuItem getSelectConnectedComponentContextMenuItem() {
		return selectConnectedComponentContextMenuItem;
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

	public SplittableTabPane getOutputTabPane() {
		return outputSplittableTabPane;
	}

	public ContextMenu getNetworkTabContextMenu() {
		return networkTabContextMenu;
	}

	public Label getInputFoodSizeLabel() {
		return inputFoodSizeLabel;
	}

	public Label getInputReactionsSizeLabel() {
		return inputReactionsSizeLabel;
	}

	public ZoomableScrollPane getNetworkScrollPane() {
		return zoomableScrollPane;
	}

	public CheckMenuItem getMoveLabelsMenuItem() {
		return moveLabelsMenuItem;
	}

	public CheckMenuItem getUseColorsMenuItem() {
		return useColorsMenuItem;
	}

	public CheckMenuItem getUseDarkThemeCheckMenuItem() {
		return useDarkThemeCheckMenuItem;
	}

	public MenuItem getListCatalystsMenuItem() {
		return listCatalystsMenuItem;
	}

	public MenuItem getListInhibitorsMenuItem() {
		return listInhibitorsMenuItem;
	}

	public MenuItem getListProductsMenuItem() {
		return listProductsMenuItem;
	}

	public MenuItem getListReactantsMenuItem() {
		return listReactantsMenuItem;
	}

	public MenuItem getListReactionsMenuItem() {
		return listReactionsMenuItem;
	}

	public MenuItem getListFoodMenuItem() {
		return listFoodMenuItem;
	}

	public MenuItem getExportImageNetworkMenuItem() {
		return exportImageNetworkMenuItem;
	}

	public StackPane getNetworkPane() {
		return networkPane;
	}

	public ToggleButton getFindNetworkToggleButton() {
		return findNetworkToggleButton;
	}

	public MenuButton getAnimateNetworkMenuButton() {
		return animateNetworkMenuButton;
	}

	public ToggleButton getFindParsedReactionsToggleButton() {
		return findParsedReactionsToggleButton;
	}

	public ToggleButton getFindLogToggleButton() {
		return findLogToggleButton;
	}

	public MenuButton getExportNetworkMenuButton() {
		return exportNetworkMenuButton;
	}
}