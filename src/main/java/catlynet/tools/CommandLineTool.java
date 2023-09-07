/*
 * CommandLineTool.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.tools;

import catlynet.action.ImportWimsFormat;
import catlynet.algorithm.AlgorithmBase;
import catlynet.algorithm.MinIRAFHeuristic;
import catlynet.io.ModelIO;
import catlynet.model.ReactionSystem;
import catlynet.settings.ArrowNotation;
import catlynet.settings.ReactionNotation;
import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ResourceManagerFX;
import jloda.util.*;
import jloda.util.progress.ProgressPercentage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandLineTool {
	/**
	 * add functional annotations to DNA alignments
	 */
	public static void main(String[] args) {
		try {
			ResourceManagerFX.addResourceRoot(catlynet.resources.Resources.class, "catlynet.resources");
			ProgramProperties.setProgramName(CommandLineTool.class.getSimpleName());
			ProgramProperties.setProgramVersion(catlynet.main.Version.SHORT_DESCRIPTION);

			PeakMemoryUsageMonitor.start();
			(new CommandLineTool()).run(args);
			System.err.println("Total time:  " + PeakMemoryUsageMonitor.getSecondsSinceStartString());
			System.err.println("Peak memory: " + PeakMemoryUsageMonitor.getPeakUsageString());
			System.exit(0);
		} catch (Exception ex) {
			Basic.caught(ex);
			System.exit(1);
		}
	}

	/**
	 * run the program
	 */
	private void run(String[] args) throws IOException, UsageException {

		var allAlgorithms = AlgorithmBase.listAllAlgorithms();

		final ArgsOptions options = new ArgsOptions(args, this, "Performs Max RAF and other computations");
		options.setVersion(ProgramProperties.getProgramVersion());
		options.setLicense("Copyright (C) 2023. GPL 3. This program comes with ABSOLUTELY NO WARRANTY.");
		options.setAuthors("Daniel H. Huson and Mike Steel.");

		var algorithmName = options.getOptionMandatory("-c", "compute", "The computation to perform", allAlgorithms, "");

		var inputFile = options.getOptionMandatory("-i", "input", "Input file (stdin ok)", "");
		var outputFile = options.getOption("-o", "output", "Output file (stdout ok)", "stdout");
		var reactionNotation = StringUtils.valueOfIgnoreCase(ReactionNotation.class, options.getOption("-rn", "reactionNotation", "Output reaction notation", ReactionNotation.values(), ReactionNotation.Full.name()));
		var arrowNotation = StringUtils.valueOfIgnoreCase(ArrowNotation.class, options.getOption("-an", "arrowNotation", "Output arrow notation", ArrowNotation.values(), ArrowNotation.UsesMinus.name()));

		var numberRandomizedInsertionOrders = (new MinIRAFHeuristic()).getNumberOfRandomInsertionOrders();
		if (algorithmName.equals(StringUtils.toCamelCase(MinIRAFHeuristic.Name)) || options.isDoHelp()) {
			numberRandomizedInsertionOrders = options.getOption("-r", "runs", "Number of randomized runs for " + MinIRAFHeuristic.Name + " heuristic", numberRandomizedInsertionOrders);
		}
		options.comment(ArgsOptions.OTHER);
		final var propertiesFile = options.getOption("-P", "propertiesFile", "Properties file", catlynet.main.CatlyNet.getDefaultPropertiesFile());
		options.done();

		FileUtils.checkAllFilesDifferent(inputFile, outputFile);
		FileUtils.fileExistsAndIsNonEmpty(inputFile);
		FileUtils.checkFileWritable(outputFile, true);

		ProgramProperties.load(propertiesFile);

		var inputSystem = parseInput(inputFile);

		var algorithm = AlgorithmBase.getAlgorithmByName(algorithmName);
		if (algorithm == null)
			throw new IOException("Algorithm not found: " + algorithmName);

		if (algorithm instanceof MinIRAFHeuristic irrRAFHeuristic) {
			irrRAFHeuristic.setNumberOfRandomInsertionOrders(numberRandomizedInsertionOrders);
			var outputSystems = irrRAFHeuristic.applyAllSmallest(inputSystem, new ProgressPercentage("Running", algorithmName));

			if (!outputFile.equalsIgnoreCase("stdout"))
				System.err.println("Writing file: " + outputFile);

			try (var w = FileUtils.getOutputWriterPossiblyZIPorGZIP(outputFile)) {
				for (var outputSystem : outputSystems) {
					ModelIO.write(outputSystem, w, true, reactionNotation, arrowNotation);
					w.write("\n");
				}
			}
		} else {
			var outputSystem = algorithm.apply(inputSystem, new ProgressPercentage("Running", algorithmName));

			if (!outputFile.equalsIgnoreCase("stdout"))
				System.err.println("Writing file: " + outputFile);

			try (var w = FileUtils.getOutputWriterPossiblyZIPorGZIP(outputFile)) {
				ModelIO.write(outputSystem, w, true, reactionNotation, arrowNotation);
			}
		}
	}

	private ReactionSystem parseInput(String fileName) throws IOException {
		final ArrayList<String> inputLines;
		final Pair<ReactionNotation, ArrowNotation> notation;

		if (ImportWimsFormat.isInWimsFormat(fileName)) {
			inputLines = ImportWimsFormat.importToString(fileName);
			notation = ReactionNotation.detectNotation(inputLines.subList(0, 10));
		} else {
			inputLines = FileUtils.getLinesFromFile(fileName);
			final var lines = FileUtils.getFirstLinesFromFile(new File(fileName), 10);
			if (lines == null)
				throw new IOException("Can't read file: " + fileName);
			notation = ReactionNotation.detectNotation(Arrays.asList(lines));
		}

		if (notation == null) {
			throw new IOException("Couldn't detect 'full', 'sparse' or 'tabbed' file format");
		}

		var reactionSystem = new ReactionSystem();
		try (BufferedReader r = new BufferedReader(new StringReader(StringUtils.toString(inputLines, "\n")))) {
			reactionSystem.clear();
			final String leadingComments = ModelIO.read(reactionSystem, r, notation.getFirst());

			System.err.println("Read " + reactionSystem.size() + " reactions" + (reactionSystem.getNumberOfTwoWayReactions() > 0 ? "(" + reactionSystem.getNumberOfTwoWayReactions() + " two-way)" : "")
							   + " and " + reactionSystem.getFoods().size() + " food items from file: " + fileName);

			if (!leadingComments.isBlank())
				System.err.println("Comments:\n" + leadingComments);

			reactionSystem.updateIsInhibitorsPresent();
			if (reactionSystem.isInhibitorsPresent()) {
				System.err.println("Input catalytic reaction system contains inhibitions. These are ignored in the computation of maxCAF, maxRAF and maxPseudoRAF");
			}

			return reactionSystem;
		}
	}
}

