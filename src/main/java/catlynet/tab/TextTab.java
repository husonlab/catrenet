/*
 * TextTab.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.dialog.ExportTextFileDialog;
import catlynet.window.MainWindow;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import jloda.fx.find.ISearcher;
import jloda.fx.find.TextAreaSearcher;
import jloda.fx.util.RunAfterAWhile;
import jloda.util.StringUtils;

/**
 * text tab
 * Daniel Huson, 4.2020
 */
public class TextTab extends Tab {
    private final MainWindow mainWindow;

    private final String name;
    final private TextArea textArea;

    private final ISearcher searcher;

    /**
     * create a text tab
     *
	 */
    public TextTab(MainWindow mainWindow, String name) {
        this.mainWindow = mainWindow;
        this.name = name;

        setText(name);
        setId(name);
        textArea = new TextArea();
        setContent(textArea);
        textArea.wrapTextProperty().bindBidirectional(mainWindow.getController().getWrapTextMenuItem().selectedProperty());
        setClosable(true);

        disableProperty().bind(textArea.textProperty().isEmpty());
        searcher = new TextAreaSearcher(name, getTextArea());

        textArea.textProperty().addListener(e -> {
            RunAfterAWhile.applyInFXThread(textArea, () -> textArea.positionCaret(textArea.getText().length()));
        });
    }

    public void copyToClipboard() {
        var content = new ClipboardContent();
        content.putString(textArea.getSelectedText().isEmpty() ? textArea.getText() : textArea.getSelectedText());
        Clipboard.getSystemClipboard().setContent(content);
    }

    public void exportToFile() {
        ExportTextFileDialog.apply(mainWindow, StringUtils.toCamelCase(name), textArea.getText());
    }

    public String getName() {
        return name;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextTab)) return false;
        return ((TextTab) o).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public ISearcher getSearcher() {
        return searcher;
    }
}
