/*
 * ReactionGraphSearcher.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.view;

import javafx.beans.property.*;
import javafx.scene.control.MultipleSelectionModel;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.find.IObjectSearcher;
import jloda.graph.Graph;
import jloda.graph.Node;

/**
 * performs search on the reaction graph
 * Daniel Huson, 2.2020
 */
public class ReactionGraphSearcher implements IObjectSearcher<Node> {
    private final ZoomableScrollPane scrollPane;
    private final ReactionGraphView graphView;
    private final ItemSelectionModel<Node> nodeSelection;
    private final Graph graph;
    private Node which;
    private final ObjectProperty<Node> found = new SimpleObjectProperty<>();

    public ReactionGraphSearcher(ZoomableScrollPane scrollPane, ReactionGraphView graphView) {
        this.scrollPane = scrollPane;
        this.graphView = graphView;
        nodeSelection = graphView.getNodeSelection();
        this.graph = graphView.getReactionGraph();
    }

    @Override
    public boolean gotoFirst() {
        which = graph.getFirstNode();
        return which != null;
    }

    @Override
    public boolean gotoNext() {
        if (which != null)
            which = graph.getNextNode(which);
        return which != null;
    }

    @Override
    public boolean gotoLast() {
        which = graph.getLastNode();
        return which != null;
    }

    @Override
    public boolean gotoPrevious() {
        if (which != null)
            which = graph.getPrevNode(which);
        return which != null;
    }

    @Override
    public boolean isCurrentSet() {
        return which != null;
    }

    @Override
    public boolean isCurrentSelected() {
        return which != null && nodeSelection.isSelected(which);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        if (which != null) {
            if (select) {
                nodeSelection.select(which);
            } else {
                nodeSelection.clearSelection(which);
            }
        }
    }

    @Override
    public String getCurrentLabel() {
        if (which != null)
            return graphView.getNode2shapeAndLabel().get(which).getSecond().getText();
        else
            return null;
    }

    @Override
    public void setCurrentLabel(String newLabel) {

    }

    @Override
    public int numberOfObjects() {
        return graph.getNumberOfNodes();
    }

    @Override
    public ReadOnlyObjectProperty<Node> foundProperty() {
        return found;
    }

    @Override
    public MultipleSelectionModel<Node> getSelectionModel() {
        return null;
    }

    @Override
    public String getName() {
        return "Graph find";
    }

    @Override
    public ReadOnlyBooleanProperty isGlobalFindable() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public ReadOnlyBooleanProperty isSelectionFindable() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    public void updateView() {
        if (which != null) {
            scrollPane.ensureVisible(graphView.getNode2shapeAndLabel().get(which).getFirst());
        }
    }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public void selectAll(boolean select) {
        if (select)
            nodeSelection.getSelectedItems().addAll(graph.getNodesAsSet());
        else
            nodeSelection.clearSelection();
    }
}
