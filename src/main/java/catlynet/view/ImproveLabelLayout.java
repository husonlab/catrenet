/*
 * ImproveLabelLayout.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.view;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import javafx.application.Platform;
import javafx.scene.Node;
import jloda.fx.util.GeometryUtilsFX;
import jloda.util.Pair;

import java.util.stream.Collectors;

/**
 * improves label layout, trying to avoid any collisions with lines or other labels
 * Daniel Huson, 3.2023
 */
public class ImproveLabelLayout {
	public static void apply(ReactionGraphView graphView) {
		var pairs = graphView.getReactionGraph().nodeStream()
				.filter(v -> v.getInfo() instanceof Reaction || v.getInfo() instanceof MoleculeType)
				.map(v -> new Pair<>(v, graphView.getNode2view().get(v))).collect(Collectors.toList());

		for (var pair : pairs) {
			Platform.runLater(() -> {
				var v = pair.getFirst();
				var nv = pair.getSecond();
				var shape = nv.getShape();
				var label = nv.getLabel();
				label.applyCss();
				var dx = 0.5 * label.getWidth();
				var dy = 0.5 * label.getHeight();

				//label.setTextFill(Color.RED);
				label.setLayoutX(-dx);
				label.setLayoutY(-dy);

				var x = computeRadius(nv.getShape()) + dx + 4;
				var y = -dy;

				if (v.getDegree() == 1) {
					var other = graphView.getNode2view().get(v.getFirstAdjacentEdge().getOpposite(v)).getShape();
					var angle = GeometryUtilsFX.computeAngle(shape.getTranslateX() - other.getTranslateX(), shape.getTranslateY() - other.getTranslateY());
					var point = GeometryUtilsFX.rotate(x, y, angle);
					label.setLayoutX(point.getX() - dx);
					label.setLayoutY(point.getY() - dy);
				} else {

					var ok = false;

					for (var angle = 0; angle < 360; angle += 10) {
						var point = GeometryUtilsFX.rotate(x, y, angle);
						label.setLayoutX(point.getX() - dx);
						label.setLayoutY(point.getY() - dy);
						ok = true;
						var sceneBounds = label.localToScene(label.getBoundsInLocal());
						for (var e : v.adjacentEdges()) {
							var ev = graphView.getEdge2view().get(e);
							if (ev.intersectsInScene(sceneBounds)) {
								ok = false;
								break;
							}
						}
						if (pairs.size() < 100 && ok) {
							for (var other : pairs) {
								if (other != pair) {
									var otherLabel = other.getSecond().getLabel();
									var otherBounds = otherLabel.localToScene(otherLabel.getBoundsInLocal());
									if (sceneBounds.intersects(otherBounds)) {
										ok = false;
										break;
									}
								}
							}
						}
						if (ok) {
							//System.err.printf("%s: %d (%.1f, %.1f)%n", nv.getLabel().getText(),angle,label.getLayoutX(),label.getLayoutY());
							break;
						}
					}
					if (!ok) {
						label.setLayoutX(x);
						label.setLayoutY(y);
					}
				}
			});
		}
	}

	public static double computeRadius(Node node) {
		var bounds = node.getLayoutBounds();
		return 0.5 * Math.sqrt(bounds.getWidth() * bounds.getWidth() + bounds.getHeight() * bounds.getHeight());
	}
}
