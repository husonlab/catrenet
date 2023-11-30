/*
 * WarnAboutMissingMoleculesOrUnusedFood.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.action;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.util.CollectionUtils;
import jloda.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * warn about molecules unsed in the reactions that are never provided or produced, or food items that are never used
 * Daniel Huson, 4.2020
 */
public class WarnAboutMissingMoleculesOrUnusedFood {
    public static void run(MainWindow mainWindow) {
        final ReactionSystem inputReactions = mainWindow.getInputReactionSystem();
        final MainWindowController controller = mainWindow.getController();

        final AService<Result> aService = new AService<>(() -> {
            final Set<MoleculeType> missingCatalysts = inputReactions.getReactions().parallelStream().map(Reaction::getCatalystElements).flatMap(Collection::stream).collect(Collectors.toCollection(TreeSet::new));
            final Set<MoleculeType> foodAndProducts = new HashSet<>(inputReactions.getFoods());
            foodAndProducts.addAll(inputReactions.getReactions().parallelStream().filter(r -> r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both).map(Reaction::getProducts).flatMap(Collection::stream).collect(Collectors.toSet()));
            foodAndProducts.addAll(inputReactions.getReactions().parallelStream().filter(r -> r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both).map(Reaction::getReactants).flatMap(Collection::stream).collect(Collectors.toSet()));

            missingCatalysts.removeAll(foodAndProducts);

			return new Result(missingCatalysts, CollectionUtils.difference(inputReactions.getFoods(), inputReactions.computeMentionedFoods(inputReactions.getFoods())));
        });
        aService.setOnSucceeded(c -> {
            var missingCatalysts = aService.getValue().getMissingCatalysts().size();
            if (missingCatalysts == 1) {
                var message = "There is one catalyst that is never provided or produced: '" + aService.getValue().getMissingCatalysts().iterator().next() + "'.";
                NotificationManager.showWarning(message);
                controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\n" + message);
            } else if (missingCatalysts == 2) {
                var message = "There are " + aService.getValue().getMissingCatalysts().size() + " catalysts that are never provided or produced: '"
                              + StringUtils.toString(aService.getValue().getMissingCatalysts(), "', '") + "'.";
                NotificationManager.showWarning(message);
                controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\n" + message);
            }
            var unusedFood = aService.getValue().getUnusedFood().size();
            if (unusedFood == 1) {
                var message = "There is one food item that is never used: '" + aService.getValue().getUnusedFood().iterator().next() + "'.";
                NotificationManager.showWarning(message);
                controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\n" + message);

            } else if (unusedFood == 2) {
                var message = "There are " + aService.getValue().getUnusedFood().size() + " food items that are never used: '"
                              + StringUtils.toString(aService.getValue().getUnusedFood(), "', '") + "'.";
                NotificationManager.showWarning(message);
                controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\n" + message);
            }
        });
        aService.start();


    }

    static class Result {
        final private Collection<MoleculeType> missingCatalysts;
        final private Collection<MoleculeType> unusedFood;

        public Result(Collection<MoleculeType> missingCatalysts, Collection<MoleculeType> unusedFood) {
            this.missingCatalysts = missingCatalysts;
            this.unusedFood = unusedFood;
        }

        public Collection<MoleculeType> getMissingCatalysts() {
            return missingCatalysts;
        }

        public Collection<MoleculeType> getUnusedFood() {
            return unusedFood;
        }
    }
}
