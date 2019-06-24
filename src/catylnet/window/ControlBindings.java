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

import catylnet.io.Save;
import catylnet.io.SaveChangesDialog;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.WindowGeometry;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

public class ControlBindings {
    private static int windowsCreated = 0;

    public static void setup(MainWindow window) {
        final MainWindowController controller = window.getController();

        RecentFilesManager.getInstance().setFileOpener(FileOpenManager.getFileOpener());
        RecentFilesManager.getInstance().setupMenu(controller.getRecentFilesMenu());

        window.getStage().setOnCloseRequest((e) -> {
            controller.getCloseMenuItem().getOnAction().handle(null);
            e.consume();
        });

        controller.getNewMenuItem().setOnAction((e) -> {
            final MainWindow newWindow = (MainWindow) MainWindowManager.getInstance().createAndShowWindow(false);
            newWindow.getStage().setTitle(ProgramProperties.getProgramName() + " [" + (++windowsCreated) + "]");
            MainWindowManager.getInstance().setLastFocusedMainWindow(newWindow);
        });

        controller.getOpenMenuItem().setOnAction(FileOpenManager.createOpenFileEventHandler(window.getStage()));

        controller.getSaveMenItem().setOnAction(e -> Save.showSaveDialog(window));

        controller.getCloseMenuItem().setOnAction(e -> {
            if (SaveChangesDialog.apply(window)) {
                ProgramProperties.put("WindowGeometry", (new WindowGeometry(window.getStage())).toString());
                MainWindowManager.getInstance().closeMainWindow(window);
            }
        });
    }
}
