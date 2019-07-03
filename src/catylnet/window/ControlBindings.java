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

package catylnet.window;

import catylnet.action.*;
import catylnet.io.Save;
import catylnet.io.SaveChangesDialog;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.TextAreaSearcher;
import jloda.fx.util.NotificationManager;
import jloda.fx.util.Print;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

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
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().cut();
        });
        controller.getCutMenuItem().disableProperty().bind(controller.getCrsTextArea().selectedTextProperty().isEmpty());

        controller.getCopyMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().copy();
        });
        controller.getCopyMenuItem().disableProperty().bind(controller.getCutMenuItem().disableProperty());

        controller.getPasteMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().paste();
        });

        controller.getClearMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().deleteText(0, controller.getCrsTextArea().getText().length());
        });
        controller.getCutMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getUndoMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(controller.getCrsTextArea().undoableProperty().not());

        controller.getRedoMenuItem().setOnAction((e) -> {
            if (controller.getCrsTextArea().isFocused())
                controller.getCrsTextArea().redo();
        });
        controller.getRedoMenuItem().disableProperty().bind(controller.getCrsTextArea().redoableProperty().not());

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().selectAll();
        });
        controller.getSelectAllMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            controller.getCrsTextArea().selectRange(0, 0);
        });
        controller.getSelectNoneMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getVerifyInputMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window))
                NotificationManager.showInformation(String.format("Input is valid. Found %,d reactions and %,d food items", window.getModel().getReactions().size(), window.getModel().getFoods().size()));
        });
        controller.getRunRAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getRafTab().getTabPane().getSelectionModel().select(controller.getRafTab());
                RunRAF.apply(window);
            }
        });
        controller.getRunRAFMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getRunCAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getCafTab().getTabPane().getSelectionModel().select(controller.getCafTab());
                RunCAF.apply(window);
            }
        });
        controller.getRunCAFMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getRunPseudoRAFMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                controller.getPseudoRafTab().getTabPane().getSelectionModel().select(controller.getPseudoRafTab());
                RunPseudoRAF.apply(window);
            }
        });
        controller.getRunPseudoRAFMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getRunMenuItem().setOnAction((e) -> {
            if (ParseInput.apply(window)) {
                RunCAF.apply(window);
                RunRAF.apply(window);
                RunPseudoRAF.apply(window);
            }
        });
        controller.getRunMenuItem().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getRunButton().setOnAction(controller.getRunMenuItem().getOnAction());
        controller.getRunButton().disableProperty().bind(controller.getCrsTextArea().textProperty().isEmpty());

        controller.getRafTab().disableProperty().bind(controller.getRafTextArea().textProperty().isEmpty());
        controller.getCafTab().disableProperty().bind(controller.getCafTextArea().textProperty().isEmpty());
        controller.getPseudoRafTab().disableProperty().bind(controller.getPseudoRAFTextArea().textProperty().isEmpty());

        controller.getAboutMenuItem().setOnAction((e) -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(2)));

        final FindToolBar findToolBar = new FindToolBar(new TextAreaSearcher("Input", controller.getCrsTextArea()));
        controller.getReactionsInputVBox().getChildren().add(findToolBar);
        findToolBar.showFindToolBarProperty().bindBidirectional(controller.getFindInInputButton().selectedProperty());

        controller.getFindMenuItem().setOnAction((e) -> {
            controller.getCrsTextArea().requestFocus();
            if (!findToolBar.isShowFindToolBar())
                findToolBar.setShowFindToolBar(true);
        });

        controller.getFindAgainMenuItem().setOnAction((e) -> {
            findToolBar.findAgain();
        });
        controller.getFindAgainMenuItem().disableProperty().bind(findToolBar.canFindAgainProperty().not());

        window.getStage().widthProperty().addListener((c, o, n) -> {
            if (!Double.isNaN(o.doubleValue()) && n.doubleValue() > 0)
                controller.getMainSplitPane().setDividerPosition(0, controller.getMainSplitPane().getDividerPositions()[0] * o.doubleValue() / n.doubleValue());
        });
        if (window.getStage().getWidth() > 0)
            controller.getMainSplitPane().setDividerPosition(0, 200.0 / window.getStage().getWidth());
    }

}
