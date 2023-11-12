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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.GraphSearcher;
import jloda.fx.find.ISearcher;
import jloda.fx.find.TextAreaSearcher;
import jloda.fx.icons.MaterialIcons;

/**
 * setup find
 * Daniel Huson, 2.2020
 */
public class SetupFind {
    /**
     * setup the find dialog
     *
	 */

	public static void apply(MainWindow window) {
		var emptySearcher = new EmptySearcher();
        var controller = window.getController();


		var findToolBar = new FindToolBar(window.getStage(), emptySearcher);

		findToolBar.getController().getAnchorPane().getStylesheets().add(MaterialIcons.getInstance().getStyleSheet());

        var gv = window.getReactionGraphView();
        var graphSearcher = new GraphSearcher(gv.getReactionGraph(), gv.getNodeSelection(), (v) -> gv.getLabel(v).getText(), (v, t) -> gv.getLabel(v).setText(t));
        graphSearcher.foundProperty().addListener((c, o, n) -> {
            if (n != null && gv.getLabel(n) != null) {
                controller.getNetworkScrollPane().ensureVisible(gv.getLabel(n));
            }
        });

		var logSearcher = new TextAreaSearcher("Log", controller.getLogTextArea());
		var parsedSearcher = new TextAreaSearcher("Parsed", controller.getParsedReactionsTextArea());

		controller.getTopMainVBox().getChildren().add(findToolBar);
		controller.getFindButton().selectedProperty().bindBidirectional(findToolBar.showFindToolBarProperty());

		controller.getFindMenuItem().setOnAction(e -> {
			controller.getFindButton().setSelected(true);
		});
		controller.getFindAgainMenuItem().setOnAction(e -> findToolBar.findAgain());
		controller.getFindAgainMenuItem().disableProperty().bind(findToolBar.canFindAgainProperty());

		controller.getOutputTabPane().getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
			if (n == null)
				findToolBar.setSearchers(emptySearcher);
			else if (n == controller.getNetworkTab()) {
				findToolBar.setSearchers(graphSearcher);
			} else if (n == controller.getLogTab()) {
				findToolBar.setSearchers(logSearcher);
			} else if (n == controller.getParsedReactionsTab()) {
				findToolBar.setSearchers(parsedSearcher);
			} else if (n instanceof TextTab textTab) {
				findToolBar.setSearchers(textTab.getSearcher());
			} else
				findToolBar.setSearchers(emptySearcher);
		});

		var inputFoodFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Input food", controller.getInputFoodTextArea()));
		inputFoodFindToolBar.getController().getAnchorPane().getStylesheets().add(MaterialIcons.getInstance().getStyleSheet());

		controller.getFoodInputVBox().getChildren().add(inputFoodFindToolBar);
		controller.getFindFoodTextToggleButton().selectedProperty().bindBidirectional(inputFoodFindToolBar.showFindToolBarProperty());

		var inputReactionsFindToolBar = new FindToolBar(window.getStage(), new TextAreaSearcher("Input reactions", controller.getInputTextArea()));
		inputReactionsFindToolBar.getController().getAnchorPane().getStylesheets().add(MaterialIcons.getInstance().getStyleSheet());

		controller.getReactionsInputVBox().getChildren().add(inputReactionsFindToolBar);
		controller.getFindReactionsTextToggleButton().selectedProperty().bindBidirectional(inputReactionsFindToolBar.showFindToolBarProperty());

	}

	public static class EmptySearcher implements ISearcher {
		@Override
		public String getName() {
			return "empty";
		}

		@Override
		public ReadOnlyBooleanProperty isGlobalFindable() {
			return new SimpleBooleanProperty(false);
		}

		@Override
		public ReadOnlyBooleanProperty isSelectionFindable() {
			return new SimpleBooleanProperty(false);
		}

		@Override
		public void updateView() {

		}

		@Override
		public boolean canFindAll() {
			return false;
		}

		@Override
		public void selectAll(boolean select) {

		}
    }
}
