/*
 * MainWindowController.java Copyright (C) 2019. Daniel H. Huson
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

package catylnet.window;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import jloda.fx.window.SplashScreen;
import jloda.util.ProgramProperties;

import java.net.URL;
import java.time.Duration;
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
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private Menu algorithmMenu;

    @FXML
    private MenuItem analyzeMenuItem;

    @FXML
    private Menu helpMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private Button analyzeButton;

    @FXML
    private FlowPane statusFlowPane;

    @FXML
    private Label memoryUsageLabel;

    @FXML
    private TextArea crsTextArea;

    @FXML
    private ComboBox<?> foodSourcesComboBox;

    @FXML
    private TextArea propertiesTextArea;

    @FXML
    void initialize() {
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert newMenuItem != null : "fx:id=\"newMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
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
        assert clearMenuItem != null : "fx:id=\"deleteMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert selectNoneMenuItem != null : "fx:id=\"selectNoneMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert algorithmMenu != null : "fx:id=\"algorithmMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert analyzeMenuItem != null : "fx:id=\"analyzeMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert helpMenu != null : "fx:id=\"helpMenu\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert analyzeButton != null : "fx:id=\"analyzeButton\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusFlowPane != null : "fx:id=\"statusFlowPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert memoryUsageLabel != null : "fx:id=\"memoryUsageLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert crsTextArea != null : "fx:id=\"crsTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert foodSourcesComboBox != null : "fx:id=\"foodSourcesComboBox\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert propertiesTextArea != null : "fx:id=\"propertiesTextArea\" was not injected: check your FXML file 'MainWindow.fxml'.";


        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            fileMenu.getItems().remove(getQuitMenuItem());
            helpMenu.getItems().remove(getAboutMenuItem());
            //editMenu.getItems().remove(getPreferencesMenuItem());
        } else {
            getAboutMenuItem().setOnAction((e) -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(1)));
        }


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

    public MenuItem getDeleteMenuItem() {
        return clearMenuItem;
    }

    public MenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public MenuItem getSelectNoneMenuItem() {
        return selectNoneMenuItem;
    }

    public Menu getAlgorithmMenu() {
        return algorithmMenu;
    }

    public MenuItem getAnalyzeMenuItem() {
        return analyzeMenuItem;
    }

    public Menu getHelpMenu() {
        return helpMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public Button getAnalyzeButton() {
        return analyzeButton;
    }

    public FlowPane getStatusFlowPane() {
        return statusFlowPane;
    }

    public Label getMemoryUsageLabel() {
        return memoryUsageLabel;
    }

    public TextArea getCrsTextArea() {
        return crsTextArea;
    }

    public TextArea getPropertiesTextArea() {
        return propertiesTextArea;
    }

    public ComboBox<?> getFoodSourcesComboBox() {
        return foodSourcesComboBox;
    }
}
