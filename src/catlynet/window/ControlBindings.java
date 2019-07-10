/*
 * ControlBindings.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.action.*;
import catlynet.io.ModelIO;
import catlynet.io.Save;
import catlynet.io.SaveChangesDialog;
import javafx.application.Platform;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.TextAreaSearcher;
import jloda.fx.util.NotificationManager;
import jloda.fx.util.Print;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;

public class ControlBindings {

    public static void setup(MainWindow window) {
        final MainWindowController controller = window.getController();

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getRecentFilesMenu());

        window.getStage().setOnCloseRequest((e) -> {
            controller.getCloseMenuItem().getOnAction().handle(null);
            e.consume();
        });

        controller.getNewMenuItem().setOnAction((e) -> {
            NewWindow.apply();
        });

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        controller.getSaveMenItem().setOnAction(e -> Save.showSaveDialog(window));

        controller.getCloseMenuItem().setOnAction(e -> {
            if (SaveChangesDialog.apply(window)) {
                ProgramProperties.put("WindowGeometry", (new WindowGeometry(window.getStage())).toString());
                MainWindowManager.getInstance().closeMainWindow(window);
            }
        });

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(window.getStage()));
        controller.getPrintMenuItem().setOnAction((e) -> Print.print(window.getStage(), controller.getMainSplitPane()));

        controller.getCutMenuItem().setOnAction((e) -> {
            if (!controller.getInputTextArea().isFocused()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().cut();
            }
        });
        controller.getCutMenuItem().disableProperty().bind(controller.getInputTextArea().selectedTextProperty().isEmpty());

        controller.getCopyMenuItem().setOnAction((e) -> {
            if (!controller.getInputTextArea().isFocused()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().copy();
            }
        });
        controller.getCopyMenuItem().disableProperty().bind(controller.getCutMenuItem().disableProperty());

        controller.getPasteMenuItem().setOnAction((e) -> {
            if (!controller.getInputTextArea().isFocused()) {
                controller.getInputTextArea().requestFocus();
                controller.getInputTextArea().paste();
            }
        });

        controller.getClearMenuItem().setOnAction((e) -> {
            controller.getInputTextArea().requestFocus();
            controller.getInputTextArea().deleteText(0, controller.getInputTextArea().getText().length());
        });
        controller.getCutMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getUndoMenuItem().setOnAction((e) -> {
            controller.getInputTextArea().requestFocus();
            controller.getInputTextArea().undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(controller.getInputTextArea().undoableProperty().not());

        controller.getRedoMenuItem().setOnAction((e) -> {
            controller.getInputTextArea().requestFocus();
            controller.getInputTextArea().redo();
        });
        controller.getRedoMenuItem().disableProperty().bind(controller.getInputTextArea().redoableProperty().not());

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            controller.getInputTextArea().requestFocus();
            controller.getInputTextArea().selectAll();
        });
        controller.getSelectAllMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            controller.getInputTextArea().requestFocus();
            controller.getInputTextArea().selectRange(0, 0);
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getVerifyInputMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getReactionsTab().getTabPane().getSelectionModel().select(controller.getReactionsTab());
                try (StringWriter w = new StringWriter()) {
                    ModelIO.write(window.getModel(), w, false, false);
                    controller.getReactionsTextArea().setText(w.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                final String message = String.format("Input is valid. Found %,d reactions and %,d food items", window.getModel().getReactions().size(), window.getModel().getFoods().size());
                NotificationManager.showInformation(message);
                window.getLogStream().println(message);
            }
        });

        controller.getRunRAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getRafTab().getTabPane().getSelectionModel().select(controller.getRafTab());
                RunRAF.apply(window);
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunCAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getCafTab().getTabPane().getSelectionModel().select(controller.getCafTab());
                RunCAF.apply(window);
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunPseudoRAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getPseudoRafTab().getTabPane().getSelectionModel().select(controller.getPseudoRafTab());
                RunPseudoRAF.apply(window);
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunMenuItem().setOnAction((e) -> {
            controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab());
            window.getLogStream().println("Run:");

            if (ParseInput.apply(window)) {
                try (StringWriter w = new StringWriter()) {
                    ModelIO.write(window.getModel(), w, false, false);
                    controller.getReactionsTextArea().setText(w.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                RunCAF.apply(window);
                RunRAF.apply(window);
                RunPseudoRAF.apply(window);
            }
        });
        controller.getRunMenuItem().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getRunButton().setOnAction(controller.getRunMenuItem().getOnAction());
        controller.getRunButton().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getReactionsTab().disableProperty().bind(controller.getReactionsTextArea().textProperty().isEmpty());
        controller.getRafTab().disableProperty().bind(controller.getRafTextArea().textProperty().isEmpty());
        controller.getCafTab().disableProperty().bind(controller.getCafTextArea().textProperty().isEmpty());
        controller.getPseudoRafTab().disableProperty().bind(controller.getPseudoRAFTextArea().textProperty().isEmpty());

        controller.getAboutMenuItem().setOnAction((e) -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(2)));


        window.getStage().widthProperty().addListener((c, o, n) -> {
            if (!Double.isNaN(o.doubleValue()) && n.doubleValue() > 0)
                controller.getMainSplitPane().setDividerPosition(0, controller.getMainSplitPane().getDividerPositions()[0] * o.doubleValue() / n.doubleValue());
        });
        if (window.getStage().getWidth() > 0)
            controller.getMainSplitPane().setDividerPosition(0, 200.0 / window.getStage().getWidth());

        setupFind(window, controller);

        controller.getLogTextArea().appendText(Basic.stopCollectingStdErr());
    }

    private static void setupFind(MainWindow window, MainWindowController controller) {
        final FindToolBar inputFindToolBar = new FindToolBar(new TextAreaSearcher("Input", controller.getInputTextArea()));
        controller.getReactionsInputVBox().getChildren().add(inputFindToolBar);

        final FindToolBar logFindToolBar = new FindToolBar(new TextAreaSearcher("Log", controller.getLogTextArea()));
        controller.getLogVBox().getChildren().add(logFindToolBar);


        final FindToolBar cafFindToolBar = new FindToolBar(new TextAreaSearcher("CAF", controller.getCafTextArea()));
        controller.getCafVBox().getChildren().add(cafFindToolBar);


        final FindToolBar rafFindToolBar = new FindToolBar(new TextAreaSearcher("RAF", controller.getRafTextArea()));
        controller.getRafVBox().getChildren().add(rafFindToolBar);


        final FindToolBar pseudoRafFindToolBar = new FindToolBar(new TextAreaSearcher("Pseudo-RAF", controller.getPseudoRAFTextArea()));
        controller.getPseudoRafVBox().getChildren().add(pseudoRafFindToolBar);

        controller.getFindMenuItem().setOnAction((e) -> {
            if (controller.getInputTextArea().isFocused())
                inputFindToolBar.setShowFindToolBar(true);
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.setShowFindToolBar(true);
            else if (controller.getCafTab().isSelected() || controller.getCafTextArea().isFocused())
                cafFindToolBar.setShowFindToolBar(true);
            else if (controller.getRafTab().isSelected() || controller.getRafTextArea().isFocused())
                rafFindToolBar.setShowFindToolBar(true);
            else if (controller.getPseudoRafTab().isSelected() || controller.getPseudoRAFTextArea().isFocused())
                pseudoRafFindToolBar.setShowFindToolBar(true);
        });

        controller.getFindAgainMenuItem().setOnAction((e) -> {
            if (controller.getInputTextArea().isFocused())
                inputFindToolBar.findAgain();
            else if (controller.getLogTab().isSelected() || controller.getLogTextArea().isFocused())
                logFindToolBar.findAgain();
            else if (controller.getCafTab().isSelected() || controller.getCafTextArea().isFocused())
                cafFindToolBar.findAgain();
            else if (controller.getRafTab().isSelected() || controller.getRafTextArea().isFocused())
                rafFindToolBar.findAgain();
            else if (controller.getPseudoRafTab().isSelected() || controller.getPseudoRAFTextArea().isFocused())
                pseudoRafFindToolBar.findAgain();
        });

        Platform.runLater(() -> controller.getLogTab().getTabPane().getSelectionModel().select(controller.getLogTab()));
    }

}
