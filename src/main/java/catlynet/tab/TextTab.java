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

import catlynet.window.MainWindow;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import jloda.fx.find.FindToolBar;
import jloda.fx.util.ExtendedFXMLLoader;

/**
 * text tab, with v box and text area
 * Daniel Huson, 4.2020
 */
public class TextTab {
    final private String reactionSystemName;
    final private Tab tab;
    final private VBox vBox;
    final private TextArea textArea;
    private FindToolBar findToolBar;

    private final TextTabController controller;

    /**
     * create a text tab
     *
	 */
    public TextTab(MainWindow mainWindow, String reactionSystemName) {
        this.reactionSystemName = reactionSystemName;
        final ExtendedFXMLLoader<TextTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(TextTab.class);
        controller = extendedFXMLLoader.getController();

        tab = controller.getTab();
        tab.setText(reactionSystemName);
        tab.setId(reactionSystemName);
        vBox = controller.getVbox();
        textArea = controller.getTextArea();
        tab.setUserData(this);
        tab.setClosable(true);

        tab.disableProperty().bind(textArea.textProperty().isEmpty());

        var exportItem = new SimpleObjectProperty<MenuItem>();
        InvalidationListener invalidationListener = e -> {
            exportItem.set(null);
            for (var item : mainWindow.getController().getExportMenu().getItems()) {
                if (item.getText() != null && item.getText().equals(reactionSystemName + "...")) {
                    exportItem.set(item);
                    break;
                }
            }
        };
        mainWindow.getController().getExportMenu().getItems().addListener(new WeakInvalidationListener(invalidationListener));
        invalidationListener.invalidated(null);

        controller.getExportMenuItem().setOnAction(e -> exportItem.get().getOnAction().handle(e));
        controller.getExportMenuItem().disableProperty().bind(exportItem.isNull());

        tab.setOnClosed(e -> {
            if (exportItem.get() != null)
                mainWindow.getController().getExportMenu().getItems().remove(exportItem.get());
        });
    }

    public String getReactionSystemName() {
        return reactionSystemName;
    }

    public Tab getTab() {
        return tab;
    }

    public VBox getvBox() {
        return vBox;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public FindToolBar getFindToolBar() {
        return findToolBar;
    }

    public void setFindToolBar(FindToolBar findToolBar) {
        if (findToolBar != null)
            vBox.getChildren().add(findToolBar);
        this.findToolBar = findToolBar;
    }

    public TextTabController getController() {
        return controller;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextTab)) return false;
        return ((TextTab) o).reactionSystemName.equals(reactionSystemName);
    }

    @Override
    public int hashCode() {
        return reactionSystemName.hashCode();
    }
}
