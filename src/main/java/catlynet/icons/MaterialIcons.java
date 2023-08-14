/*
 * MaterialIcons.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.icons;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MaterialIcons {
	private static MaterialIcons instance;

	public static MaterialIcons getInstance() {
		if (instance == null) {
			instance = new MaterialIcons();
		}
		return instance;
	}

	private final Map<String, String> nameCodePointMap = new TreeMap<>();
	private final String styleSheet;

	private MaterialIcons() {
		try (var ins = Objects.requireNonNull(MaterialIcons.class.getResource("MaterialIconsOutlined-Regular.otf")).openStream()) {
			Font.loadFont(ins, 10);
			/*
			for(int i=0;i<13278;i++) {
				all.add(new Pair<>(String.parsePositiveDoubles(i),graphic(String.parsePositiveDoubles((char)i),null)));
			}
			*/
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		try (var r = new BufferedReader(new InputStreamReader(Objects.requireNonNull(MaterialIcons.class.getResource("MaterialIconsOutlined-Regular.codepoints")).openStream()))) {
			while (r.ready()) {
				var tokens = r.readLine().split("\\s+");
				if (tokens.length == 2) {
					var materialIconName = tokens[0].trim();
					var codePoint = String.valueOf((char) Integer.parseInt(tokens[1].trim(), 16));
					nameCodePointMap.put(materialIconName, codePoint);
				}
			}
			/*
			for(int i=0;i<13278;i++) {
				all.add(new Pair<>(String.parsePositiveDoubles(i),graphic(String.parsePositiveDoubles((char)i),null)));
			}
			*/
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		styleSheet = Objects.requireNonNull(getClass().getResource("button.css")).toExternalForm();
	}

	public static Node graphic(String materialIconName) {
		return graphic(materialIconName, null);
	}

	public static Node graphic(String materialIconName, String style) {
		var nameCodePointMap = getInstance().getNameCodePointMap();
		var ch = nameCodePointMap.getOrDefault(materialIconName, nameCodePointMap.get("question_mark"));
		var label = new Label(ch);
		label.getStyleClass().add("icon-text");
		label.setAlignment(Pos.CENTER);
		label.setStyle("-fx-font-family: 'Material Icons Outlined'; -fx-font-size: 18; -fx-background-color: transparent;" + (style != null ? style : ""));
		return label;
	}

	public static void setIcon(Labeled labeled, String materialIconName) {
		setIcon(labeled, materialIconName, null, true);
	}


	public static void setIcon(Labeled labeled, String materialIconName, String style, boolean graphicOnly) {
		labeled.getStylesheets().add(getInstance().styleSheet);
		labeled.setGraphic(graphic(materialIconName, style));
		if (graphicOnly)
			labeled.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	public Map<String, String> getNameCodePointMap() {
		return nameCodePointMap;
	}

	public String getStyleSheet() {
		return styleSheet;
	}
}
