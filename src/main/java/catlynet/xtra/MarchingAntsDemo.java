/*
 * MarchingAntsDemo.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.xtra;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import jloda.fx.util.CopyLineShape;
import jloda.fx.util.MarchingAnts;
import jloda.fx.util.MouseDragToTranslate;

/**
 * demonstrates marching ants
 * Daniel Huson, 8.2023
 */
public class MarchingAntsDemo extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		var color = Color.DARKORANGE;

		var line = new CubicCurve(100, 100, 100, 200, 100, 200, 200, 200);
		line.setStrokeWidth(8);
		line.setStroke(color.deriveColor(1, 1, 1, 0.6));
		line.setFill(Color.TRANSPARENT);

		var below = CopyLineShape.apply(line);
		below.setStroke(color.deriveColor(1, 1, 1, 0.4));
		below.setStrokeWidth(line.getStrokeWidth());
		below.setFill(Color.TRANSPARENT);

		var end = new Rectangle(10, 10);
		end.translateXProperty().bindBidirectional(line.endXProperty());
		end.translateYProperty().bindBidirectional(line.endYProperty());
		end.setFill(Color.GREEN);

		var start = new Button("Start");

		var speed = new SimpleDoubleProperty(0.0);

		MarchingAnts.apply(line, speed, 3.0, 20.0);

		start.setOnAction(e -> {
			if (speed.get() == 0.0)
				speed.set(0.4);
			else if (speed.get() == 0.4)
				speed.set(3.0);
			else if (speed.get() == 3.0)
				speed.set(10.0);
			else speed.set(0.0);
		});

		var slider = new Slider(0.0, 20.0, 0.0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(1.0);
		slider.setMinorTickCount(4);
		slider.setPrefWidth(800);
		speed.bindBidirectional(slider.valueProperty());

		var borderPane = new BorderPane();
		borderPane.setTop(new ToolBar(start, slider));
		borderPane.setCenter(new Pane(below, line, end));

		MouseDragToTranslate.setup(end);

		stage.setScene(new Scene(borderPane, 900, 800));
		stage.show();
	}

}
