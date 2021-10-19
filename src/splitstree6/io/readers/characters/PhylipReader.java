/*
 * PhylipCharactersImporter.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree6.io.readers.characters;

import jloda.util.*;
import jloda.util.progress.ProgressListener;
import splitstree6.data.CharactersBlock;
import splitstree6.data.TaxaBlock;
import splitstree6.data.parts.CharactersType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * phylip sequence import
 * Daniel Huson, 3.2020
 */
public class PhylipReader extends CharactersReader {
	public PhylipReader() {
		setFileExtensions("phy", "phylip");
	}

	@Override
	public void read(ProgressListener progress, String fileName, TaxaBlock taxaBlock, CharactersBlock characters) throws IOException {
		int nTax = -1;
		int nChar = -1;
		var interleaved = false;

		try (FileLineIterator it = new FileLineIterator(fileName)) {
			var hasEmptyLine = false;
			progress.setMaximum(it.getMaximumProgress());
			progress.setProgress(0);
			var countNonEmptyLines = 0;

			for (var line : it.lines()) {
				if (!line.isBlank()) {
					if (nTax == -1) {
						try {
							var tokens = line.trim().split("\\s+");
							nTax = Integer.parseInt(tokens[0]);
							nChar = Integer.parseInt(tokens[1]);
							//taxa.setNtax(nTax);
						} catch (Exception ex) {
							throw new IOException("Failed to read number of taxa and characters");
						}
					}
					countNonEmptyLines++;
					if (hasEmptyLine && countNonEmptyLines > nTax + 1) {
						interleaved = true;
						break;
					}
				} else {
					if (countNonEmptyLines == nTax + 1)
						hasEmptyLine = true;
				}
				progress.setProgress(it.getProgress());
			}
		}

		if (!interleaved) {
			try (FileLineIterator it = new FileLineIterator(fileName)) {
				progress.setMaximum(it.getMaximumProgress());
				progress.setProgress(0);

				var first = true;

				var taxaSet = new HashSet<String>();
				var taxonNames = new ArrayList<String>();
				var sequences = new ArrayList<String>();
				String taxonName = null;
				var sequence = new StringBuilder();

				for (var line : it.lines()) {
					if (!line.isBlank()) {
						if (first) {
							first = false;
							try {
								var tokens = line.trim().split("\\s+");
								nTax = Integer.parseInt(tokens[0]);
								nChar = Integer.parseInt(tokens[1]);
								//taxa.setNtax(nTax);
							} catch (Exception ex) {
								throw new IOException("Failed to read number of taxa and characters");
							}
						} else {
							if (taxonName == null) {
								taxonName = line.substring(0, 10).trim();
								taxonName = StringUtils.getUniqueName(taxonName, taxaSet);
								taxaSet.add(taxonName);
								sequence.setLength(0);
								sequence.append(line.substring(10).replaceAll("\\s+", ""));
							} else
								sequence.append(line.replaceAll("\\s+", ""));
							if (sequence.length() == nChar) {
								sequences.add(sequence.toString());
								sequence.setLength(0);
								taxonNames.add(taxonName);
								taxonName = null;
							}
						}
					}
					progress.setProgress(it.getProgress());
				}
				if (sequence.length() > 0) {
					taxonNames.add(taxonName);
					sequences.add(sequence.toString());
					sequence.setLength(0);
				}
				if (taxonNames.size() != nTax) {
					throw new IOException(String.format("Expected %d taxa, found: %d", nTax, taxonNames.size()));
				}
				taxaBlock.addTaxaByNames(taxonNames);
				characters.setDimension(nTax, nChar);
				characters.setDataType(CharactersType.guessType(CharactersType.union(sequences.toArray(new String[0]))));
				characters.setGapCharacter(getGap());
				characters.setMissingCharacter(getMissing());

				for (int i = 0; i < sequences.size(); i++) {
					var seq = sequences.get(i);
					if (seq.length() != nChar)
						throw new IOException(String.format("Sequence %d: expected %d characters, found: %d", (i + 1), nChar, seq.length()));

					for (int j = 0; j < seq.length(); j++) {
						var ch = seq.charAt(j);
						if (Character.isWhitespace(ch))
							throw new IOException(String.format("Sequence %d contains whitespace: %100s", (i + 1), seq));
						characters.set(i + 1, j + 1, seq.charAt(j));
					}
				}
			}
		} else // interleaved
		{
			try (FileLineIterator it = new FileLineIterator(fileName)) {
				progress.setMaximum(it.getMaximumProgress());
				progress.setProgress(0);

				var taxaSet = new HashSet<String>();
				var taxonNames = new ArrayList<String>();
				var sequenceBuffers = new ArrayList<StringBuilder>();

				int which = 0;

				var first = true;

				for (var line : it.lines()) {
					if (!line.isBlank()) {
						if (first) {
							first = false;
							try {
								var tokens = line.trim().split("\\s+");
								nTax = Integer.parseInt(tokens[0]);
								nChar = Integer.parseInt(tokens[1]);
							} catch (Exception ex) {
								throw new IOException("Failed to read number of taxa and characters");
							}
							continue;
						} else if (taxonNames.size() < nTax) {
							var name = line.substring(0, 10).trim();
							name = StringUtils.getUniqueName(name, taxaSet);
							taxaSet.add(name);
							taxonNames.add(name);
							sequenceBuffers.add(new StringBuilder());
							sequenceBuffers.get(which).append(line.substring(10).replaceAll("\\s+", ""));
						} else if (line.startsWith(taxonNames.get(which))) {
							sequenceBuffers.get(which).append(line.substring(10).replaceAll("\\s+", ""));
						} else
							sequenceBuffers.get(which).append(line.replaceAll("\\s+", ""));
						if (++which == nTax)
							which = 0;
					}

					progress.setProgress(it.getProgress());
				}
				taxaBlock.addTaxaByNames(taxonNames);
				var sequences = new ArrayList<String>();
				for (var buf : sequenceBuffers) {
					sequences.add(buf.toString());
				}
				characters.setDimension(nTax, nChar);
				characters.setDataType(CharactersType.guessType(CharactersType.union(sequences.toArray(new String[0]))));
				characters.setGapCharacter(getGap());
				characters.setMissingCharacter(getMissing());

				for (int i = 0; i < sequences.size(); i++) {
					var seq = sequences.get(i);
					for (int j = 0; j < seq.length(); j++)
						characters.set(i + 1, j + 1, seq.charAt(j));
				}
			}
		}
	}

	@Override
	public boolean accepts(String fileName) {
		if (!super.accepts(fileName))
			return false;
		var line = FileUtils.getFirstLineFromFile(new File(fileName));
		if (line == null)
			return false;
		var tokens = line.trim().split("\\s+");
		return tokens.length == 2 && Basic.isInteger(tokens[0]) && Basic.isInteger(tokens[1]);
	}
}

