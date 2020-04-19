/*
 * RunAll.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.action;

import catlynet.algorithm.*;
import catlynet.io.ModelIO;
import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.tab.TabManager;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.value.ChangeListener;
import jloda.fx.util.AService;
import jloda.util.Basic;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * run all algorithms
 * Daniel Huson, 2.2020
 */
public class RunAll {
    public static void apply(MainWindow window, ChangeListener<Boolean> runningListener) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        window.getLogStream().println("\nRun +++++ " + simpleDateFormat.format(System.currentTimeMillis()) + " +++++:");

        final MainWindowController controller = window.getController();
        final TabManager tabManager = window.getTabManager();

        controller.getWorkingReactionsTextArea().clear();
        tabManager.clearAll();

        window.getExportManager().clear();

        if (VerifyInput.verify(window)) {
            final ReactionSystem inputReactions = window.getInputReactionSystem();

            controller.getWorkingReactionsTextArea().setText(String.format("# Input has %,d reactions (%,d two-way and %,d one-way) on %,d food items\n\n%s",
                    inputReactions.size(), inputReactions.getNumberOfTwoWayReactions(), inputReactions.getNumberOfOneWayReactions(), inputReactions.getFoodSize(),
                    ModelIO.toString(window.getInputReactionSystem().sorted(), true, window.getDocument().getReactionNotation(), window.getDocument().getArrowNotation())));

            AService<Set<MoleculeType>> aService = new AService<>(() -> {
                final Set<MoleculeType> catalysts = inputReactions.getReactions().parallelStream().map(Reaction::getCatalystElements).flatMap(Collection::stream).collect(Collectors.toSet());
                final Set<MoleculeType> foodAndProducts = new HashSet<>(inputReactions.getFoods());
                foodAndProducts.addAll(inputReactions.getReactions().parallelStream().filter(r -> r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both).map(Reaction::getProducts).flatMap(Collection::stream).collect(Collectors.toSet()));
                foodAndProducts.addAll(inputReactions.getReactions().parallelStream().filter(r -> r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both).map(Reaction::getReactants).flatMap(Collection::stream).collect(Collectors.toSet()));

                catalysts.removeAll(foodAndProducts);
                return catalysts;
            });
            aService.setOnSucceeded(c -> {
                if (aService.getValue().size() > 0)
                    controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\n\nThere are " + aService.getValue().size() + " catalysts that are never provided or produced: " + Basic.toString(aService.getValue(), ", ") + "\n");
            });
            aService.start();

            RunAlgorithm.apply(window, inputReactions, new MaxCAFAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new MaxRAFAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new MaxPseudoRAFAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new TrivialCAFsAlgorithm(), runningListener, false);
            RunAlgorithm.apply(window, inputReactions, new TrivialRAFsAlgorithm(), runningListener, false);

            if (inputReactions.isInhibitorsPresent()) {
                RunAlgorithm.apply(window, inputReactions, new MuCAFAlgorithm(), runningListener, false);
                RunAlgorithm.apply(window, inputReactions, new URAFAlgorithm(), runningListener, false);
            }
        }
    }
}
