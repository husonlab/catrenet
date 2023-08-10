/*
 * PolymerModel.java Copyright (C) 2023 Daniel H. Huson
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

package catlynet.algorithm;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.StringUtils;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.Well19937c;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * implements the polymer model
 * Based on notes by Mike Steel
 * Daniel Huson, 8.2023
 */
public class PolymerModel implements IDescribed {
	public static String Name = "PolymerModel";
	private Parameters inputParameters;

	/**
	 * constructor
	 */
	public PolymerModel() {
	}

	public String getName() {
		return Name;
	}

	@Override
	public String getDescription() {
		return "runs the polymer model with alphabet-size k, food set max length k, polymer max length n and mean number of catalyzed reactions m";
	}

	public ReactionSystem apply() throws CanceledException {
		if (inputParameters != null) {
			return apply(inputParameters.r, inputParameters.k, inputParameters.n, inputParameters.m, inputParameters.seed);
		} else
			return null;
	}

	/**
	 * runs the polymer model with alphabet-size k, polymer length n and mean number of catalyzed reactions m
	 *
	 * @param r    alphabet size
	 * @param k    food items max length
	 * @param n    polymer max length
	 * @param m    mean number of reactions catalyzed by any molecule (Poisson distribution)
	 * @param seed random number seed
	 * @return reaction system
	 */
	public static ReactionSystem apply(int r, int k, int n, double m, int seed) {
		var reactionSystem = new ReactionSystem("PolymerModel_r%d_k%d_n%d_m%s_s%d".formatted(r, k, n, StringUtils.removeTrailingZerosAfterDot("%.2f", m), seed));

		var foodNames = new ArrayList<String>();
		createPolymersRec(r, k, "", foodNames);
		reactionSystem.getFoods().addAll(foodNames.stream().map(MoleculeType::valueOf).toList());


		var polymers = new ArrayList<String>();
		createPolymersRec(r, n, "", polymers);

		if (false)
			System.err.println("Polymers:\n" + StringUtils.toString(polymers, "\n"));

		var reactions = new ArrayList<Reaction>();

		var count = 0;
		for (var polymer : polymers) {
			for (var i = 1; i < polymer.length(); i++) {
				var prefix = polymer.substring(0, i);
				var suffix = polymer.substring(i);
				var reaction = new Reaction(String.format("r%03d", (++count)));
				reaction.getReactants().add(MoleculeType.valueOf(prefix));
				reaction.getReactants().add(MoleculeType.valueOf(suffix));
				reaction.getProducts().add(MoleculeType.valueOf(polymer));
				reaction.setDirection(Reaction.Direction.both);
				reactions.add(reaction);
			}
		}

		var random = new Random(seed);
		var poissonDistribution = new PoissonDistribution(new Well19937c(seed), m, 1.0E-12, 10000000);

		for (var polymer : polymers) {
			var replicate = poissonDistribution.sample();
			while (replicate > 0) {
				var reaction = reactions.get(random.nextInt(reactions.size()));
				var catalysts = reaction.getCatalysts();
				if (catalysts.isBlank())
					reaction.setCatalysts(polymer);
				else
					reaction.setCatalysts(catalysts + "," + polymer);
				replicate--;
			}
		}

		for (var reaction : reactions) {
			if (!reaction.getCatalysts().isBlank())
				reactionSystem.getReactions().add(reaction);
		}
		return reactionSystem;
	}

	private static void createPolymersRec(int r, int k, String prefix, List<String> molecules) {
		if (k > 0) {
			for (var i = 0; i < r; i++) {
				var polymer = prefix + (char) ('a' + i);
				molecules.add(polymer);
			}
			for (var i = 0; i < r; i++) {
				var polymer = prefix + (char) ('a' + i);
				createPolymersRec(r, k - 1, polymer, molecules);
			}
		}
	}

	public Parameters getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(Parameters inputParameters) {
		this.inputParameters = inputParameters;
	}

	public record Parameters(int r, int k, int n, double m, int seed) {
	}

	private static String toLetters(String number) {
		var buf = new StringBuilder();
		for (var i = 0; i < number.length(); i++) {
			buf.append((char) (number.charAt(i) - '0' + 'a'));
		}
		return buf.toString();
	}
}

