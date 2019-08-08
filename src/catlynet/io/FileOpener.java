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

package catlynet.io;

import catlynet.action.NewWindow;
import catlynet.action.ParseInput;
import catlynet.format.ArrowNotation;
import catlynet.format.ReactionNotation;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * opens a file
 * Daniel Huson, 6.2019
 */
public class FileOpener implements Consumer<String> {

    @Override
    public void accept(String fileName) {
        MainWindow window = (MainWindow) MainWindowManager.getInstance().getLastFocusedMainWindow();
        if (window == null || !window.isEmpty())
            window = NewWindow.apply();

        final ReactionSystem reactionSystem = window.getInputReactionSystem();

        try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {
            window.getDocument().setFileName(fileName);
            final Pair<ReactionNotation, ArrowNotation> pair = ReactionNotation.detectNotation(new File(fileName));
            if (pair == null)
                throw new IOException("Couldn't detect 'full', 'sparse' or 'tabbed' file format");

            reactionSystem.clear();
            final String leadingComments = ModelIO.read(window.getInputReactionSystem(), r, pair.getFirst());

            window.getController().getInputTextArea().setText((leadingComments.length() > 0 ? leadingComments + "\n" : "") + ModelIO.toString(window.getInputReactionSystem(), false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
            final String food = ModelIO.getFoodString(window.getInputReactionSystem(), window.getDocument().getReactionNotation());
            if (window.getController().getFoodSetComboBox().getItems().contains(food))
                window.getController().getFoodSetComboBox().getItems().add(0, food);
            window.getController().getFoodSetComboBox().getSelectionModel().select(food);

            final String infoString = "Read " + reactionSystem.size() + " reactions" + (reactionSystem.getNumberOfTwoWayReactions() > 0 ? "(" + reactionSystem.getNumberOfTwoWayReactions() + " two-way)" : "")
                    + " and " + reactionSystem.getFoods().size() + " food items from file: " + fileName;

            NotificationManager.showInformation(infoString);

            window.getLogStream().println(infoString);
            // window.getLogStream().println("Input format:   " + pair.getFirst());
            // window.getLogStream().println("Display format: " + window.getDocument().getReactionNotation());
            RecentFilesManager.getInstance().insertRecentFile(fileName);

            ParseInput.apply(window);

        } catch (IOException e) {
            NotificationManager.showError("Open file failed: " + e.getMessage());
        }
    }
}
