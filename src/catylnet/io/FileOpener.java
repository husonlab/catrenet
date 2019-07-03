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

import catylnet.action.NewWindow;
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
    private final MainWindow window;

    public FileOpener(MainWindow window) {
        this.window = window;
    }

    @Override
    public void accept(String fileName) {
        if (window.isEmpty()) {
            try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {
                window.getDocument().setFileName(fileName);
                window.getModel().clear();
                window.getModel().read(r);
                window.getController().getCrsTextArea().setText(window.getModel().getReactionsAsString());
                final String food = Basic.toString(window.getModel().getFoods(), " ");
                if (window.getController().getFoodSetComboBox().getItems().contains(food))
                    window.getController().getFoodSetComboBox().getItems().add(0, food);
                window.getController().getFoodSetComboBox().getSelectionModel().select(food);

                NotificationManager.showInformation("Read " + window.getModel().getReactions().size() + " reactions and " +
                        window.getModel().getFoods().size() + " foods from file: " + fileName);
            } catch (IOException e) {
                NotificationManager.showError("Open file failed: " + e.getMessage());
            }
        } else {
            new FileOpener(NewWindow.apply()).accept(fileName);
        }
    }
}
