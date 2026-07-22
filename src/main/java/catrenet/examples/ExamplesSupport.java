/*
 * ExamplesSupport.java Copyright (C) 2026 Daniel H. Huson
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
 *
 */

package catrenet.examples;

import javafx.scene.control.Menu;
import jloda.fx.examples.ExamplesManager;
import jloda.fx.examples.ExamplesMenu;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Provides access to bundled example files
 * <p>
 * This class sits in the same package as the example resources, so the resource directory
 * is derived from its own package name and there is nothing to keep in sync: move the class
 * and the resources together and the lookup follows.
 * <p>
 * Every resource in the directory ends in {@code .dat}, including {@code index.dat}, which
 * records each example's real file name and suffix.
 */
public class ExamplesSupport {
	/**
	 * The examples directory: this class's own package, e.g. {@code /catrenet/examples}.
	 */
	public static final String RESOURCE_DIR = "/" + ExamplesSupport.class.getPackageName().replace('.', '/');

	private static ExamplesManager manager;

	public static ExamplesManager getManager() {
		if (manager == null) {
			manager = ExamplesManager.createOrNull(ExamplesSupport.class, RESOURCE_DIR);
		}
		return manager;
	}

	/**
	 * Installs an "Open Example" submenu into the given menu.
	 * <p>
	 * The example is read into memory and handed to the application as text, so it can be
	 * loaded as a new untitled document and never written to disk.
	 *
	 * @param loader receives (suggested file name with its real suffix, file content)
	 */
	public static void install(Menu openExamplesMenu, BiConsumer<String, String> loader) {
		var manager = getManager();
		if (manager != null) {
			ExamplesMenu.populate(openExamplesMenu, manager, entry -> {
				try {
					loader.accept(entry.fileName(), manager.readText(entry));
				} catch (IOException ex) {
					System.err.println("Failed to open example " + entry.displayName() + ": " + ex.getMessage());
				}
			});
		}
	}

	private ExamplesSupport() {
	}
}