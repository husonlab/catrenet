/*
 * SetupFind.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.tab.TextTab;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.GraphSearcher;
import jloda.fx.find.TextAreaSearcher;

import java.util.stream.Collectors;

/**
 * setup find
 * Daniel Huson, 2.2020
 */
public class SetupFind {
    /**
     * setup the find dialog
     *
	 */
    static void apply(MainWindow window) {
        var controller = window.getController();

        var inputFoodFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Input food", controller.getInputFoodTextArea()));
        controller.getFoodInputVBox().getChildren().add(inputFoodFindToolBar);

        var inputReactionsFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Input reactions", controller.getInputTextArea()));
        controller.getReactionsInputVBox().getChildren().add(inputReactionsFindToolBar);

        var workingInputReactionsFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Expanded reactions", controller.getWorkingReactionsTextArea()));
        controller.getWorkingReactionsVBox().getChildren().add(workingInputReactionsFindToolBar);
        controller.getFindWorkingReactionsToggleButton().selectedProperty().bindBidirectional(workingInputReactionsFindToolBar.showFindToolBarProperty());

        var logFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Log", controller.getLogTextArea()));
        controller.getLogVBox().getChildren().add(logFindToolBar);
        controller.getFindLogToggleButton().selectedProperty().bindBidirectional(logFindToolBar.showFindToolBarProperty());

        controller.getOutputTabPane().getTabs().addListener((ListChangeListener<Tab>) z -> {
            while (z.next()) {
                for (var textTab : z.getAddedSubList().stream().filter(t -> t.getUserData() instanceof TextTab).map(t -> (TextTab) t.getUserData()).collect(Collectors.toList())) {
                    var findToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher(textTab.getReactionSystemName(), textTab.getTextArea()));
                    textTab.setFindToolBar(findToolBar);
                    textTab.getController().getFindToggleButton().selectedProperty().bindBidirectional(findToolBar.showFindToolBarProperty());
                }
            }
        });

        var gv = window.getReactionGraphView();
        var graphSearcher = new GraphSearcher(gv.getReactionGraph(), gv.getNodeSelection(), (v) -> gv.getLabel(v).getText(), (v, t) -> gv.getLabel(v).setText(t));
        graphSearcher.foundProperty().addListener((c, o, n) -> {
            if (n != null && gv.getLabel(n) != null) {
                controller.getNetworkScrollPane().ensureVisible(gv.getLabel(n));
            }
        });
        var networkFindToolBar = new FindToolBar(window.getStage(), graphSearcher);

        controller.getFindNetworkToggleButton().selectedProperty().bindBidirectional(networkFindToolBar.showFindToolBarProperty());

        controller.getNetworkVBox().getChildren().add(networkFindToolBar);

        controller.getFindMenuItem().setOnAction(e -> {
            if (controller.getInputFoodTextArea().isFocused())
                inputFoodFindToolBar.setShowFindToolBar(true);
            else if (controller.getInputTextArea().isFocused())
                inputReactionsFindToolBar.setShowFindToolBar(true);
            else if (controller.getWorkingReactionsTextArea().isFocused())
                workingInputReactionsFindToolBar.setShowFindToolBar(true);
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.setShowFindToolBar(true);
            else if (controller.getNetworkTab().isSelected() || controller.getNetworkBorderPane().isFocused())
                networkFindToolBar.setShowFindToolBar(true);
            else {
                for (var textTab : window.getTabManager().textTabs()) {
                    if (textTab.getTab().isSelected() || textTab.getTextArea().isFocused()) {
                        textTab.getFindToolBar().setShowFindToolBar(true);
                        break;
                    }
                }
            }
        });

        controller.getFindAgainMenuItem().setOnAction((e) -> {
            if (controller.getInputFoodTextArea().isFocused())
                inputFoodFindToolBar.findAgain();
            else if (controller.getWorkingReactionsTextArea().isFocused())
                workingInputReactionsFindToolBar.findAgain();
            else if (controller.getInputTextArea().isFocused())
                inputReactionsFindToolBar.findAgain();
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.findAgain();
            else if (controller.getNetworkTab().isSelected() || controller.getNetworkBorderPane().isFocused())
                networkFindToolBar.findAgain();
            else {
                for (var textTab : window.getTabManager().textTabs()) {
                    if (textTab.getTab().isSelected() || textTab.getTextArea().isFocused()) {
                        textTab.getFindToolBar().findAgain();
                        break;
                    }
                }
            }
        });
    }
}
