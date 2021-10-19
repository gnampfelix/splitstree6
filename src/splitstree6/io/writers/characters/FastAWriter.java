/*
 *  FastAWriter.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.io.writers.characters;

import jloda.seq.FastA;
import splitstree6.data.CharactersBlock;
import splitstree6.data.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

public class FastAWriter extends CharactersWriter {
	public FastAWriter() {
		setFileExtensions("fasta", "fas", "fa", "seq", "fsa", "fna", "dna");
	}

	public void write(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {
		var fasta = new FastA();
		var ntax = taxa.getNtax();
		var nchar = characters.getNchar();

		for (var i = 1; i <= ntax; i++) {
			var sequence = new StringBuilder("");
			for (var j = 1; j <= nchar; j++) {
				sequence.append(characters.get(i, j));
			}
			fasta.add(taxa.getLabel(i), sequence.toString().toUpperCase());
		}
		fasta.write(w);
	}
}
