/*
 * WorkflowTabController.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.tabs.workflow;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.icons.MaterialIcons;

public class WorkflowTabController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private BorderPane borderPane;

	@FXML
	private Pane mainPane;

	@FXML
	private VBox topVBox;

	@FXML
	private ToolBar toolBar;

	@FXML
	private Button zoomButton;

	@FXML
	private Button zoomInButton;

	@FXML
	private Button zoomOutButton;

	private ZoomableScrollPane scrollPane;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private void initialize() {
		MaterialIcons.setIcon(zoomButton, "crop_free");
		MaterialIcons.setIcon(zoomInButton, "zoom_in");
		MaterialIcons.setIcon(zoomOutButton, "zoom_out");

		borderPane.getChildren().remove(mainPane);
		var anchorPane = new AnchorPane(mainPane);
		AnchorPane.setRightAnchor(mainPane, 20.0);
		AnchorPane.setLeftAnchor(mainPane, 20.0);
		AnchorPane.setTopAnchor(mainPane, 20.0);
		AnchorPane.setBottomAnchor(mainPane, 20.0);
		scrollPane = new ZoomableScrollPane(anchorPane);
		borderPane.setCenter(scrollPane);
	}

	public ZoomableScrollPane getScrollPane() {
		return scrollPane;
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public BorderPane getBorderPane() {
		return borderPane;
	}

	public Pane getMainPane() {
		return mainPane;
	}

	public VBox getTopVBox() {
		return topVBox;
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public Button getZoomButton() {
		return zoomButton;
	}

	public Button getZoomInButton() {
		return zoomInButton;
	}

	public Button getZoomOutButton() {
		return zoomOutButton;
	}

	public ProgressIndicator getProgressIndicator() {
		return progressIndicator;
	}
}
