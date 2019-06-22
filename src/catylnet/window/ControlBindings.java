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

import jloda.fx.window.MainWindowManager;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

public class ControlBindings {
    private static int windowsCreated = 0;

    public static void setup(MainWindow window) {
        final MainWindowController controller = window.getController();

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

        controller.getCloseMenuItem().setOnAction(e -> {
            if (window.getDocument().isDirty()) {
                // ask to save
            }
            MainWindowManager.getInstance().closeMainWindow(window);
        });
    }
}
