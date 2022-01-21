/*
 * ImportWimsFormat.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.action;

import catlynet.io.FileOpener;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * imports data in Wim's format
 * Daniel Huson, 2.2020
 */
public class ImportWimsFormat {

    public static void apply(Stage stage) {
        File previousDir = new File(ProgramProperties.get("ImportDir", ""));

        final FileChooser fileChooser = new FileChooser();
        if (previousDir.isDirectory())
            fileChooser.setInitialDirectory(previousDir);
        fileChooser.setTitle("Import File - " + ProgramProperties.getProgramVersion());
        fileChooser.getExtensionFilters().add(TextFileFilter.getInstance());
        final File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            ProgramProperties.put("ImportDir", selectedFile.getParent());
            (new FileOpener()).accept(selectedFile.getPath());
            RecentFilesManager.getInstance().insertRecentFile(selectedFile.getPath());
        }
    }

    /**
     * does this file require importing?
     *
     * @param fileName
     * @return true, if in Wim's format
     */
    public static boolean isInWimsFormat(String fileName) {
		final String line = FileUtils.getFirstLineFromFile(new File(fileName));
        return line != null && line.startsWith("<meta-data>");
    }

    /**
     * import into CRS format and return as lines
     *
     * @param fileName
     * @return name of new file
     * @throws IOException
     */
    public static ArrayList<String> importToString(String fileName) throws IOException {
        final ArrayList<String> food = new ArrayList<>();
        final Set<String> foodSet = new HashSet<>();
        final ArrayList<String> reactions = new ArrayList<>();
        final Set<String> reactionsSet = new HashSet<>();

        try (FileLineIterator it = new FileLineIterator(fileName)) {
            String part = "";
            int nrMolecules = -1;
            int nrFoodSet = -1;
            int nrReactions = -1;

            int moleculesFound = 0;

            long lineNrMolecules = 0;
            long lineNrFoodSet = 0;
            long lineReactions = 0;

            while (it.hasNext()) {
                final String line = it.next().trim();
                if (line.length() > 0) {
                    if (line.startsWith("<") && line.endsWith(">"))
                        part = line;
                    else {
                        switch (part) {
                            case "<meta-data>": {
								switch (StringUtils.getFirstWord(line)) {
									case "nrMolecules":
										nrMolecules = NumberUtils.parseInt(StringUtils.getLastWord(line));
                                        lineNrMolecules = it.getLineNumber();
										break;
									case "nrFoodSet":
                                        nrFoodSet = NumberUtils.parseInt(StringUtils.getLastWord(line));
                                        lineNrFoodSet = it.getLineNumber();
										break;
									case "nrReactions":
                                        nrReactions = NumberUtils.parseInt(StringUtils.getLastWord(line));
                                        lineReactions = it.getLineNumber();
                                        break;
                                }
                                break;
                            }
                            case "<molecules>": {
                                moleculesFound++;
                                // skip all molecules
                                break;
                            }
                            case "<food set>": {
                                if (foodSet.contains(line))
                                    throw new IOExceptionWithLineNumber(it.getLineNumber(), "Wim's format: <food> contains duplicate item: " + line);
                                else
                                    foodSet.add(line);
								food.add(StringUtils.getLastWord(line));
                                break;
                            }
                            case "<reactions>": {
                                if (reactionsSet.contains(line))
                                    throw new IOExceptionWithLineNumber(it.getLineNumber(), "Wim's format: <reactions> contains duplicate item: " + line);
                                else
                                    reactionsSet.add(line);
                                reactions.add(line.replaceAll("\\t[0-9.]*$", ""));
                            }
                        }
                    }
                }
            }
            if (nrMolecules > 0 && nrMolecules != moleculesFound) {
                throw new IOExceptionWithLineNumber(lineNrMolecules, String.format("Wim's format: Expected nrMolecules=%d molecules, found %d", nrMolecules, moleculesFound));
            }
            if (nrFoodSet > 0 && nrFoodSet != food.size()) {
                throw new IOExceptionWithLineNumber(lineNrFoodSet, String.format("Wim's format: Expected nrFoodSet=%d food items, found %d", nrFoodSet, food.size()));
            }
            if (nrReactions > 0 && nrReactions != reactions.size()) {
                System.err.println("first reaction: " + reactions.get(0));
                System.err.println("last reaction:  " + reactions.get(reactions.size() - 1));
                throw new IOExceptionWithLineNumber(lineReactions, String.format("Wim's format: Expected nrReactions=%d reactions, found %d", nrReactions, reactions.size()));
            }
        }
        final ArrayList<String> output = new ArrayList<>();
        output.add("# Imported from file: " + fileName);
        output.add("# Food: " + food.size());
        output.add("# Reactions: " + reactions.size());
        output.add("");
        output.addAll(reactions);
		output.add("");
		output.add("F: " + StringUtils.toString(food, ", "));
		output.add("# EOF");

        return output;
    }
}
