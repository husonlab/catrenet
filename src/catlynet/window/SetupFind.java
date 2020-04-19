/*
 * SetupFind.java Copyright (C) 2020. Daniel H. Huson
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

import catlynet.tab.TextTab;
import catlynet.view.ReactionGraphView;
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
     * @param window
     */
    static void apply(MainWindow window) {
        final MainWindowController controller = window.getController();

        final FindToolBar inputFoodFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Input food", controller.getInputFoodTextArea()));
        controller.getFoodInputVBox().getChildren().add(inputFoodFindToolBar);

        final FindToolBar inputReactionsFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Input reactions", controller.getInputTextArea()));
        controller.getReactionsInputVBox().getChildren().add(inputReactionsFindToolBar);

        final FindToolBar expandedReactionsFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Expanded reactions", controller.getWorkingReactionsTextArea()));
        controller.getWorkingReactionsVBox().getChildren().add(expandedReactionsFindToolBar);

        final FindToolBar logFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Log", controller.getLogTextArea()));
        controller.getLogVBox().getChildren().add(logFindToolBar);

        controller.getOutputTabPane().getTabs().addListener((ListChangeListener<Tab>) z -> {
            while (z.next()) {
                for (TextTab textTab : z.getAddedSubList().stream().filter(t -> t.getUserData() instanceof TextTab).map(t -> (TextTab) t.getUserData()).collect(Collectors.toList())) {
                    final FindToolBar findToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher(textTab.getReactionSystemName(), textTab.getTextArea()));
                    textTab.setFindToolBar(findToolBar);
                }
            }
        });

        final ReactionGraphView gv = window.getReactionGraphView();
        final FindToolBar graphFindToolBar = new FindToolBar(window.getStage(), new GraphSearcher(window.getController().getVisualizationScrollPane(), gv.getReactionGraph(), gv.getNodeSelection(), gv::getLabel, null));
        controller.getVisualizationVBox().getChildren().add(graphFindToolBar);

        controller.getFindMenuItem().setOnAction((e) -> {
            if (controller.getInputFoodTextArea().isFocused())
                inputFoodFindToolBar.setShowFindToolBar(true);
            else if (controller.getInputTextArea().isFocused())
                inputReactionsFindToolBar.setShowFindToolBar(true);
            else if (controller.getWorkingReactionsTextArea().isFocused())
                expandedReactionsFindToolBar.setShowFindToolBar(true);
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.setShowFindToolBar(true);
            else if (controller.getVisualizationTab().isSelected() || controller.getVisualizationBorderPane().isFocused())
                graphFindToolBar.setShowFindToolBar(true);
            else {
                for (TextTab textTab : window.getTabManager().textTabs()) {
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
                expandedReactionsFindToolBar.findAgain();
            else if (controller.getInputTextArea().isFocused())
                inputReactionsFindToolBar.findAgain();
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.findAgain();
            else if (controller.getVisualizationTab().isSelected() || controller.getVisualizationBorderPane().isFocused())
                graphFindToolBar.findAgain();
            else {
                for (TextTab textTab : window.getTabManager().textTabs()) {
                    if (textTab.getTab().isSelected() || textTab.getTextArea().isFocused()) {
                        textTab.getFindToolBar().findAgain();
                        break;
                    }
                }
            }
        });
    }
}
