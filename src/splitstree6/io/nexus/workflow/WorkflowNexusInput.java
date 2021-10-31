/*
 *  WorkflowNexusInput.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree6.io.nexus.workflow;

import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.Pair;
import jloda.util.parse.NexusStreamParser;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import splitstree6.algorithms.taxa.taxa2taxa.TaxaFilter;
import splitstree6.data.SourceBlock;
import splitstree6.data.SplitsTree6Block;
import splitstree6.io.nexus.AlgorithmNexusInput;
import splitstree6.io.nexus.SplitsTree6NexusInput;
import splitstree6.window.MainWindow;
import splitstree6.workflow.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * imports a file in SplitsTree6 nexus input format
 * Daniel Huson, 10.2021
 */
public class WorkflowNexusInput {
	public static boolean isApplicable(String fileName) {
		try (NexusStreamParser np = new NexusStreamParser(new FileReader(fileName))) {
			if (np.peekMatchIgnoreCase("#nexus")) {
				np.matchIgnoreCase("#nexus");
				return np.peekMatchBeginBlock(SplitsTree6Block.BLOCK_NAME);
			}
		} catch (IOException ex) {
			return false;
		}
		return false;
	}

	public static void open(MainWindow mainWindow, String fileName) {
		var workflow = mainWindow.getWorkflow();
		workflow.clear();

		if (false) {
			try (var reader = new BufferedReader(new FileReader(fileName))) {
				input(new ProgressPercentage(), workflow, reader);
			} catch (Exception exception) {
				NotificationManager.showError("Open file failed: " + exception);
			}

		} else {
			var service = new AService<Workflow>(mainWindow.getController().getBottomFlowPane());
			service.setCallable(() -> {
				var inputWorkFlow = new Workflow(null);
				try (var reader = new BufferedReader(new FileReader(fileName))) {
					input(service.getProgressListener(), inputWorkFlow, reader);
				}
				return inputWorkFlow;
			});
			service.setOnSucceeded(e -> {
				var inputWorkFlow = service.getValue();
				NotificationManager.showInformation("Loaded file: " + fileName + ", workflow nodes: " + inputWorkFlow.size());
				workflow.shallowCopy(inputWorkFlow);

			});

			service.setOnFailed(e -> NotificationManager.showError("Open file failed : " + service.getException()));
			service.setOnCancelled(e -> NotificationManager.showError("Open file : canceled"));
			service.start();
		}
		mainWindow.setFileName(fileName);
	}

	/**
	 * input a work flow from a reader
	 */
	public static void input(ProgressListener progress, Workflow workflow, Reader reader) throws IOException {
		try (NexusStreamParser np = new NexusStreamParser(reader)) {
			np.matchIgnoreCase("#nexus");

			final SplitsTree6Block splitsTree6Block = new SplitsTree6Block();
			(new SplitsTree6NexusInput()).parse(np, splitsTree6Block);
			// todo: check input based on splitsTree6Block

			final NexusDataBlockInput dataInput = new NexusDataBlockInput();

			var inputTaxaBlock = dataInput.parse(np);
			var taxaFilter = (new AlgorithmNexusInput()).parse(np);
			if (!(taxaFilter instanceof TaxaFilter))
				throw new IOExceptionWithLineNumber("Excepted TaxaFilter", np.lineno());
			var workingTaxaBlock = dataInput.parse(np);
			var workingTaxaTitle = dataInput.getTitle();
			var inputDataBlock = dataInput.parse(np, inputTaxaBlock);
			var dataTaxaFilter = (new AlgorithmNexusInput()).parse(np);
			if (!(dataTaxaFilter instanceof DataTaxaFilter))
				throw new IOExceptionWithLineNumber("Excepted DataTaxaFilter", np.lineno());
			if (dataTaxaFilter.getFromClass() != inputDataBlock.getClass())
				throw new IOExceptionWithLineNumber("Input data and DataTaxaFilter of incompatible types", np.lineno());
			var workingDataBlock = dataInput.parse(np, inputTaxaBlock);
			var workingDataTitle = dataInput.getTitle();
			if (dataTaxaFilter.getToClass() != workingDataBlock.getClass())
				throw new IOExceptionWithLineNumber("Working data and DataTaxaFilter of incompatible types", np.lineno());

			workflow.setupInputAndWorkingNodes(new SourceBlock(), inputTaxaBlock, (TaxaFilter) taxaFilter, workingTaxaBlock, inputDataBlock, (DataTaxaFilter) dataTaxaFilter, workingDataBlock);

			final var titleNodeMap = new HashMap<String, DataNode>();
			titleNodeMap.put(workingTaxaTitle, workflow.getWorkingTaxaNode());
			titleNodeMap.put(workingDataTitle, workflow.getWorkingDataNode());

			final var title2algorithmAndLink = new HashMap<String, Pair<Algorithm, String>>();

			while (np.peekMatchIgnoreCase("begin")) {
				if (np.peekMatchBeginBlock("algorithm")) {
					final AlgorithmNexusInput algorithmInput = new AlgorithmNexusInput();
					final Algorithm algorithm = algorithmInput.parse(np);
					title2algorithmAndLink.put(algorithmInput.getTitle(), new Pair<>(algorithm, algorithmInput.getLink().getSecond()));
				} else {
					final DataBlock newDataBlock = dataInput.parse(np, workingTaxaBlock);
					/*
					if (dataBlock instanceof TraitsBlock)
						taxaBlock.setTraitsBlock((TraitsBlock) dataBlock);

					 */
					final DataNode newDataNode = workflow.newDataNode(newDataBlock);
					if (dataInput.getLink() != null) {
						final var algorithmAndLink = title2algorithmAndLink.get(dataInput.getLink().getSecond());
						final Algorithm algorithm = algorithmAndLink.getFirst();
						final DataNode parentDataNode = titleNodeMap.get(algorithmAndLink.getSecond());
						workflow.newAlgorithmNode(algorithm, workflow.getWorkingTaxaNode(), parentDataNode, newDataNode);
					}
					titleNodeMap.put(dataInput.getTitle(), newDataNode);
				}
				progress.setProgress(np.lineno());
			}
		} catch (Exception ex) {
			Basic.caught(ex);
			throw ex;
		}
	}
}
