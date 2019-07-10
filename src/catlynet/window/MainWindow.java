/*
 * MainWindow.java Copyright (C) 2019. Daniel H. Huson
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

package catlynet.window;

import catlynet.io.CRSFileFilter;
import catlynet.io.FileOpener;
import catlynet.model.Model;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.MemoryUsage;
import jloda.fx.util.PrintStreamToTextArea;
import jloda.fx.util.TextFileFilter;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.util.Basic;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;

import java.io.PrintStream;
import java.util.Arrays;

public class MainWindow implements IMainWindow {
    private Stage stage;
    private final MainWindowController controller;
    private final Parent root;
    private final FlowPane statusPane;

    private final PrintStream logStream;

    private final UndoManager undoManager = new UndoManager();

    private final Document document = new Document();
    private final Model model = new Model();

    public MainWindow() {
        Platform.setImplicitExit(false);

        {
            final ExtendedFXMLLoader<MainWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            root = extendedFXMLLoader.getRoot();
            controller = extendedFXMLLoader.getController();

            logStream = new PrintStreamToTextArea(controller.getLogTextArea());

            statusPane = controller.getStatusFlowPane();
        }

        FileOpenManager.setExtensions(Arrays.asList(CRSFileFilter.getInstance(), TextFileFilter.getInstance()));
        FileOpenManager.setFileOpener(new FileOpener());
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public IMainWindow createNew() {
        return new MainWindow();
    }

    @Override
    public void show(Stage stage, double screenX, double screenY, double width, double height) {
        if (stage == null)
            stage = new Stage();
        this.stage = stage;
        stage.getIcons().addAll(ProgramProperties.getProgramIconsFX());

        final Scene scene = new Scene(root, width, height);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.setX(screenX);
        stage.setY(screenY);

        document.dirtyProperty().bind(controller.getInputTextArea().undoableProperty());

        final InvalidationListener listener = ((e) -> {
            if (document.getFileName() == null)
                getStage().setTitle("Untitled - " + ProgramProperties.getProgramName());
            else
                getStage().setTitle(Basic.getFileNameWithoutPath(document.getFileName()) + (document.isDirty() ? "*" : "") + " - " + ProgramProperties.getProgramName());
        });
        document.fileNameProperty().addListener(listener);

        document.dirtyProperty().addListener(listener);

        getStage().titleProperty().addListener((e) -> MainWindowManager.getInstance().fireChanged());

        ControlBindings.setup(this);

        final MemoryUsage memoryUsage = MemoryUsage.getInstance();
        controller.getMemoryUsageLabel().textProperty().bind(memoryUsage.memoryUsageStringProperty());

        stage.show();
    }

    @Override
    public boolean isEmpty() {
        return (getController().getFoodSetComboBox().getSelectionModel().getSelectedItem() == null
                || getController().getFoodSetComboBox().getSelectionModel().getSelectedItem().trim().length() == 0)
                && getController().getInputTextArea().getText().trim().length() == 0;
    }

    @Override
    public void close() {
        stage.hide();
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public MainWindowController getController() {
        return controller;
    }

    public FlowPane getStatusPane() {
        return statusPane;
    }

    public Document getDocument() {
        return document;
    }

    public Model getModel() {
        return model;
    }

    public PrintStream getLogStream() {
        return logStream;
    }
}
