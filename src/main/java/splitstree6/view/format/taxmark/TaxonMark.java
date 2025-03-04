/*
 *  TaxonMark.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.view.format.taxmark;

import javafx.scene.layout.Pane;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.ExtendedFXMLLoader;
import splitstree6.window.MainWindow;


public class TaxonMark extends Pane {
	private final TaxonMarkController controller;
	private final TaxonMarkPresenter presenter;

	public TaxonMark(MainWindow mainWindow, UndoManager undoManager) {
		var loader = new ExtendedFXMLLoader<TaxonMarkController>(TaxonMarkController.class);
		controller = loader.getController();
		getChildren().add(loader.getRoot());

		presenter = new TaxonMarkPresenter(mainWindow, undoManager, controller);
	}

}
