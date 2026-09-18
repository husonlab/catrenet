/*
 *  SetupExport.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.window;

import catrenet.dialog.ExportTextFileDialog;
import catrenet.tab.TextTab;
import catrenet.view.ReactionGraphView;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import jloda.fx.dialog.ExportImageDialog;
import jloda.fx.find.ISearcher;
import jloda.fx.print.ImageCropper;
import jloda.fx.print.Print;
import jloda.fx.util.ClipboardUtils;
import jloda.util.StringUtils;

/**
 * setup export
 * Daniel Huson, 8.2023
 */
public class SetupExport {
    /**
     * setup the export menu
     */
    public static void apply(MainWindow mainWindow) {
        var controller = mainWindow.getController();

        var trueProperty = new SimpleBooleanProperty(true);
        var copyMenuItem = controller.getCopyExportMenuItem();
        var exportMenuItem = controller.getExportExportMenuItem();


        controller.getOutputTabPane().getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n == controller.getNetworkTab()) {
				copyMenuItem.setOnAction(e -> copyNetworkImage(mainWindow.getReactionGraphView(), controller.getNetworkScrollPane().getContent()));

				exportMenuItem.setOnAction(e -> ExportImageDialog.show(mainWindow.getDocument().getFileName(), mainWindow.getStage(), controller.getNetworkScrollPane().getContent(), true));
                copyMenuItem.disableProperty().bind(mainWindow.getReactionGraphView().emptyProperty());
                exportMenuItem.disableProperty().bind(mainWindow.getReactionGraphView().emptyProperty());

                controller.getCopyNetworkContextMenuItem().setOnAction(e -> copyMenuItem.getOnAction().handle(e));
                controller.getCopyNetworkContextMenuItem().disableProperty().bind(copyMenuItem.disableProperty());
            } else if (n == controller.getLogTab()) {
                var textArea = controller.getLogTextArea();
                copyMenuItem.setOnAction(e -> {
                    ClipboardUtils.putString(textArea.getSelectedText().isEmpty() ? textArea.getText() : textArea.getSelectedText());
                });
                exportMenuItem.setOnAction(e -> {
                    ExportTextFileDialog.apply(mainWindow, "log", textArea.getText());
                });
                copyMenuItem.disableProperty().bind(textArea.textProperty().isEmpty());
                exportMenuItem.disableProperty().bind(textArea.textProperty().isEmpty());

            } else if (n == controller.getParsedReactionsTab()) {
                var textArea = controller.getParsedReactionsTextArea();
                copyMenuItem.setOnAction(e -> {
                    ClipboardUtils.putString(textArea.getSelectedText().isEmpty() ? textArea.getText() : textArea.getSelectedText());
                });
                exportMenuItem.setOnAction(e -> {
                    ExportTextFileDialog.apply(mainWindow, "parsed", textArea.getText());
                });
                copyMenuItem.disableProperty().bind(textArea.textProperty().isEmpty());
                exportMenuItem.disableProperty().bind(textArea.textProperty().isEmpty());

            } else if (n instanceof TextTab textTab) {
                copyMenuItem.setOnAction(e -> textTab.copyToClipboard());
                exportMenuItem.setOnAction(e -> textTab.exportToFile());
                copyMenuItem.disableProperty().bind(textTab.getTextArea().textProperty().isEmpty());
                exportMenuItem.disableProperty().bind(textTab.getTextArea().textProperty().isEmpty());
            } else {
                copyMenuItem.disableProperty().bind(trueProperty);
                exportMenuItem.disableProperty().bind(trueProperty);
            }

        });

    }

	/**
	 * copy the network image to the clipboard, clipped to a tight bounding box around the painted
	 * content (as done in phyloparallelograms), together with the labels of any selected nodes as text.
	 * The snapshot scale and crop parameters match jloda's ClipboardUtils.putImage().
	 */
	public static void copyNetworkImage(ReactionGraphView graphView, Node content) {
		var string = (graphView.getNodeSelection().isEmpty() ? null : StringUtils.toString(graphView.getSelectedLabels(), "\n"));
		var image = Print.createHighResSnapshot(content, 3.0);
		var cropped = ImageCropper.cropMargins(image, 20, 0.02, 0.1);
		ClipboardUtils.put(string, cropped, null);
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
