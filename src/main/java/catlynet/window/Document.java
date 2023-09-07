/*
 * Document.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.window;

import catlynet.model.ReactionSystem;
import catlynet.settings.ArrowNotation;
import catlynet.settings.ReactionNotation;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import jloda.fx.util.ProgramProperties;
import jloda.graph.Graph;

public class Document {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final BooleanProperty warnedAboutInhibitions = new SimpleBooleanProperty(false);

    private final ObservableMap<String, ReactionSystem> reactionSystems = FXCollections.observableHashMap();
    private final ObjectProperty<Graph> reactionDependencyNetwork = new SimpleObjectProperty<>(this, "reactionDependencyNetwork");
    private final ObjectProperty<Graph> moleculeDependencyNetwork = new SimpleObjectProperty<>(this, "moleculeDependencyNetwork");

    private final ObjectProperty<ReactionNotation> reactionNotation = new SimpleObjectProperty<>(ReactionNotation.valueOfIgnoreCase(ProgramProperties.get("ReactionNotation", "Sparse")));
    private final ObjectProperty<ArrowNotation> arrowNotation = new SimpleObjectProperty<>(ArrowNotation.valueOfLabel(ProgramProperties.get("ArrowNotation", "=>")));

    /**
     * constructor
     */
    public Document() {
    }

    public ReactionSystem getInputReactionSystem() {
        return getReactionSystem("Input");
    }

    public ReactionSystem getReactionSystem(String name) {
        ReactionSystem inputReactionSystem = reactionSystems.get(name);
        if (inputReactionSystem == null) {
            inputReactionSystem = new ReactionSystem(name);
            reactionSystems.put(name, inputReactionSystem);
        }
        return inputReactionSystem;
    }

    public ObservableMap<String, ReactionSystem> getReactionSystems() {
        return reactionSystems;
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public ReactionNotation getReactionNotation() {
        return reactionNotation.get();
    }

    public ObjectProperty<ReactionNotation> reactionNotationProperty() {
        return reactionNotation;
    }

    public void setReactionNotation(ReactionNotation reactionNotation) {
        this.reactionNotation.set(reactionNotation);
    }

    public ArrowNotation getArrowNotation() {
        return arrowNotation.get();
    }

    public ObjectProperty<ArrowNotation> arrowNotationProperty() {
        return arrowNotation;
    }

    public void setArrowNotation(ArrowNotation arrowNotation) {
        this.arrowNotation.set(arrowNotation);
    }

    public boolean isWarnedAboutInhibitions() {
        return warnedAboutInhibitions.get();
    }

    public BooleanProperty warnedAboutInhibitionsProperty() {
        return warnedAboutInhibitions;
    }

    public void setWarnedAboutInhibitions(boolean warnedAboutInhibitions) {
        this.warnedAboutInhibitions.set(warnedAboutInhibitions);
    }

    public Graph getReactionDependencyNetwork() {
        return reactionDependencyNetwork.get();
    }

    public ObjectProperty<Graph> reactionDependencyNetworkProperty() {
        return reactionDependencyNetwork;
    }

    public void setReactionDependencyNetwork(Graph reactionDependencyNetwork) {
        this.reactionDependencyNetwork.set(reactionDependencyNetwork);
    }

    public Graph getMoleculeDependencyNetwork() {
        return moleculeDependencyNetwork.get();
    }

    public ObjectProperty<Graph> moleculeDependencyNetworkProperty() {
        return moleculeDependencyNetwork;
    }

    public void setMoleculeDependencyNetwork(Graph moleculeDependencyNetwork) {
        this.moleculeDependencyNetwork.set(moleculeDependencyNetwork);
    }
}
