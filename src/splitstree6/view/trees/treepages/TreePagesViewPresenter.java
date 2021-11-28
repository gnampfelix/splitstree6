/*
 *  TreePagesViewPresenter.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.view.trees.treepages;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import jloda.fx.util.Print;
import jloda.phylo.PhyloTree;
import jloda.util.NumberUtils;
import jloda.util.Pair;
import splitstree6.tabs.IDisplayTabPresenter;
import splitstree6.window.MainWindow;

/**
 * multi tree view presenter
 * Daniel Huson, 11.2021
 */
public class TreePagesViewPresenter implements IDisplayTabPresenter {
	private final static ObservableList<String> gridValues = FXCollections.observableArrayList("1 x 1");

	private final MainWindow mainWindow;
	private final TreePagesView treePageView;

	private final TreePagesViewController controller;

	private final ObjectProperty<Dimension2D> boxDimensions = new SimpleObjectProperty<>(new Dimension2D(0, 0));

	/**
	 * constructor
	 */
	public TreePagesViewPresenter(MainWindow mainWindow, TreePagesView treePagesView, ObjectProperty<Bounds> targetBounds, ObservableList<PhyloTree> phyloTrees) {
		this.mainWindow = mainWindow;
		this.treePageView = treePagesView;

		controller = treePagesView.getController();

		controller.getDiagramCBox().setButtonCell(ComboBoxUtils.createDiagramComboBoxListCell());
		controller.getDiagramCBox().setCellFactory(ComboBoxUtils.createDiagramComboxBoxCallback());
		controller.getDiagramCBox().getItems().addAll(ComputeTreeEmbedding.Diagram.values());
		controller.getDiagramCBox().valueProperty().bindBidirectional(treePagesView.optionDiagramProperty());

		controller.getRootSideCBox().setButtonCell(ComboBoxUtils.createRootSideComboBoxListCell());
		controller.getRootSideCBox().setCellFactory(ComboBoxUtils.createRootSideComboBoxCallback());
		controller.getRootSideCBox().getItems().addAll(TreePane.Orientation.values());

		controller.getRootSideCBox().valueProperty().bindBidirectional(treePagesView.optionOrientationProperty());
		//controller.getRootSideCBox().disableProperty().bind(Bindings.createObjectBinding(() -> treePagesView.getOptionDiagram().isRadial(), treePagesView.optionDiagramProperty()));

		controller.getShowTreeNamesToggleButton().selectedProperty().bindBidirectional(treePagesView.optionShowTreeNamesProperty());

		{
			controller.getRowsColsCBox().getItems().setAll(gridValues);
			var text = String.format("%d x %d", treePagesView.getOptionRows(), treePagesView.getOptionCols());
			if (gridValues.contains(text))
				gridValues.set(0, text);
			controller.getRowsColsCBox().setValue(text);
		}

		gridValues.addListener((ListChangeListener<? super String>) e -> controller.getRowsColsCBox().getItems().setAll(gridValues));

		treePagesView.optionRowsProperty().addListener((v, o, n) -> {
			var text = String.format("%d x %d", treePagesView.getOptionRows(), treePagesView.getOptionCols());
			if (!gridValues.contains(text))
				gridValues.set(0, text);
			controller.getRowsColsCBox().setValue(text);
		});

		controller.getRowsColsCBox().valueProperty().addListener((v, o, n) -> {
			if (n != null) {
				var pair = parseRowsColsText(n);
				if (pair != null) {
					treePagesView.setOptionRows(pair.getFirst());
					treePagesView.setOptionCols(pair.getSecond());
					Platform.runLater(() -> controller.getRowsColsCBox().setValue(String.format("%d x %d", treePageView.getOptionRows(), treePagesView.getOptionCols())));
				}
			}
		});

		targetBounds.addListener((v, o, n) -> {
			var width = n.getWidth();
			var height = n.getHeight() - 120;
			controller.getPagination().setPrefWidth(width);
			controller.getPagination().setPrefHeight(height);
			boxDimensions.set(new Dimension2D(width / treePagesView.getOptionCols() - 5, height / treePagesView.getOptionRows() - 5));
		});

		treePagesView.optionRowsProperty().addListener((v, o, n) ->
				boxDimensions.set(new Dimension2D(boxDimensions.get().getWidth(), boxDimensions.get().getHeight() * o.intValue() / n.intValue())));

		treePagesView.optionColsProperty().addListener((v, o, n) ->
				boxDimensions.set(new Dimension2D(boxDimensions.get().getWidth() * o.intValue() / n.intValue(), boxDimensions.get().getHeight())));

		var numberOfPages = new SimpleIntegerProperty(0);

		treePagesView.pageNumberProperty().addListener((v, o, n) -> controller.getPagination().setCurrentPageIndex(n.intValue() - 1));
		controller.getPagination().currentPageIndexProperty().addListener((v, o, n) -> treePagesView.setPageNumber(n.intValue() + 1));

		ObjectProperty<TreePageFactory> treePageFactory = new SimpleObjectProperty<>(null);
		treePageFactory.set(new TreePageFactory(mainWindow, treePagesView, phyloTrees, treePagesView.optionRowsProperty(), treePagesView.optionColsProperty(), boxDimensions));

		controller.getPagination().pageFactoryProperty().bind(treePageFactory);
		controller.getPagination().pageCountProperty().bind(numberOfPages);

		{
			InvalidationListener invalidationListener = e -> numberOfPages.set(1 + (phyloTrees.size() - 1) / (treePagesView.getOptionRows() * treePagesView.getOptionCols()));
			treePagesView.optionRowsProperty().addListener(invalidationListener);
			treePagesView.optionColsProperty().addListener(invalidationListener);
			phyloTrees.addListener(invalidationListener);
			invalidationListener.invalidated(null);
		}

		treePagesView.pageNumberProperty().addListener((v, o, n) -> {
			if (n.intValue() < 1)
				Platform.runLater(() -> treePagesView.setPageNumber(1));
			else if (n.intValue() >= numberOfPages.get())
				Platform.runLater(() -> treePagesView.setPageNumber((Math.max(1, numberOfPages.get()))));
		});

		Platform.runLater(this::setupMenuItems);

		controller.getPrintButton().setOnAction(e -> Print.print(mainWindow.getStage(), treePagesView.imageNodeProperty().get()));
		controller.getPrintButton().disableProperty().bind(treePagesView.emptyProperty());
	}

