/*
 * RunCAF.java Copyright (C) 2019. Daniel H. Huson
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

import catlynet.algorithm.ComputeCAF;
import catlynet.io.ModelIO;
import catlynet.model.Model;
import catlynet.window.MainWindow;
import catlynet.window.MainWindowController;
import jloda.fx.util.AService;
import jloda.fx.util.NotificationManager;
import jloda.util.Basic;

/**
 * run the CAF algorithm
 * Daniel Huson, 7.2019
 */
public class RunCAF {
    public static void apply(MainWindow window) {
        final MainWindowController controller = window.getController();
        final AService<Model> service = new AService<>(() -> {
            final Model result = new Model();
            ComputeCAF.apply(window.getModel(), result);
            return result;
        }, controller.getStatusFlowPane());

        service.setOnSucceeded((c) -> {
            final Model result = service.getValue();
            NotificationManager.showInformation("CAF has " + result.getReactions().size() + " elements");
            window.getLogStream().println("CAF has " + result.getReactions().size() + " elements");
            if (result.getReactions().size() > 0)
                controller.getCafTextArea().setText("Food: " + Basic.toString(result.getFoods(), " ") + "\n\n" + ModelIO.getReactionsAsString(result));
            else
                controller.getCafTextArea().setText("");
        });
        service.start();
    }
}
