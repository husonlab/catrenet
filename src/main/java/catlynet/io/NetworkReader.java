/*
 * NetworkReader.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.io;

import catlynet.view.ReactionGraphView;
import catlynet.window.Document;
import jloda.fx.util.TriConsumer;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.io.GraphGML;

import java.io.IOException;
import java.io.Reader;

public class NetworkReader {
	public static void read(Reader r, Document document, ReactionGraphView graphView) throws IOException {

		var graph = graphView.getReactionGraph();
		graph.clear();
		TriConsumer<String, Node, String> labelNodeConsumer = (label, v, value) -> {
			switch (label) {
				case "shape" -> {
				}
				case "points" -> {
				}
				case "width" -> {
				}
				case "height" -> {
				}

				case "type" -> {
					switch (value) {

					}
				}
				case "x" -> {

				}
				case "y" -> {
				}
				case "fill" -> {
				}
				case "stroke" -> {
				}
				case "strokeWidth" -> {
				}
				case "lx" -> {
				}
				case "ly" -> {
				}
				case "label" -> {
				}
			}
			;
		};
		TriConsumer<String, Edge, String> labelEdgeConsumer = (label, e, valueString) -> {
		};

		var info = GraphGML.readGML(r, graph, labelNodeConsumer, labelEdgeConsumer);


	}
}
