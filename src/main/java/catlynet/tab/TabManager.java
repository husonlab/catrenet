/*
 * TabManager.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.tab;

import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * manage all reaction system tabs
 * Daniel Huson, 4.2020
 */
public class TabManager {
	private final MainWindow mainWindow;
    private final ObservableList<Tab> tabs;

	public TabManager(MainWindow mainWindow, ObservableList<Tab> tabs) {
		this.mainWindow = mainWindow;
        this.tabs = tabs;
    }

    public void clearAll() {
        tabs.stream().filter(t -> t instanceof TextTab).toList().forEach(tabs::remove);
    }

    public void clear(String reactionSystemName) {
        final Optional<Tab> tab = tabs.stream().filter(t -> t instanceof TextTab textTab && textTab.getName().equals(reactionSystemName)).findAny();
        tab.ifPresent(tabs::remove);
    }

    public TextTab getTextTab(String name, ReactionSystem reactionSystem) {
        final Optional<TextTab> tab = tabs.stream().filter(t -> t instanceof TextTab).map(t -> (TextTab) t).filter(t -> t.getName().equals(name)).findAny();
        tab.ifPresent(tabs::remove);
        { // create new tab and add in alphabetical order
            final TextTab textTab = reactionSystem != null ? new ReactionSystemTab(mainWindow, reactionSystem) : new TextTab(mainWindow, name);
            boolean added = false;
            for (int i = 0; !added && i < tabs.size(); i++) {
                final Tab current = tabs.get(i);
                if (current instanceof TextTab currentTextTab && textTab.getName().compareTo(currentTextTab.getName()) < 0) {
                    tabs.add(i, textTab);
                    added = true;
                }
            }
            if (!added)
                tabs.add(textTab);
            return textTab;
        }
    }

    public List<TextTab> textTabs() {
        return tabs.stream().filter(t -> t instanceof TextTab).map(t -> (TextTab) t).collect(Collectors.toList());
    }
}
