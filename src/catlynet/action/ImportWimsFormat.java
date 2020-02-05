/*
 * ImportWimsFormat.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.action;

import catlynet.io.FileOpener;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TextFileFilter;
import jloda.util.Basic;
import jloda.util.FileLineIterator;
import jloda.util.ProgramProperties;

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
        final String line = Basic.getFirstLineFromFile(new File(fileName));
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
        final ArrayList<String> reactions = new ArrayList<>();

        try (FileLineIterator it = new FileLineIterator(fileName)) {
            String part = "";
            int nrMolecules = -1;
            int nrFoodSet = -1;
            int nrReactions = -1;

            int moleculesFound = 0;

            while (it.hasNext()) {
                final String line = it.next().trim();
                if (line.length() > 0) {
                    if (line.startsWith("<") && line.endsWith(">"))
                        part = line;
                    else {
                        switch (part) {
                            case "<meta-data>": {
                                switch (Basic.getFirstWord(line)) {
                                    case "nrMolecules":
                                        nrMolecules = Basic.parseInt(Basic.getLastWord(line));
                                        break;
                                    case "nrFoodSet":
                                        nrFoodSet = Basic.parseInt(Basic.getLastWord(line));
                                        break;
                                    case "nrReactions":
                                        nrReactions = Basic.parseInt(Basic.getLastWord(line));
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
                                food.add(Basic.getLastWord(line));
                                break;
                            }
                            case "<reactions>": {
                                reactions.add(line.replaceAll("\\t[0-9.]*$", ""));
                            }
                        }
                    }
                }
            }
            if (nrMolecules > 0 && nrMolecules != moleculesFound) {
                throw new IOException(String.format("Expected nrMolecules=%d molecules, found %d", nrMolecules, moleculesFound));
            }
            if (nrFoodSet > 0 && nrFoodSet != food.size()) {
                throw new IOException(String.format("Expected nrFoodSet=%d food items, found %d", nrFoodSet, food.size()));
            }
            if (nrReactions > 0 && nrReactions != reactions.size()) {
                System.err.println("first reaction: " + reactions.get(0));
                System.err.println("last reaction:  " + reactions.get(reactions.size() - 1));
                throw new IOException(String.format("Expected nrReactions=%d reactions, found %d", nrReactions, reactions.size()));
            }
            {
                final Set<String> foodSet = new HashSet<>(food);
                if (foodSet.size() < food.size()) {
                    throw new IOException("<food> contains duplicate items");
                }
                final Set<String> reactionSet = new HashSet<>(reactions);
                if (reactionSet.size() < reactions.size()) {
                    throw new IOException("<reactions> contains duplicate items");
                }
            }
        }
        final ArrayList<String> output = new ArrayList<>();
        output.add("# Imported from file: " + fileName);
        output.add("# Food: " + food.size());
        output.add("# Reactions: " + reactions.size());
        output.add("");
        output.addAll(reactions);
        output.add("");
        output.add("F: " + Basic.toString(food, ", "));
        output.add("# EOF");

        return output;
    }
}
