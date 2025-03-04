/*
 *  LayoutUtils.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.layout;

import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import jloda.fx.util.BasicFX;
import jloda.fx.util.GeometryUtilsFX;
import splitstree6.layout.tree.LayoutOrientation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * layout utils
 * Daniel Huson, 1.2022
 */
public class LayoutUtils {
	public static void applyOrientation(Collection<? extends Node> shapes, LayoutOrientation oldOrientation, LayoutOrientation newOrientation,
										Consumer<LayoutOrientation> orientationConsumer,
										BooleanProperty changingOrientation) {
		if (!changingOrientation.get()) {
			changingOrientation.set(true);

			var transitions = new ArrayList<Transition>();

			for (var shape : shapes) {
				var translate = new TranslateTransition(Duration.seconds(1));
				translate.setNode(shape);
				var point = new Point2D(shape.getTranslateX(), shape.getTranslateY());

				if (oldOrientation.angle() != 0)
					point = GeometryUtilsFX.rotate(point, oldOrientation.angle());
				if (oldOrientation.flip())
					point = new Point2D(-point.getX(), point.getY());

				if (newOrientation.flip())
					point = new Point2D(-point.getX(), point.getY());
				if (newOrientation.angle() != 0)
					point = GeometryUtilsFX.rotate(point, -newOrientation.angle());
				translate.setToX(point.getX());
				translate.setToY(point.getY());
				transitions.add(translate);
			}
			var parallel = new ParallelTransition(transitions.toArray(new Transition[0]));
			if (orientationConsumer != null)
				parallel.setOnFinished(e -> Platform.runLater(() -> {
					orientationConsumer.accept(newOrientation);
					changingOrientation.set(false);
				}));
			parallel.play();
		}
	}


	/**
	 * scale the translateX and translateY properties by the given values
	 *
	 * @param root      applied to this node and all descendants
	 * @param predicate determines whether node should be processed
	 * @param scaleX    scale x factor
	 * @param scaleY    scale y factor
	 */
	public static void scaleTranslate(javafx.scene.Node root, Predicate<Node> predicate, double scaleX, double scaleY) {
		for (var node : BasicFX.getAllRecursively(root, predicate)) {
			node.setTranslateX(node.getTranslateX() * scaleX);
			node.setTranslateY(node.getTranslateY() * scaleY);
		}
	}
}
