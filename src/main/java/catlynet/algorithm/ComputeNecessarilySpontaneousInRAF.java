/*
 * ComputeNecessarilySpontaneousInRAF.java Copyright (C) 2022 Daniel H. Huson
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
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import jloda.fx.util.AService;
import jloda.util.CanceledException;
import jloda.util.Single;
import jloda.util.StringUtils;
import jloda.util.progress.ProgressSilent;

import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * determines which reactions are necessarily spontaneous in a RAF
 * Daniel Huson, 4.2020
 */
public class ComputeNecessarilySpontaneousInRAF implements IDescribed {

    public String getDescription() {
        return "determine those reactions that must initially run uncatalyzed and then beome catalyzed later";
    }

    public static void apply(MainWindow window, final ReactionSystem inputReactions, MainWindowController controller, ChangeListener<Boolean> runningListener) {

        final AService<Collection<String>> service = new AService<>(controller.getStatusFlowPane());

        service.setCallable(() -> {
            final ReactionSystem maxRAF = (new MaxRAFAlgorithm()).apply(inputReactions, service.getProgressListener());
            return maxRAF.getReactions().parallelStream().filter(r -> computeMaxRAFsForModifiedReactions(maxRAF, r) < maxRAF.getReactions().size()).map(Reaction::getName).collect(Collectors.toCollection(TreeSet::new));
        });

        service.runningProperty().addListener(new WeakChangeListener<>(runningListener));

        service.setOnSucceeded(c -> {
            final String output = String.format("Necessarily spontaneous reactions (%d): %s\n",
                    service.getValue().size(), StringUtils.toString(service.getValue(), ", "));
            controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\nMaxRAF: " + output);
            window.getController().getLogTab().getTabPane().getSelectionModel().select(window.getController().getLogTab());
        });
        service.start();
    }

    private static int computeMaxRAFsForModifiedReactions(ReactionSystem maxRAF, Reaction r0) {

        final Collection<MoleculeType> catalystConjunctions = r0.getCatalystConjunctions();

        final Single<Integer> maxSize = new Single<>(0);
        for (MoleculeType and : catalystConjunctions) {
            final ReactionSystem input = new ReactionSystem();
            input.getFoods().addAll(maxRAF.getFoods());
            for (Reaction r : maxRAF.getReactions()) {
                if (r != r0)
                    input.getReactions().add(r);
                else {
                    if (r.getDirection() == Reaction.Direction.forward || r.getDirection() == Reaction.Direction.both) {
                        final Reaction modified = new Reaction(r.getName() + (r.getDirection() == Reaction.Direction.both ? "/+" : ""), r);
                        modified.setDirection(Reaction.Direction.forward);
                        modified.getReactants().addAll(MoleculeType.valuesOf(StringUtils.split(and.getName(), '&')));
                        input.getReactions().add(modified);
                    }
                    if (r.getDirection() == Reaction.Direction.reverse || r.getDirection() == Reaction.Direction.both) {
                        final Reaction modified = new Reaction(r.getName() + (r.getDirection() == Reaction.Direction.both ? "/-" : ""), r);
                        modified.setDirection(Reaction.Direction.reverse);
                        modified.getProducts().addAll(MoleculeType.valuesOf(StringUtils.split(and.getName(), '&')));
                        input.getReactions().add(modified);
                    }
                }
            }
            try {
                maxSize.set(Math.max(maxSize.get(), (int) (new MaxRAFAlgorithm()).apply(input, new ProgressSilent()).getReactionNames().stream().filter(n -> !n.endsWith("/-")).count()));
            } catch (CanceledException ignored) {
            }
        }
        return maxSize.get();
    }

}
