/*
 * Splits2View.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.algorithms.trees.trees2text;

import splitstree6.data.IViewChoice;
import splitstree6.data.TextBlock;
import splitstree6.data.TreesBlock;
import splitstree6.workflow.Algorithm;

public abstract class Trees2Text extends Algorithm<TreesBlock, TextBlock> implements IViewChoice {
	public Trees2Text() {
		super(TreesBlock.class, TextBlock.class);
	}
}
