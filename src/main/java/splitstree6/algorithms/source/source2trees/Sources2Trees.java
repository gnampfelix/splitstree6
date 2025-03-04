/*
 * Sources2Trees.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.algorithms.source.source2trees;

import splitstree6.data.SourceBlock;
import splitstree6.data.TreesBlock;
import splitstree6.workflow.Algorithm;

public abstract class Sources2Trees extends Algorithm<SourceBlock, TreesBlock> {
	public Sources2Trees() {
		super(SourceBlock.class, TreesBlock.class);
	}
}
