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

import catlynet.dialog.ExportTextFileDialog;
import catlynet.tab.TextTab;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import jloda.fx.dialog.ExportImageDialog;
import jloda.fx.find.ISearcher;
import jloda.fx.util.BasicFX;
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
                copyMenuItem.setOnAction(e -> {
                    var graphView = mainWindow.getReactionGraphView();
                    final var content = new ClipboardContent();
                    if (!graphView.getNodeSelection().isEmpty())
                        content.putString(StringUtils.toString(graphView.getSelectedLabels(), "\n"));
                    var parameters = new SnapshotParameters();
                    var bounds = controller.getNetworkCopyPane().getBoundsInParent();
                    var right = BasicFX.isScrollBarVisible(controller.getNetworkScrollPane(), Orientation.VERTICAL) ? 20 : 6;
                    var bottom = BasicFX.isScrollBarVisible(controller.getNetworkScrollPane(), Orientation.HORIZONTAL) ? 20 : 6;

                    parameters.setViewport(new Rectangle2D(bounds.getMinX() + 3, bounds.getMinY() + 3, bounds.getWidth() - right, bounds.getHeight() - bottom));
                    var image = controller.getNetworkCopyPane().snapshot(parameters, null);
                    content.putImage(image);
                    Clipboard.getSystemClipboard().setContent(content);
                });

                exportMenuItem.setOnAction(e -> ExportImageDialog.show(mainWindow.getDocument().getFileName(), mainWindow.getStage(), controller.getNetworkScrollPane().getContent()));
                copyMenuItem.disableProperty().bind(mainWindow.getReactionGraphView().emptyProperty());
                exportMenuItem.disableProperty().bind(mainWindow.getReactionGraphView().emptyProperty());

                controller.getCopyNetworkContextMenuItem().setOnAction(e -> copyMenuItem.getOnAction().handle(e));
                controller.getCopyNetworkContextMenuItem().disableProperty().bind(copyMenuItem.disableProperty());
            } else if (n == controller.getLogTab()) {
                var textArea = controller.getLogTextArea();
                copyMenuItem.setOnAction(e -> {
                    var content = new ClipboardContent();
                    content.putString(textArea.getSelectedText().isEmpty() ? textArea.getText() : textArea.getSelectedText());
                    Clipboard.getSystemClipboard().setContent(content);
                });
                exportMenuItem.setOnAction(e -> {
                    ExportTextFileDialog.apply(mainWindow, "log", textArea.getText());
                });
                copyMenuItem.disableProperty().bind(textArea.textProperty().isEmpty());
                exportMenuItem.disableProperty().bind(textArea.textProperty().isEmpty());

            } else if (n == controller.getParsedReactionsTab()) {
                var textArea = controller.getParsedReactionsTextArea();
                copyMenuItem.setOnAction(e -> {
                    var content = new ClipboardContent();
                    content.putString(textArea.getSelectedText().isEmpty() ? textArea.getText() : textArea.getSelectedText());
                    Clipboard.getSystemClipboard().setContent(content);
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
