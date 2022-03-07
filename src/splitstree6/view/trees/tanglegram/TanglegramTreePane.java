/*
 * TanglegramTreePane.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree6.view.trees.tanglegram;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.stage.Stage;
import jloda.fx.selection.SelectionModel;
import jloda.phylo.PhyloTree;
import splitstree6.data.TaxaBlock;
import splitstree6.data.parts.Taxon;
import splitstree6.view.trees.layout.ComputeHeightAndAngles;
import splitstree6.view.trees.layout.TreeDiagramType;
import splitstree6.view.trees.layout.TreeLabel;
import splitstree6.view.trees.treepages.LayoutOrientation;
import splitstree6.view.trees.treepages.RunAfterAWhile;
import splitstree6.view.trees.treepages.TreePane;

/**
 * a tanglegram tree pane
 * Daniel Huson, 12.2021
 */
public class TanglegramTreePane extends Group {
	private final InvalidationListener updater;
	private Runnable runAfterUpdate;

	public TanglegramTreePane(Stage stage, TaxaBlock taxaBlock, SelectionModel<Taxon> taxonSelectionModel,
							  ObjectProperty<PhyloTree> tree, ObjectProperty<Dimension2D> dimensions,
							  ObjectProperty<TreeDiagramType> optionDiagram, ObjectProperty<ComputeHeightAndAngles.Averaging> optionAveraging, ObjectProperty<LayoutOrientation> optionOrientation,
							  ReadOnlyDoubleProperty fontScaleFactor,
							  ReadOnlyBooleanProperty showInternalLabels) {

		updater = e -> RunAfterAWhile.apply(this, () ->
				Platform.runLater(() -> {
					getChildren().clear();
					if (dimensions.get().getWidth() > 0 && dimensions.get().getHeight() > 0 && tree.get() != null) {
						var treePane = new TreePane(stage, taxaBlock, tree.get(), taxonSelectionModel, dimensions.get().getWidth(), dimensions.get().getHeight(),
								optionDiagram.get(), optionAveraging.get(), optionOrientation, fontScaleFactor, new SimpleObjectProperty<>(TreeLabel.None), showInternalLabels, null);
						treePane.setRunAfterUpdate(getRunAfterUpdate());
						treePane.drawTree();
						getChildren().add(treePane);
					}
				})
		);

		tree.addListener(new WeakInvalidationListener(updater));
		optionDiagram.addListener(new WeakInvalidationListener(updater));
		// optionOrientation.addListener(new WeakInvalidationListener(updater)); // treepane listens for changes of orientation
		dimensions.addListener(new WeakInvalidationListener(updater));
		optionAveraging.addListener(new WeakInvalidationListener(updater));
	}

	public Runnable getRunAfterUpdate() {
		return runAfterUpdate;
	}

	public void setRunAfterUpdate(Runnable runAfterUpdate) {
		this.runAfterUpdate = runAfterUpdate;
	}
}
