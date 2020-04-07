/*
 * ComputeNecessarilySpontaneousInRAF.java Copyright (C) 2020. Daniel H. Huson
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

package catlynet.algorithm;

import catlynet.model.MoleculeType;
import catlynet.model.Reaction;
import catlynet.model.ReactionSystem;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import jloda.fx.util.AService;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressSilent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * determines which reactions are necessarily spontaneous in a RAF
 */
public class ComputeNecessarilySpontaneousInRAF {
    public static void apply(MainWindow window, final ReactionSystem inputReactions, MainWindowController controller, ChangeListener<Boolean> runningListener) {

        final AService<Collection<String>> service = new AService<>(controller.getStatusFlowPane());

        service.setCallable(() -> {
            final Set<String> result = new TreeSet<>();

            final ReactionSystem maxRAF = (new MaxRAFAlgorithm()).apply(inputReactions, service.getProgressListener()).computeExpandedSystem();

            maxRAF.getReactions().parallelStream().forEach(r -> {
                if (computeMaxRAFsForModifiedReactions(maxRAF, r).parallelStream().anyMatch(s -> s.getReactions().size() < maxRAF.getReactions().size()))
                    result.add(r.getName());
            });
            return result;
        });

        service.runningProperty().addListener(new WeakChangeListener<>(runningListener));

        service.setOnSucceeded(c -> {
            final String output = String.format("Necessarily spontaneous reactions (%d): %s\n",
                    service.getValue().size(), Basic.toString(service.getValue(), ", "));
            final TextArea maxRAFTextArea = window.getTabManager().getTextArea(MaxRAFAlgorithm.Name);
            maxRAFTextArea.setText(maxRAFTextArea.getText() + "\n\n" + output);
            controller.getLogTextArea().setText(controller.getLogTextArea().getText() + "\nMaxRAF: " + output);

            final Tab maxRAFTab = window.getTabManager().getTab(MaxRAFAlgorithm.Name);
            maxRAFTab.getTabPane().getSelectionModel().select(maxRAFTab);
        });
        service.start();
    }

    private static Collection<ReactionSystem> computeMaxRAFsForModifiedReactions(ReactionSystem maxRAF, Reaction r) {
        final ArrayList<ReactionSystem> result = new ArrayList<>();

        final Collection<MoleculeType> catalystConjunctions = r.getCatalystConjunctions();

        for (MoleculeType one : catalystConjunctions) {
            final ReactionSystem input = new ReactionSystem();
            input.getFoods().addAll(maxRAF.getFoods());
            for (Reaction s : maxRAF.getReactions()) {
                if (s != r)
                    input.getReactions().add(s);
                else {
                    final Reaction modified = new Reaction(r);
                    modified.getCatalysts().clear();
                    modified.getCatalysts().addAll(catalystConjunctions.stream().filter(c -> c != one).collect(Collectors.toSet()));
                    modified.getReactants().addAll(MoleculeType.valueOf(Basic.split(one.getName(), '&')));
                    input.getReactions().add(modified);
                }
            }
            try {
                result.add((new MaxRAFAlgorithm()).apply(input, new ProgressSilent()).computeExpandedSystem());
            } catch (CanceledException ignored) {
            }
        }
        return result;
    }
}
