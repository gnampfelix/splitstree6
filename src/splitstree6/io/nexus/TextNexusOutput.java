/*
 * TraitsNexusOutput.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.io.nexus;


import splitstree6.data.TaxaBlock;
import splitstree6.data.TextBlock;

import java.io.IOException;
import java.io.Writer;

/**
 * text nexus output
 * Daniel Huson, 2.2023
 */
public class TextNexusOutput extends NexusIOBase implements INexusOutput<TextBlock> {
	/**
	 * write a block in nexus format
	 */
	@Override
	public void write(Writer w, TaxaBlock taxaBlock, TextBlock textBlock) throws IOException {
		w.write("\nBEGIN TEXT;\n");
		writeTitleAndLink(w);
		w.write("TEXT\n");
		for (var line : textBlock.getLines()) {
			var index = line.indexOf(';');
			if (index > 0 && index < line.length() - 1)
				w.write("\t'%s'%n".formatted(line));
			else
				w.write("\t" + line + "\n");
		}
		w.write(";\n");
		w.write("END; [TEXT]\n");
	}
}
