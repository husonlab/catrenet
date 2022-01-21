/*
 * ShowHideNodeLabels.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.action;

import catlynet.view.ReactionGraphView;
import javafx.scene.control.Label;
import jloda.graph.Node;

/**
 * shows or hides all selected node labels
 * Daniel Huson, 1.2020
 */
public class ShowHideNodeLabels {

    public static void apply(ReactionGraphView reactionGraphView) {
        boolean show = false;
        final Iterable<Node> nodes;
        if (reactionGraphView.getNodeSelection().size() > 0)
            nodes = reactionGraphView.getNodeSelection().getSelectedItems();
        else
            nodes = reactionGraphView.getReactionGraph().nodes();

        for (Node v : nodes) {
            final Label label = reactionGraphView.getLabel(v);
            if (!label.isVisible()) {
                show = true;
                break;
            }
        }

        for (Node v : nodes) {
            final Label label = reactionGraphView.getLabel(v);
            if (!label.getText().equals("&"))
                label.setVisible(show);
        }
    }
}
