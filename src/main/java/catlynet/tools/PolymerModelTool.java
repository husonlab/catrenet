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

import catlynet.algorithm.PolymerModel;
import catlynet.io.ModelIO;
import catlynet.settings.ArrowNotation;
import catlynet.settings.ReactionNotation;
import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ResourceManagerFX;
import jloda.util.*;
import jloda.util.progress.ProgressPercentage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PolymerModelTool {
	/**
	 * add functional annotations to DNA alignments
	 */
	public static void main(String[] args) throws IOException {
		try {
			ResourceManagerFX.addResourceRoot(catlynet.resources.Resources.class, "catlynet.resources");
			ProgramProperties.setProgramName(PolymerModelTool.class.getSimpleName());
			ProgramProperties.setProgramVersion(catlynet.main.Version.SHORT_DESCRIPTION);

			PeakMemoryUsageMonitor.start();
			(new PolymerModelTool()).run(args);
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
		final var options = new ArgsOptions(args, this, "Constructions polymer models");
		options.setVersion(ProgramProperties.getProgramVersion());
		options.setLicense("Copyright (C) 2023. GPL 3. This program comes with ABSOLUTELY NO WARRANTY.");
		options.setAuthors("Daniel H. Huson and Mike Steel.");

		options.comment("Parameters");
		var alphabetSizeDef = StringUtils.toString(options.getOption("-a", "alphabetSize", "alphabet size (list (x,y,z,...) or range (x-z or x-z/step) ok)", List.of("2")), "");
		var foodMaxLengthDef = StringUtils.toString(options.getOption("-k", "foodMaxLength", "food molecule max length  (list or range ok)", List.of("2")), "");
		var polymerMaxLengthDef = StringUtils.toString(options.getOption("-n", "polymerMaxLength", "polymer max length  (list or range ok)", List.of("4")), "");
		var meansDef = StringUtils.toString(options.getOption("-m", "meanCatalyzed", "mean number of catalyzed reactions per molecule  (list or range ok)", List.of("2.0")), "");
		var replicatesDef = StringUtils.toString(options.getOption("-r", "replicate", "The replicate number (list or range ok)", List.of("1")), "");

		options.comment("Output");
		var outputDir = options.getOption("-o", "output", "Output directory (or stdout)", "stdout");
		var fileNameTemplate = options.getOption("-f", "fileName", "file name template (use %a,%k,%n,%m,%r for parameters)", "polymer_model_a%a_k%k_n%n_m%m_r%r.crs");

		options.comment("Format");
		var reactionNotation = StringUtils.valueOfIgnoreCase(ReactionNotation.class, options.getOption("-rn", "reactionNotation", "Output reaction notation", ReactionNotation.values(), ReactionNotation.Full.name()));
		var arrowNotation = StringUtils.valueOfIgnoreCase(ArrowNotation.class, options.getOption("-an", "arrowNotation", "Output arrow notation", ArrowNotation.values(), ArrowNotation.UsesMinus.name()));

		options.done();

		var alphabetSizes = NumberUtils.parsePositiveIntegers(alphabetSizeDef, false);
		var foodMaxLengths = NumberUtils.parsePositiveIntegers(foodMaxLengthDef, false);
		var polymerMaxLengths = NumberUtils.parsePositiveIntegers(polymerMaxLengthDef, false);
		var means = NumberUtils.parsePositiveDoubles(meansDef, false);
		var replicates = NumberUtils.parsePositiveIntegers(replicatesDef, false);
		var countFiles = 0;
		try (var progress = new ProgressPercentage("Writing files to: " + outputDir)) {
			progress.setMaximum((long) alphabetSizes.size() * foodMaxLengths.size() * polymerMaxLengths.size() * means.size() * replicates.size());

			for (var a : alphabetSizes) {
				for (var k : foodMaxLengths) {
					for (var n : polymerMaxLengths) {
						for (var m : means) {
							for (var r : replicates) {
								String fileName;
								if (outputDir.equals("stdout"))
									fileName = "stdout";
								else {
									fileName = outputDir + File.separator + fileNameTemplate
											.replaceAll("%a", String.valueOf(a))
											.replaceAll("%k", String.valueOf(k))
											.replaceAll("%n", String.valueOf(n))
											.replaceAll("%m", StringUtils.removeTrailingZerosAfterDot(m))
											.replaceAll("%r", String.valueOf(r));
								}
								var polymerModel = new PolymerModel();
								polymerModel.setInputParameters(new PolymerModel.Parameters(a, k, n, m, r));
								var reactionSystem = polymerModel.apply();
								try (var w = FileUtils.getOutputWriterPossiblyZIPorGZIP(fileName)) {
									w.write("# Polymer model a=%d k=%d n=%d m=%s r=%d:%n%n".formatted(a, k, n, StringUtils.removeTrailingZerosAfterDot(m), r));
									ModelIO.write(reactionSystem, w, true, reactionNotation, arrowNotation);
									w.write("\n#EOF\n");
									countFiles++;
								}
								progress.incrementProgress();
							}
						}
					}
				}
			}
		}
		System.err.printf("Number of files created: %,d%n%n", countFiles);
	}
}

