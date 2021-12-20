/*
 *  Copyright (C) 2018. Daniel H. Huson
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

/*
 *  CreateEdgesRectangular.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.view.trees.layout;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.graph.Edge;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * draws edges in rectangular layout
 * Daniel Huson, 12.2021
 */
public class CreateEdgesRectangular {

	public static Collection<Shape> apply(ComputeTreeLayout.Diagram diagram, PhyloTree tree, NodeArray<Shape> nodeShapeMap, Color color, boolean linkNodesEdgesLabels, BiConsumer<Edge, Shape> edgeCallback) {
		var shapes = new ArrayList<Shape>();

		for (var e : tree.edges()) {
			var sourceShape = nodeShapeMap.get(e.getSource());
			var targetShape = nodeShapeMap.get(e.getTarget());
			var line = new Path();
			line.setFill(Color.TRANSPARENT);
			line.setStroke(color);
			line.setStrokeLineCap(StrokeLineCap.ROUND);
			line.setStrokeWidth(1);
			line.setPickOnBounds(false);

			var moveTo = new MoveTo();
			if (linkNodesEdgesLabels) {
				moveTo.xProperty().bind(sourceShape.translateXProperty());
				moveTo.yProperty().bind(sourceShape.translateYProperty());
			} else {
				moveTo.setX(sourceShape.getTranslateX());
				moveTo.setY(sourceShape.getTranslateY());
			}
			line.getElements().add(moveTo);

			if (!tree.isReticulatedEdge(e) && !tree.isTransferEdge(e)) {
				var lineTo1 = new LineTo();
				line.getElements().add(lineTo1);
				if (linkNodesEdgesLabels) {
					lineTo1.xProperty().bind(sourceShape.translateXProperty());
					lineTo1.yProperty().bind(targetShape.translateYProperty());
				} else {
					var dx = targetShape.getTranslateX() - sourceShape.getTranslateX();
					var dy = targetShape.getTranslateY() - sourceShape.getTranslateY();
					if (Math.abs(dx) <= 8 || Math.abs(dy) <= 8) {
						lineTo1.setX(sourceShape.getTranslateX());
						lineTo1.setY(targetShape.getTranslateY());
					} else {
						lineTo1.setX(sourceShape.getTranslateX());
						lineTo1.setY(sourceShape.getTranslateY() + dy + (dy > 0 ? -4 : 4));

						var quadTo = new QuadCurveTo();
						line.getElements().add(quadTo);
						quadTo.setControlX(sourceShape.getTranslateX());
						quadTo.setControlY(targetShape.getTranslateY());
						quadTo.setX(sourceShape.getTranslateX() + (dx > 0 ? +4 : -4));
						quadTo.setY(targetShape.getTranslateY());
					}
				}

				var lineTo2 = new LineTo();
				line.getElements().add(lineTo2);

				if (linkNodesEdgesLabels) {
					lineTo2.xProperty().bind(targetShape.translateXProperty());
					lineTo2.yProperty().bind(targetShape.translateYProperty());
				} else {
					lineTo2.setX(targetShape.getTranslateX());
					lineTo2.setY(targetShape.getTranslateY());
				}
			} else { // special edge
				line.setStroke(Color.DARKORANGE);

				var quadCurveTo = new QuadCurveTo();
				line.getElements().add(quadCurveTo);
				if (linkNodesEdgesLabels) {
					quadCurveTo.controlXProperty().bind(sourceShape.translateXProperty());
					quadCurveTo.controlYProperty().bind(targetShape.translateYProperty());
					quadCurveTo.xProperty().bind(targetShape.translateXProperty());
					quadCurveTo.yProperty().bind(targetShape.translateYProperty());
				} else {
					quadCurveTo.setControlX(sourceShape.getTranslateX());
					quadCurveTo.setControlY(targetShape.getTranslateY());
					quadCurveTo.setX(targetShape.getTranslateX());
					quadCurveTo.setY(targetShape.getTranslateY());
				}

			}
			shapes.add(line);
			edgeCallback.accept(e, line);
		}

		if (false)
			for (var v : tree.nodes()) {
				for (var w : tree.lsaChildren(v)) {
					if (w.inEdgesStream(false).anyMatch(tree::isReticulatedEdge)) {
						var sourceShape = nodeShapeMap.get(v);
						var targetShape = nodeShapeMap.get(w);

						var line = new Line();
						line.setStartX(sourceShape.getTranslateX());
						line.setStartY(sourceShape.getTranslateY());
						line.setEndX(targetShape.getTranslateX());
						line.setEndY(targetShape.getTranslateY());
						line.setStroke(Color.DEEPPINK.deriveColor(1, 1, 1, 0.5));
						shapes.add(line);
					}
				}
			}
		return shapes;
	}
}
