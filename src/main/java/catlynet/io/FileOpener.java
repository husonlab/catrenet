/*
 * FileOpener.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.io;

import catlynet.action.ImportWimsFormat;
import catlynet.action.NewWindow;
import catlynet.action.VerifyInput;
import catlynet.settings.ArrowNotation;
import catlynet.settings.ReactionNotation;
import catlynet.window.MainWindow;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.FileUtils;
import jloda.util.Pair;
import jloda.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * opens a file
 * Daniel Huson, 6.2019
 */
public class FileOpener implements Consumer<String> {

    @Override
    public void accept(String fileName) {
        var window = (MainWindow) MainWindowManager.getInstance().getLastFocusedMainWindow();
        if (window == null || !window.isEmpty())
            window = NewWindow.apply();

		var reactionSystem = window.getInputReactionSystem();

        try {
            final ArrayList<String> inputLines;

            final Pair<ReactionNotation, ArrowNotation> notation;

            if (ImportWimsFormat.isInWimsFormat(fileName)) {
                inputLines = ImportWimsFormat.importToString(fileName);
				notation = ReactionNotation.detectNotation(inputLines.subList(0, 10));
				window.getDocument().setFileName(FileUtils.getFileWithNewUniqueName(FileUtils.replaceFileSuffix(fileName, ".crs")).getPath());
				window.getDocument().setDirty(true);
            } else {
				inputLines = FileUtils.getLinesFromFile(fileName);
				window.getDocument().setFileName(fileName);
				var lines = FileUtils.getFirstLinesFromFile(new File(fileName), 10);
                if (lines == null)
                    throw new IOException("Can't read file: " + fileName);
                notation = ReactionNotation.detectNotation(Arrays.asList(lines));
            }

            if (notation == null) {
                throw new IOException("Couldn't detect 'full', 'sparse' or 'tabbed' file format");
            }

			try (var r = new BufferedReader(new StringReader(StringUtils.toString(inputLines, "\n")))) {
				reactionSystem.clear();
				var leadingComments = ModelIO.read(window.getInputReactionSystem(), r, notation.getFirst());

				window.getController().getInputTextArea().setText((leadingComments.length() > 0 ? leadingComments + "\n" : "") + ModelIO.toString(window.getInputReactionSystem(), false, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation()));
				var food = ModelIO.getFoodString(window.getInputReactionSystem(), window.getDocument().getReactionNotation());

				window.getController().getInputFoodTextArea().setText(food);

				var infoString = "Read " + reactionSystem.size() + " reactions" + (reactionSystem.getNumberOfTwoWayReactions() > 0 ? "(" + reactionSystem.getNumberOfTwoWayReactions() + " two-way)" : "")
								 + " and " + reactionSystem.getFoods().size() + " food items from file: " + fileName;

                NotificationManager.showInformation(infoString);

                window.getLogStream().println(infoString);
                // window.getLogStream().println("Input format:   " + pair.getFirst());
                // window.getLogStream().println("Display format: " + window.getDocument().getReactionNotation());
                RecentFilesManager.getInstance().insertRecentFile(fileName);

                VerifyInput.verify(window);
            }

        } catch (Exception e) {
            if (false) { // here we need to drop the text into a window and highlight the error
                try {
					var inputLines = FileUtils.getLinesFromFile(fileName);
					window.getController().getInputTextArea().setText(StringUtils.toString(inputLines, "\n"));
				} catch (IOException ignored) {
                }
            }
            NotificationManager.showError("Open file '" + fileName + "' failed: " + e.getMessage());
        }
    }
}
