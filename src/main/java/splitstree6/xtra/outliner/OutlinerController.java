/*
 *  OutlinerController.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.xtra.outliner;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class OutlinerController {

	@FXML
	private MenuItem closeMenuItem;

	@FXML
	private MenuItem copyMenuItem;

	@FXML
	private Pane mainPane;

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem openMenuItem;

	@FXML
	private StackPane stackPane;

	@FXML
	private ToolBar tooBar;

	@FXML
	private VBox topPane;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Label label;

	@FXML
	private Button redrawButton;

	@FXML
	private CheckBox othersCheckBox;

	@FXML
	private CheckBox referenceCheckbox;


	@FXML
	private void initialize() {
		progressBar.setVisible(false);
	}

	public MenuItem getCloseMenuItem() {
		return closeMenuItem;
	}

	public MenuItem getCopyMenuItem() {
		return copyMenuItem;
	}

	public Pane getMainPane() {
		return mainPane;
	}

	public Label getLabel() {
		return label;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public MenuItem getOpenMenuItem() {
		return openMenuItem;
	}

	public StackPane getStackPane() {
		return stackPane;
	}

	public ToolBar getTooBar() {
		return tooBar;
	}

	public VBox getTopPane() {
		return topPane;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	@FXML
	private ToggleButton outlineTreeToggleButton;

	public Button getRedrawButton() {
		return redrawButton;
	}

	public CheckBox getOthersCheckBox() {
		return othersCheckBox;
	}

	public CheckBox getReferenceCheckbox() {
		return referenceCheckbox;
	}

	public ToggleButton getOutlineTreeToggleButton() {
		return outlineTreeToggleButton;
	}
}
