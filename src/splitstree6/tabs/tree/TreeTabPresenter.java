/*
 *  TreeTabPresenter.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.tabs.tree;

import splitstree6.tabs.IDisplayTabPresenter;
import splitstree6.window.MainWindow;

public class TreeTabPresenter implements IDisplayTabPresenter {
	private final MainWindow mainWindow;
	private final TreeTab tab;

	public TreeTabPresenter(MainWindow mainWindow, TreeTab tab) {
		this.mainWindow = mainWindow;
		this.tab = tab;
	}

	public void setup() {
		var controller = mainWindow.getController();

		controller.getCopyImageMenuItem().setOnAction(null);

		controller.getPasteMenuItem().setOnAction(null);

		controller.getUndoMenuItem().setOnAction(e -> tab.getUndoManager().undo());
		controller.getUndoMenuItem().disableProperty().bind(tab.getUndoManager().undoableProperty().not());
		controller.getRedoMenuItem().setOnAction(e -> tab.getUndoManager().redo());
		controller.getRedoMenuItem().disableProperty().bind(tab.getUndoManager().redoableProperty().not());

		controller.getFindMenuItem().setOnAction(null);
		controller.getFindAgainMenuItem().setOnAction(null);

		controller.getReplaceMenuItem().setOnAction(null);

		controller.getSelectAllMenuItem().setOnAction(null);
		controller.getSelectNoneMenuItem().setOnAction(null);

		controller.getSelectAllNodesMenuItem().setOnAction(null);
		controller.getSelectAllLabeledNodesMenuItem().setOnAction(null);
		controller.getSelectAllBelowMenuItem().setOnAction(null);

		controller.getInvertNodeSelectionMenuItem().setOnAction(null);
		controller.getDeselectAllNodesMenuItem().setOnAction(null);
		controller.getSelectAllEdgesMenuItem().setOnAction(null);
		controller.getSelectAllLabeledEdgesMenuItem().setOnAction(null);
		controller.getSelectAllEdgesBelowMenuItem().setOnAction(null);
		controller.getInvertEdgeSelectionMenuItem().setOnAction(null);
		controller.getDeselectEdgesMenuItem().setOnAction(null);
		controller.getSelectFromPreviousMenuItem().setOnAction(null);

		controller.getIncreaseFontSizeMenuItem().setOnAction(null);
		controller.getDecreaseFontSizeMenuItem().setOnAction(null);

		controller.getZoomInMenuItem().setOnAction(null);
		controller.getZoomOutMenuItem().setOnAction(null);


		controller.getResetMenuItem().setOnAction(null);
		controller.getRotateLeftMenuItem().setOnAction(null);
		controller.getRotateRightMenuItem().setOnAction(null);
		controller.getFlipMenuItem().setOnAction(null);
		controller.getWrapTextMenuItem().setOnAction(null);
		controller.getFormatNodesMenuItem().setOnAction(null);
		controller.getLayoutLabelsMenuItem().setOnAction(null);
		controller.getSparseLabelsCheckMenuItem().setOnAction(null);
		controller.getShowScaleBarMenuItem().setOnAction(null);

	}
}
