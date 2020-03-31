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

import catlynet.view.ReactionGraphView;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.GraphSearcher;
import jloda.fx.find.TextAreaSearcher;

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

        final FindToolBar inputFoodFindToolBar = new FindToolBar(new TextAreaSearcher("Input food", controller.getInputFoodTextArea()));
        controller.getFoodInputVBox().getChildren().add(inputFoodFindToolBar);

        final FindToolBar inputReactionsFindToolBar = new FindToolBar(new TextAreaSearcher("Input reactions", controller.getInputTextArea()));
        controller.getReactionsInputVBox().getChildren().add(inputReactionsFindToolBar);

        final FindToolBar logFindToolBar = new FindToolBar(new TextAreaSearcher("Log", controller.getLogTextArea()));
        controller.getLogVBox().getChildren().add(logFindToolBar);

        final FindToolBar cafFindToolBar = new FindToolBar(new TextAreaSearcher("CAF", controller.getCafTextArea()));
        controller.getCafVBox().getChildren().add(cafFindToolBar);

        final FindToolBar rafFindToolBar = new FindToolBar(new TextAreaSearcher("RAF", controller.getRafTextArea()));
        controller.getRafVBox().getChildren().add(rafFindToolBar);

        final FindToolBar pseudoRafFindToolBar = new FindToolBar(new TextAreaSearcher("Pseudo-RAF", controller.getPseudoRAFTextArea()));
        controller.getPseudoRafVBox().getChildren().add(pseudoRafFindToolBar);

        final FindToolBar minIrrRafFindToolBar = new FindToolBar(new TextAreaSearcher("irr-RAF", controller.getIrrRAFTextArea()));
        controller.getIrrRAFVBox().getChildren().add(minIrrRafFindToolBar);

        final FindToolBar muCAFFindToolBar = new FindToolBar(new TextAreaSearcher("Mu-CAF", controller.getPseudoRAFTextArea()));
        controller.getMuCafVBox().getChildren().add(muCAFFindToolBar);

        final ReactionGraphView gv = window.getReactionGraphView();
        final FindToolBar graphFindToolBar = new FindToolBar(new GraphSearcher(window.getController().getVisualizationScrollPane(), gv.getReactionGraph(), gv.getNodeSelection(), gv::getLabel, null));
        controller.getVisualizationVBox().getChildren().add(graphFindToolBar);

        controller.getFindMenuItem().setOnAction((e) -> {
            if (controller.getInputFoodTextArea().isFocused())
                inputFoodFindToolBar.setShowFindToolBar(true);
            else if (controller.getInputTextArea().isFocused())
                inputReactionsFindToolBar.setShowFindToolBar(true);
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.setShowFindToolBar(true);
            else if (controller.getCafTab().isSelected() || controller.getCafTextArea().isFocused())
                cafFindToolBar.setShowFindToolBar(true);
            else if (controller.getRafTab().isSelected() || controller.getRafTextArea().isFocused())
                rafFindToolBar.setShowFindToolBar(true);
            else if (controller.getPseudoRafTab().isSelected() || controller.getPseudoRAFTextArea().isFocused())
                pseudoRafFindToolBar.setShowFindToolBar(true);
            else if (controller.getIrrRAFTab().isSelected() || controller.getIrrRAFTextArea().isFocused())
                minIrrRafFindToolBar.setShowFindToolBar(true);
            else if (controller.getMuCafTab().isSelected() || controller.getMuCafTextArea().isFocused())
                muCAFFindToolBar.setShowFindToolBar(true);
            else if (controller.getVisualizationTab().isSelected() || controller.getVisualizationBorderPane().isFocused())
                graphFindToolBar.setShowFindToolBar(true);
        });

        controller.getFindAgainMenuItem().setOnAction((e) -> {
            if (controller.getInputFoodTextArea().isFocused())
                inputFoodFindToolBar.findAgain();
            else if (controller.getInputTextArea().isFocused())
                inputReactionsFindToolBar.findAgain();
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.findAgain();
            else if (controller.getCafTab().isSelected() || controller.getCafTextArea().isFocused())
                cafFindToolBar.findAgain();
            else if (controller.getRafTab().isSelected() || controller.getRafTextArea().isFocused())
                rafFindToolBar.findAgain();
            else if (controller.getPseudoRafTab().isSelected() || controller.getPseudoRAFTextArea().isFocused())
                pseudoRafFindToolBar.findAgain();
            else if (controller.getIrrRAFTab().isSelected() || controller.getIrrRAFTextArea().isFocused())
                minIrrRafFindToolBar.findAgain();
            else if (controller.getMuCafTab().isSelected() || controller.getMuCafTextArea().isFocused())
                muCAFFindToolBar.findAgain();
            else if (controller.getVisualizationTab().isSelected() || controller.getVisualizationBorderPane().isFocused())
                graphFindToolBar.findAgain();
        });
    }
}
