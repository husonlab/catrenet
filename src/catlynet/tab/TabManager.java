/*
 * TabManager.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.tab;

import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * manage all reaction system tabs
 * Daniel Huson, 4.2020
 */
public class TabManager {
    private final ObservableList<Tab> tabs;

    public TabManager(ObservableList<Tab> tabs) {
        this.tabs = tabs;
    }

    public void clearAll() {
        tabs.stream().filter(t -> t.getUserData() instanceof TextTab).collect(Collectors.toList()).forEach(tabs::remove);
    }

    public void clear(String reactionSystemName) {
        final Optional<Tab> tab = tabs.stream().filter(t -> t.getUserData() instanceof TextTab && ((TextTab) t.getUserData()).getReactionSystemName().equals(reactionSystemName)).findAny();
        tab.ifPresent(tabs::remove);
    }

    public TextTab getTextTab(String reactionSystemName) {
        final Optional<TextTab> tab = tabs.stream().filter(t -> t.getUserData() instanceof TextTab && ((TextTab) t.getUserData()).getReactionSystemName().equals(reactionSystemName)).map(t -> (TextTab) t.getUserData()).findAny();
        if (tab.isPresent())
            return tab.get();
        else { // create new tab and add in alphabetical order
            final TextTab textTab = new TextTab(reactionSystemName);
            boolean added = false;
            for (int i = 0; !added && i < tabs.size(); i++) {
                final Tab current = tabs.get(i);
                if (current.getUserData() instanceof TextTab && textTab.getReactionSystemName().compareTo(((TextTab) current.getUserData()).getReactionSystemName()) < 0) {
                    tabs.add(i, textTab.getTab());
                    added = true;
                }
            }
            if (!added)
                tabs.add(textTab.getTab());
            return textTab;
        }
    }

    public Tab getTab(String reactionSystemName) {
        return getTextTab(reactionSystemName).getTab();
    }

    public TextArea getTextArea(String reactionSystemName) {
        return getTextTab(reactionSystemName).getTextArea();
    }

    public List<TextTab> textTabs() {
        return tabs.stream().filter(t -> t.getUserData() instanceof TextTab).map(t -> (TextTab) t.getUserData()).collect(Collectors.toList());
    }
}
