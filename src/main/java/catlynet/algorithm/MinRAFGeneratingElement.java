/*
 * URAFAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jloda.fx.window.NotificationManager;
import jloda.util.CanceledException;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressListener;

import java.util.ArrayList;
import java.util.Collection;

import static catlynet.io.ModelIO.FORMAL_FOOD;

/**
 * Identifies a subset of the maxRAF that is (i) a RAF and (ii) generates a given element x (not in the food set) and (iii) which is minimal amongst all such sets satisfying (i) and (ii).
 * Daniel Huson, 7.2023
 * Based on notes by Mike Steel
 */
public class MinRAFGeneratingElement extends AlgorithmBase {
    public static final String Name = "Min RAF Generating Element";

    private final ObjectProperty<MoleculeType> target = new SimpleObjectProperty<>(this, "target");

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getDescription() {
        return "Identifies a subset of the maxRAF that is (i) a RAF and (ii) generates a given element x (not in the food set) and (iii) which is minimal amongst all such sets satisfying (i) and (ii)";
    }

    public MoleculeType getTarget() {
        return target.get();
    }

    public ObjectProperty<MoleculeType> targetProperty() {
        return target;
    }

    public void setTarget(MoleculeType target) {
        this.target.set(target);
    }

    /**
     * Identifies a subset of the maxRAF that is (i) a RAF and (ii) generates a given element x (not in the food set) and (iii) which is minimal amongst all such sets satisfying (i) and (ii).
     *
     * @param input - unexpanded catalytic reaction system
     * @return Min RAF generating a specific element
     */
    public ReactionSystem apply(ReactionSystem input, ProgressListener progress) throws CanceledException {
        var empty = new ReactionSystem();
        empty.setName(Name + " '" + getTarget().getName() + "'");

        if (getTarget() == null) {
            NotificationManager.showWarning("No element selected");
            return empty;
        } else if (input.getFoods().contains(getTarget())) {
            NotificationManager.showWarning("Element already contained in food set");
            return empty;
        }

        var maxRAF = (new MaxRAFAlgorithm()).apply(input, progress);
        if (maxRAF.size() == 0) {
            NotificationManager.showWarning("Max RAF is empty");
            return empty;
        }

        var augmented = new ReactionSystem();
        augmented.getFoods().addAll(maxRAF.getFoods());
        for (var r : maxRAF.getReactions()) {
            var r1 = new Reaction(r);
            r1.setCatalysts(andItemToAll(r1.getCatalystConjunctions(), getTarget()));
            augmented.getReactions().add(r1);
        }

        var iRAF = new MinIRAFHeuristic().apply(augmented, progress);
        iRAF.setName(Name + " '" + getTarget().getName() + "'");
        if (iRAF.size() == 0) {
            NotificationManager.showWarning("Irreducible RAF is empty");
            return empty;

        }
        var coreRAF = new CoreRAFAlgorithm().apply(augmented, progress);

        if (coreRAF.size() > 0) {
            NotificationManager.showInformation("Irreducible is unique");
        }
        return iRAF;
    }

    public static String andItemToAll(Collection<MoleculeType> conjunctions, MoleculeType x) {
        if (conjunctions.isEmpty() || conjunctions.stream().allMatch(m -> m == FORMAL_FOOD)) {
            conjunctions.clear();
            return x.getName();
        } else {
            var result = new ArrayList<MoleculeType>();
            for (var one : conjunctions) {
                if (one.equals(x))
                    result.add(x);
                else result.add(MoleculeType.valueOf(one.getName() + "&" + x));
            }
            return StringUtils.toString(result, ",");
        }

    }
}
