/*
 *  TreePane.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.viewers.multitreesviewer;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import jloda.phylo.PhyloTree;
import splitstree6.data.TaxaBlock;

public class TreePane extends Pane {
	public enum Diagram {Unrooted, Circular, Rectangular, Triangular}

	public enum RootSide {Left, Right, Bottom, Top}


	private final Diagram diagram;
	private final RootSide rootSide;
	private final boolean toScale;
	private final ObjectProperty<Font> font;

	public TreePane(Diagram diagram, RootSide rootSide, boolean toScale, ObjectProperty<Font> font) {
		this.diagram = diagram;
		this.rootSide = rootSide;
		this.toScale = toScale;
		this.font = font;
		setStyle("-fx-border-color: lightgray;");
	}

	public void drawTree(TaxaBlock taxaBlock, PhyloTree phyloTree) {
		var group = RectangularOrTriangularTreeEmbedding.apply(taxaBlock, phyloTree, diagram, true, getPrefWidth(), getPrefHeight());

		getChildren().add(new Label(phyloTree.getName(), group));
	}
}
