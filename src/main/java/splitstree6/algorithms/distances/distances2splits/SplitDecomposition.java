/*
 * SplitDecomposition.java Copyright (C) 2023 Daniel H. Huson
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

package splitstree6.algorithms.distances.distances2splits;

import jloda.util.progress.ProgressListener;
import splitstree6.algorithms.utils.SplitsBlockUtilities;
import splitstree6.data.DistancesBlock;
import splitstree6.data.SplitsBlock;
import splitstree6.data.TaxaBlock;
import splitstree6.splits.ASplit;
import splitstree6.splits.Compatibility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Split decomposition
 * Created on 12/30/16.
 *
 * @author Daniel Huson
 */
public class SplitDecomposition extends Distances2Splits {

	@Override
	public String getCitation() {
		return "Bandelt and Dress 1992; H.-J.Bandelt and A.W.M.Dress. A canonical decomposition theory for metrics on a finite set. Advances in Mathematics, 92:47–105, 1992.";
	}

	@Override
	public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distancesBlock, SplitsBlock splitsBlock) throws IOException {
		if (SplitsBlockUtilities.computeSplitsForLessThan4Taxa(taxaBlock, distancesBlock, splitsBlock))
			return;

		var previousSplits = new ArrayList<ASplit>(); // list of previously computed splits
		ArrayList<ASplit> nextSplits; // current list of splits

		// ProgressDialog pd = new ProgressDialog("Split Decomposition...",""); //Set new progress bar.
		// doc.setProgressListener(pd);
		progress.setMaximum(taxaBlock.getNtax());    //initialize maximum progress
		progress.setProgress(0);

		final var previousTaxa = new BitSet(); // taxa already processed
		final var ntax = taxaBlock.getNtax();

		previousTaxa.set(1);

		for (var t = 2; t <= ntax; t++) {
			nextSplits = new ArrayList<>(t); // restart current list of splits

			// System.err.println("Processing: "+t);

			// Does t vs previous set of taxa form a split?
			{
				final var At = new BitSet();
				At.set(t);
				final var wgt = getIsolationIndex(t, At, previousTaxa, distancesBlock);
				if (wgt > 0) {
					nextSplits.add(new ASplit((BitSet) At.clone(), t, wgt));
					// System.err.println("Adding (step 3) " + nextSplits.get(nextSplits.size() - 1));
				}
			}

			// consider all previously computed splits:
			for (final var previousSplit : previousSplits) {
				final var A = previousSplit.getA();
				final var B = getComplement(previousSplit.getA(), t - 1);

				// is Au{t} vs B a split?
				{
					A.set(t);
					var wgt = Math.min(previousSplit.getWeight(), getIsolationIndex(t, A, B, distancesBlock));
					if (wgt > 0) {
						nextSplits.add(new ASplit((BitSet) A.clone(), t, wgt));
						// System.err.println("Adding (step 1) " +nextSplits.get(nextSplits.size()-1));
					}
					A.set(t, false);
				}

				// is A vs Bu{t} a split?
				{
					B.set(t);
					var wgt = Math.min(previousSplit.getWeight(), getIsolationIndex(t, B, A, distancesBlock));
					if (wgt > 0) {
						nextSplits.add(new ASplit((BitSet) B.clone(), t, wgt));
						// System.err.println("Adding (step 2) " +nextSplits.get(nextSplits.size()-1));
					}
				}
			}
			previousSplits = nextSplits;

			previousTaxa.set(t);

			progress.setProgress(t);
		}

		// add all missing trivial
		previousSplits.addAll(SplitsBlockUtilities.createAllMissingTrivial(previousSplits, ntax, 0.0));

		// copy splits to splits
		splitsBlock.getSplits().addAll(previousSplits);

		splitsBlock.setFit(SplitsBlockUtilities.computeSplitDecompositionFit(distancesBlock, splitsBlock.getSplits()));
		splitsBlock.setCycle(SplitsBlockUtilities.computeCycle(taxaBlock.getNtax(), splitsBlock.getSplits()));
		splitsBlock.setCompatibility(Compatibility.compute(taxaBlock.getNtax(), splitsBlock.getSplits(), splitsBlock.getCycle()));

		progress.setProgress(taxaBlock.getNtax());   //set progress to 100%
		progress.close();
	}

	/**
	 * Returns the isolation index for Au{x} vs B
	 *
	 * @param t maximal taxon index, assumed to be contained in set A
	 * @param A set A
	 * @param B set B
	 * @param d Distance matrix
	 * @return the isolation index
	 */
	public static float getIsolationIndex(int t, BitSet A, BitSet B, DistancesBlock d) {
		var min_val = Float.MAX_VALUE;

		for (var i = 1; i <= t; i++) {
			if (A.get(i)) {
				for (var j = 1; j <= t; j++) {
					if (B.get(j)) {
						for (var k = j; k <= t; k++) {
							if (B.get(k)) {
								var val = getIsolationIndex(t, i, j, k, d);
								if (val < min_val) {
									if (val <= 0.0000001)
										return 0;
									min_val = val;
								}
							}
						}
					}
				}
			}
		}
		return min_val;
	}

	/**
	 * Returns the isolation index of i,j vs k,l
	 *
	 * @param i a taxon
	 * @param j a taxon
	 * @param k a taxon
	 * @param m a taxon
	 * @param d Distance matrix
	 * @return the isolation index
	 */
	public static float getIsolationIndex(int i, int j, int k, int m, DistancesBlock d) {
		return (float) (0.5 * (Math.max(d.get(i, k) + d.get(j, m), d.get(i, m) + d.get(j, k)) - d.get(i, j) - d.get(k, m)));
	}

	private static BitSet getComplement(BitSet A, int ntax) {
		var result = new BitSet();
		for (var t = A.nextClearBit(1); t != -1 && t <= ntax; t = A.nextClearBit(t + 1))
			result.set(t);
		return result;
	}

	@Override
	public boolean isApplicable(TaxaBlock taxaBlock, DistancesBlock parent) {
		return parent.getNtax() > 0;
	}
}
