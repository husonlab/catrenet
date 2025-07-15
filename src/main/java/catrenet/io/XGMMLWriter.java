/*
 * XGMMLWriter.java Copyright (C) 2025 Daniel H. Huson
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

package catrenet.io;

import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import catrenet.view.EdgeType;
import catrenet.view.ReactionGraphView;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * export in XGMML format
 * Daniel Huson, 7.2025
 */
public class XGMMLWriter {
	public static void write(ReactionGraphView graphView, String fileName) throws IOException, XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter(fileName));

		writer.writeStartDocument("1.0");
		writer.writeStartElement("graph");
		writer.writeAttribute("label", "CatReNet Export " + graphView.getGraphType().name());
		writer.writeAttribute("directed", "1");
		writer.writeDefaultNamespace("http://www.cs.rpi.edu/XGMML");

		var graph = graphView.getReactionGraph();

// Write nodes
		for (var n : graph.nodes()) {
			writer.writeStartElement("node");
			writer.writeAttribute("id", String.valueOf(n.getId()));

			if (n.getInfo() instanceof Reaction reaction) {
				writer.writeAttribute("label", reaction.getName());
				writer.writeStartElement("att");
				writer.writeAttribute("name", "type");
				writer.writeAttribute("value", "reaction");
				writer.writeEndElement(); // att
			} else if (n.getInfo() instanceof MoleculeType moleculeType) {
				writer.writeAttribute("label", moleculeType.getName());
				writer.writeStartElement("att");
				writer.writeAttribute("name", "type");
				writer.writeAttribute("value", "molecule");
				writer.writeEndElement(); // att
			} else if (n.getInfo() instanceof ReactionGraphView.AndNode) {
				writer.writeStartElement("att");
				writer.writeAttribute("name", "type");
				writer.writeAttribute("value", "AndNode");
				writer.writeEndElement(); // att
			}
			writer.writeEndElement(); // node
		}

// Write edges
		for (var e : graph.edges()) {
			writer.writeStartElement("edge");
			writer.writeAttribute("source", String.valueOf(e.getSource().getId()));
			writer.writeAttribute("target", String.valueOf(e.getTarget().getId()));
			writer.writeAttribute("label", ((EdgeType) e.getInfo()).name());

			writer.writeStartElement("att");
			writer.writeAttribute("name", "type");
			writer.writeAttribute("value", ((EdgeType) e.getInfo()).name());
			writer.writeEndElement(); // att

			writer.writeEndElement(); // edge
		}

		writer.writeEndElement(); // graph
		writer.writeEndDocument();
		writer.close();
	}
}
