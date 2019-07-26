/*
 * Document.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.format.ArrowNotation;
import catlynet.format.ReactionNotation;
import catlynet.model.Model;
import catlynet.view.ReactionGraphView;
import javafx.beans.property.*;
import jloda.util.ProgramProperties;

public class Document {
    private final StringProperty fileName = new SimpleStringProperty("Untitled");
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private final ObjectProperty<ReactionNotation> reactionNotation = new SimpleObjectProperty<>(ReactionNotation.valueOfIgnoreCase(ProgramProperties.get("ReactionNotation", "Sparse")));
    private final ObjectProperty<ArrowNotation> arrowNotation = new SimpleObjectProperty<>(ArrowNotation.valueOfLabel(ProgramProperties.get("ArrowNotation", "=>")));

    private final Model inputModel = new Model();

    private final Model maxCAF = new Model();
    private final Model maxRAF = new Model();
    private final Model maxPseudoRAF = new Model();


    private final ReactionGraphView reactionGraphView = new ReactionGraphView(inputModel);


    public Document() {
    }

    public Model getInputModel() {
        return inputModel;
    }

    public Model getMaxCAF() {
        return maxCAF;
    }

    public Model getMaxRAF() {
        return maxRAF;
    }

    public Model getMaxPseudoRAF() {
        return maxPseudoRAF;
    }

    public ReactionGraphView getReactionGraphView() {
        return reactionGraphView;
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


}
