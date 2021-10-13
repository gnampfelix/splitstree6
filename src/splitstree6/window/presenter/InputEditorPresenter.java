/*
 *  InputEditorPresenter.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.window.presenter;

import splitstree6.tabs.IDisplayTab;
import splitstree6.window.MainWindow;

public class InputEditorPresenter {
	private final MainWindow mainWindow;

	public InputEditorPresenter(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void apply(IDisplayTab textEditorTab) {

		var controller = mainWindow.getController();

		controller.getPrintMenuItem().setOnAction(e -> {
		});

		controller.getUndoMenuItem().setDisable(false);
		controller.getRedoMenuItem().setDisable(false);

		controller.getCutMenuItem().setDisable(false);
		controller.getCopyMenuItem().setDisable(false);

		controller.getPasteMenuItem().setDisable(false);

		controller.getFindMenuItem().setDisable(false);
		controller.getFindAgainMenuItem().setDisable(false);

		controller.getReplaceMenuItem().setDisable(false);

		controller.getGotoLineMenuItem().setDisable(false);

		controller.getSelectAllMenuItem().setDisable(false);
		controller.getSelectNoneMenuItem().setDisable(false);

		controller.getSelectBracketsMenuItem().setDisable(false);

		controller.getIncreaseFontSizeMenuItem().setDisable(false);
		controller.getDecreaseFontSizeMenuItem().setDisable(false);

		controller.getZoomInMenuItem().setDisable(false);
		controller.getZoomOutMenuItem().setDisable(false);

		controller.getWrapTextMenuItem().setDisable(false);
	}
}