	private Pair<Integer, Integer> parseRowsColsText(String text) {
		var tokens = text.split("x");
		if (tokens.length == 2 && NumberUtils.isInteger(tokens[0].trim()) && NumberUtils.isInteger(tokens[1].trim()))
			return new Pair<>(Math.max(1, Integer.parseInt(tokens[0].trim())), Math.max(1, Integer.parseInt(tokens[1].trim())));
		else
			return null;
	}

	@Override
	public void setupMenuItems() {
		mainWindow.getController().getIncreaseFontSizeMenuItem().setOnAction(e -> treePageView.setOptionFontScaleFactor(1.2 * treePageView.getOptionFontScaleFactor()));
		mainWindow.getController().getIncreaseFontSizeMenuItem().disableProperty().bind(treePageView.emptyProperty());
		mainWindow.getController().getDecreaseFontSizeMenuItem().setOnAction(e -> treePageView.setOptionFontScaleFactor((1.0 / 1.2) * treePageView.getOptionFontScaleFactor()));
		mainWindow.getController().getDecreaseFontSizeMenuItem().disableProperty().bind(treePageView.emptyProperty());

		mainWindow.getController().getZoomInMenuItem().setOnAction(e -> treePageView.setOptionZoomFactor(1.1 * treePageView.getOptionZoomFactor()));
		mainWindow.getController().getZoomInMenuItem().disableProperty().bind(treePageView.emptyProperty().or(treePageView.optionZoomFactorProperty().greaterThan(1.0 / 1.1)));
		mainWindow.getController().getZoomOutMenuItem().setOnAction(e -> treePageView.setOptionZoomFactor((1.0 / 1.1) * treePageView.getOptionZoomFactor()));
		mainWindow.getController().getZoomOutMenuItem().disableProperty().bind(treePageView.emptyProperty());


		mainWindow.getController().getPrintMenuItem().setOnAction(controller.getPrintButton().getOnAction());
		mainWindow.getController().getPrintMenuItem().disableProperty().bind(controller.getPrintButton().disableProperty());

		mainWindow.getController().getSelectAllMenuItem().setOnAction(e -> mainWindow.getTaxonSelectionModel().selectAll(mainWindow.getWorkflow().getWorkingTaxaBlock().getTaxa()));
		mainWindow.getController().getSelectNoneMenuItem().setOnAction(e -> mainWindow.getTaxonSelectionModel().clearSelection());
		mainWindow.getController().getSelectNoneMenuItem().disableProperty().bind(mainWindow.getTaxonSelectionModel().sizeProperty().isEqualTo(0));
	}
}
