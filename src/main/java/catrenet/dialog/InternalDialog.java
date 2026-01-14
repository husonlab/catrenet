/*
 * InternalDialog.java Copyright (C) 2026 Daniel H. Huson
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
 *
 */

package catrenet.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class InternalDialog extends StackPane {
	private final AnchorPane parent;
	private final Region glassPane;

	public InternalDialog(Parent dialogContent, AnchorPane parent, boolean center) {
		this(dialogContent, parent);
		if (!center) {
			StackPane.setAlignment(dialogContent, Pos.TOP_CENTER);
		}
	}

	public InternalDialog(Parent dialogContent, AnchorPane parent) {
		this.parent = parent;

		// Semi-transparent background to block interaction
		glassPane = new Region();
		glassPane.setBackground(new Background(
				new BackgroundFill(
						Color.rgb(0, 0, 0, 0.35),
						CornerRadii.EMPTY,
						Insets.EMPTY
				)
		));
		glassPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Consume all input events (modal behavior)
		glassPane.addEventFilter(MouseEvent.ANY, MouseEvent::consume);

		// Dialog container
		VBox dialogBox = new VBox(dialogContent);
		dialogBox.getStyleClass().add("internal-dialog");
		dialogBox.setMaxWidth(400);
		dialogBox.setMaxHeight(Region.USE_PREF_SIZE);

		setAlignment(dialogBox, Pos.CENTER);

		getChildren().addAll(glassPane, dialogBox);

		// Ensure we resize with parent
		setPickOnBounds(true);
	}

	public void show() {
		if (!parent.getChildren().contains(this)) {
			parent.getChildren().add(this);

			AnchorPane.setTopAnchor(this, 0.0);
			AnchorPane.setBottomAnchor(this, 0.0);
			AnchorPane.setLeftAnchor(this, 0.0);
			AnchorPane.setRightAnchor(this, 0.0);
		}
	}

	public void close() {
		parent.getChildren().remove(this);
	}
}