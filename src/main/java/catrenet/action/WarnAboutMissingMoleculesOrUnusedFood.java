/*
 *  WarnAboutMissingMoleculesOrUnusedFood.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.action;

import catrenet.model.MoleculeType;
import catrenet.model.Reaction;
import catrenet.model.ReactionSystem;
import catrenet.window.MainWindow;
import catrenet.window.MainWindowController;
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
            var message = "";
            if (missingCatalysts == 1) {
                message += "There is one catalyst that is never provided or produced: '" + aService.getValue().getMissingCatalysts().iterator().next() + "'.";
            } else if (missingCatalysts == 2) {
                message += "There are " + aService.getValue().getMissingCatalysts().size() + " catalysts that are never provided or produced: '"
                              + StringUtils.toString(aService.getValue().getMissingCatalysts(), "', '") + "'.";
            }
            var unusedFood = aService.getValue().getUnusedFood().size();
            if (unusedFood == 1) {
                if (!message.isEmpty())
                    message += "\n";
                message += "There is one food item that is never used: '" + aService.getValue().getUnusedFood().iterator().next() + "'.";

            } else if (unusedFood == 2) {
                if (!message.isEmpty())
                    message += "\n";
                message += "There are " + aService.getValue().getUnusedFood().size() + " food items that are never used: '"
                              + StringUtils.toString(aService.getValue().getUnusedFood(), "', '") + "'.";
                NotificationManager.showWarning(message);
                controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\n" + message);
            }
            if (!message.isBlank() && !message.equals(mainWindow.lastWarningMessageProperty().get())) {
                NotificationManager.showWarning(message);
                controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\n" + message);
                mainWindow.lastWarningMessageProperty().set(message);
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
