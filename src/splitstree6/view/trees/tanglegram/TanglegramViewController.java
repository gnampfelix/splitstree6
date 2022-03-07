/*
 * TanglegramViewController.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jloda.fx.control.CopyableLabel;
import jloda.fx.util.DraggableLabel;
import splitstree6.view.trees.layout.TreeDiagramType;
import splitstree6.view.trees.treepages.LayoutOrientation;

/**
 * tanglegram view controller
 * Daniel Huson, 12.2021
 */
public class TanglegramViewController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	ScrollPane scrollPane;

	@FXML
	private VBox vBox;

	@FXML
	private ToolBar toolBar;

	@FXML
	private Button findButton;

	@FXML
	private Button expandVerticallyButton;

	@FXML
	private Button contractVerticallyButton;

	@FXML
	private Button expandHorizontallyButton;

	@FXML
	private Button contractHorizontallyButton;

	@FXML
	private Button increaseFontButton;

	@FXML
	private Button decreaseFontButton;

	@FXML
	private ComboBox<String> tree1CBox;

	@FXML
	private ComboBox<TreeDiagramType> diagram1CBox;

	@FXML
	private ComboBox<String> tree2CBox;
	@FXML
	private ComboBox<TreeDiagramType> diagram2CBox;

	@FXML
	private ComboBox<LayoutOrientation> orientationCBox;

	@FXML
	private Button previousButton;

	@FXML
	private Button nextButton;

	@FXML
	private ToggleButton showTreeNamesToggleButton;

	@FXML
	private ToggleButton showInternalLabelsToggleButton;

	@FXML
	private BorderPane borderPane;

	@FXML
	private Pane leftPane;

	@FXML
	private Pane rightPane;

	@FXML
	private Pane middlePane;

	@FXML
	private AnchorPane innerAnchorPane;


	@FXML
	private VBox formatVBox;

	@FXML
	private TitledPane formatTitledPane;

	private final CopyableLabel tree1NameLabel = new CopyableLabel();
	private final CopyableLabel tree2NameLabel = new CopyableLabel();


	@FXML
	private void initialize() {
		// draw center first:
		var left = borderPane.getLeft();
		var right = borderPane.getRight();
		var bottom = borderPane.getBottom();
		var top = borderPane.getTop();
		var center = borderPane.getCenter();
		borderPane.getChildren().clear();
		borderPane.setCenter(center);
		borderPane.setLeft(left);
		borderPane.setRight(right);
		borderPane.setTop(top);

		innerAnchorPane.getChildren().add(tree1NameLabel);
		AnchorPane.setTopAnchor(tree1NameLabel, 5.0);
		AnchorPane.setLeftAnchor(tree1NameLabel, 10.0);

		innerAnchorPane.getChildren().add(tree2NameLabel);
		AnchorPane.setTopAnchor(tree2NameLabel, 5.0);
		AnchorPane.setRightAnchor(tree2NameLabel, 10.0);

		DraggableLabel.makeDraggable(tree1NameLabel);
		DraggableLabel.makeDraggable(tree2NameLabel);

		formatVBox.setMinHeight(0);
		formatVBox.setMaxHeight(formatVBox.getPrefHeight());

		if (!formatTitledPane.isExpanded()) {
			formatVBox.setVisible(false);
			formatVBox.setMaxHeight(0);
		} else {
			formatVBox.setVisible(true);
			formatVBox.setMaxHeight(formatVBox.getPrefHeight());
		}

		formatTitledPane.expandedProperty().addListener((v, o, n) -> {
			formatVBox.setVisible(n);
			formatVBox.setMaxHeight(n ? formatVBox.getPrefHeight() : 0);
		});

		innerAnchorPane.getChildren().remove(formatVBox);
		innerAnchorPane.getChildren().add(formatVBox);

	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public VBox getvBox() {
		return vBox;
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public Button getFindButton() {
		return findButton;
	}

	public Button getExpandVerticallyButton() {
		return expandVerticallyButton;
	}

	public Button getContractVerticallyButton() {
		return contractVerticallyButton;
	}

	public Button getExpandHorizontallyButton() {
		return expandHorizontallyButton;
	}

	public Button getContractHorizontallyButton() {
		return contractHorizontallyButton;
	}

	public Button getIncreaseFontButton() {
		return increaseFontButton;
	}

	public Button getDecreaseFontButton() {
		return decreaseFontButton;
	}

	public ComboBox<String> getTree1CBox() {
		return tree1CBox;
	}

	public ComboBox<TreeDiagramType> getDiagram1CBox() {
		return diagram1CBox;
	}

	public ComboBox<String> getTree2CBox() {
		return tree2CBox;
	}

	public ComboBox<TreeDiagramType> getDiagram2CBox() {
		return diagram2CBox;
	}

	public ComboBox<LayoutOrientation> getOrientationCBox() {
		return orientationCBox;
	}

	public Button getPreviousButton() {
		return previousButton;
	}

	public Button getNextButton() {
		return nextButton;
	}

	public ToggleButton getShowTreeNamesToggleButton() {
		return showTreeNamesToggleButton;
	}

	public ToggleButton getShowInternalLabelsToggleButton() {
		return showInternalLabelsToggleButton;
	}

	public Pane getLeftPane() {
		return leftPane;
	}

	public Pane getRightPane() {
		return rightPane;
	}

	public Pane getMiddlePane() {
		return middlePane;
	}

	public BorderPane getBorderPane() {
		return borderPane;
	}

	public CopyableLabel getTree1NameLabel() {
		return tree1NameLabel;
	}

	public CopyableLabel getTree2NameLabel() {
		return tree2NameLabel;
	}

	public AnchorPane getInnerAnchorPane() {
		return innerAnchorPane;
	}

	public VBox getFormatVBox() {
		return formatVBox;
	}
}
