/*
 * MainWindow.java Copyright (C) 2024 Daniel H. Huson
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

package bioraf.window;

import bioraf.io.CRSFileFilter;
import bioraf.io.FileOpener;
import bioraf.main.BioRAF;
import bioraf.model.ReactionSystem;
import bioraf.tab.TabManager;
import bioraf.view.ReactionGraphView;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.*;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.util.FileUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Objects;

/**
 * the main window
 * Daniel Huson, 7.2019
 */
public class MainWindow implements IMainWindow {
    private Stage stage;
    private final MainWindowController controller;

    private MainWindowPresenter presenter;
    private final Parent root;
    private final FlowPane statusPane;

    private final ReactionGraphView reactionGraphView;

    private final PrintStream logStream;

    private final UndoManager undoManager = new UndoManager();

    private final TabManager tabManager;

    private final Document document = new Document();

    private final BooleanProperty hasFoodInput = new SimpleBooleanProperty(this, "hasFoodInput", false);
    private final BooleanProperty hasReactionsInput = new SimpleBooleanProperty(this, "hasReactionsInput", false);
    private final BooleanProperty empty = new SimpleBooleanProperty(this, "empty", true);

    private final StringProperty name = new SimpleStringProperty(this, "name", "Untitled");

    /**
     * constructor
     */
    public MainWindow() {
        Platform.setImplicitExit(false);

        {
            var fxmlLoader = new FXMLLoader();
            try (var ins = StatementFilter.applyMobileFXML(Objects.requireNonNull(MainWindowController.class.getResource("MainWindow.fxml")).openStream(), BioRAF.isDesktop())) {
                fxmlLoader.load(ins);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            root = fxmlLoader.getRoot();
            controller = fxmlLoader.getController();

            logStream = new PrintStreamToTextArea(controller.getLogTextArea());

            statusPane = controller.getBottomFlowPane();

            reactionGraphView = new ReactionGraphView(getDocument(), controller, getLogStream());
        }

        tabManager = new TabManager(this, controller.getOutputTabPane().getTabs());

        FileOpenManager.setExtensions(Collections.singletonList(CRSFileFilter.getInstance()));
        FileOpenManager.setFileOpener(new FileOpener());

        final InvalidationListener listener = (e -> {
            name.set(document.getFileName() == null ? "Untitled" : FileUtils.getFileNameWithoutPathOrSuffix(document.getFileName()));
            if (getStage() != null)
                getStage().setTitle(getName() + (document.isDirty() ? "*" : "") + " - " + ProgramProperties.getProgramName());
        });
        document.fileNameProperty().addListener(listener);
        document.dirtyProperty().addListener(listener);
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

        final var scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("MainWindow.css")).toExternalForm());

        stage.setScene(scene);
        stage.setX(screenX);
        stage.setY(screenY);
        stage.setWidth(width);
        stage.setHeight(height);

        getStage().titleProperty().addListener((e) -> MainWindowManager.getInstance().fireChanged());

        presenter = new MainWindowPresenter(this);

        final MemoryUsage memoryUsage = MemoryUsage.getInstance();
        controller.getMemoryUsageLabel().textProperty().bind(memoryUsage.memoryUsageStringProperty());

        // if (BioRAF.isDesktop()) // todo: if we don't briefly show the stage in App mode, the program hangs
            stage.show();

        controller.getInputFoodTextArea().textProperty().length().addListener((c, o, n) -> hasFoodInput.set(n.intValue() > 0));

        controller.getInputTextArea().textProperty().length().addListener((c, o, n) -> hasReactionsInput.set(n.intValue() > 0));
        empty.bind(controller.getInputFoodTextArea().textProperty().isEmpty().and(controller.getInputTextArea().textProperty().isEmpty()));
    }

    @Override
    public boolean isEmpty() {
        return empty.get();
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

    public MainWindowPresenter getPresenter() {
        return presenter;
    }

    public FlowPane getStatusPane() {
        return statusPane;
    }

    public Document getDocument() {
        return document;
    }

    public ReactionSystem getInputReactionSystem() {
        return document.getInputReactionSystem();
    }

    public ReactionSystem getReactionSystem(String name) {
        return document.getReactionSystem(name);
    }


    public PrintStream getLogStream() {
        return logStream;
    }

    public ReactionGraphView getReactionGraphView() {
        return reactionGraphView;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }
}
