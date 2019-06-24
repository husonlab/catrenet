/*
 * FileOpener.java Copyright (C) 2019. Daniel H. Huson
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

package catylnet.io;

import catylnet.window.MainWindow;
import jloda.fx.util.NotificationManager;
import jloda.util.Basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * opens a file
 * Daniel Huson, 6.2019
 */
public class FileOpener implements Consumer<String> {
    private final MainWindow mainWindow;

    public FileOpener(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public void accept(String fileName) {
        try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {
            mainWindow.getModel().clear();
            mainWindow.getModel().read(r);
            mainWindow.getController().getCrsTextArea().setText(Basic.toString(mainWindow.getModel().getReactions(), "\n"));
            final String food = Basic.toString(mainWindow.getModel().getFoods(), " ");
            if (mainWindow.getController().getFoodSourcesComboBox().getItems().contains(food))
                mainWindow.getController().getFoodSourcesComboBox().getItems().add(0, food);
            mainWindow.getController().getFoodSourcesComboBox().getSelectionModel().select(food);

            NotificationManager.showInformation("Read " + mainWindow.getModel().getReactions().size() + " reactions and " +
                    mainWindow.getModel().getFoods().size() + " foods from file: " + fileName);
        } catch (IOException e) {
            NotificationManager.showError("Open file failed: " + e.getMessage());
        }
    }
}
