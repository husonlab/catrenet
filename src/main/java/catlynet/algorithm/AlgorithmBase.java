/*
 * AlgorithmBase.java Copyright (C) 2022 Daniel H. Huson
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

import catlynet.model.ReactionSystem;
import jloda.util.CanceledException;
import jloda.util.PluginClassLoader;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * computes a new reaction system
 * Daniel Huson, 7.2019
 */
public abstract class AlgorithmBase implements IDescribed {
    /**
     * get the name of the reaction system computed by this algorithm
     *
     * @return name
     */
    abstract public String getName();

    @Override
    abstract public String getDescription();

    /**
     * run the algorithm
     *
     * @return output
     */
    abstract public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException;

    /**
     * list all known algorithms
     *
     * @return names of all known algorithms
     */
    public static Collection<String> listAllAlgorithms() {
        var list = new ArrayList<String>();
        for (var algorithm : PluginClassLoader.getInstances(AlgorithmBase.class, "catlynet.algorithm")) {
            list.add(StringUtils.toCamelCase(algorithm.getName()));
        }

        return list;
    }

    /**
     * get algorithm by name
     *
     * @param name
     * @return algorithm
     */
    public static AlgorithmBase getAlgorithmByName(String name) {
        for (var algorithm : PluginClassLoader.getInstances(AlgorithmBase.class, "catlynet.algorithm")) {
            if (name.equalsIgnoreCase(StringUtils.toCamelCase(algorithm.getName())))
                return algorithm;
        }
        return null;
    }
}
