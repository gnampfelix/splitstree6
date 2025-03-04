/*
 * AlgorithmNodeContextMenu.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.contextmenus.algorithmnode;

import javafx.scene.control.ContextMenu;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.ExtendedFXMLLoader;
import splitstree6.window.MainWindow;
import splitstree6.workflow.AlgorithmNode;

public class AlgorithmNodeContextMenu {
	private final ContextMenu contextMenu;

	public AlgorithmNodeContextMenu(MainWindow mainWindow, UndoManager undoManager, AlgorithmNode algorithmNode) {
		var loader = new ExtendedFXMLLoader<AlgorithmNodeContextMenuController>(AlgorithmNodeContextMenuController.class);

		var controller = loader.getController();

		new AlgorithmNodeContextMenuPresenter(mainWindow, undoManager, controller, algorithmNode);
		undoManager.undoableProperty().addListener((v, o, n) -> mainWindow.setDirty(true));

		contextMenu = controller.getContextMenu();
	}

	public ContextMenu getContextMenu() {
		return contextMenu;
	}

	public static ContextMenu create(MainWindow mainWindow, UndoManager undoManager, AlgorithmNode algorithmNode) {
		var menu = new AlgorithmNodeContextMenu(mainWindow, undoManager, algorithmNode);
		return menu.getContextMenu();
	}
}
