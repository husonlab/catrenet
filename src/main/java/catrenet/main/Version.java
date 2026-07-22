/*
 *  Version.java Copyright (C) 2024 Daniel H. Huson
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

package catrenet.main;

public class Version {
	static public final String NAME = "CatReNet";
	public static final String SUFFIX = ".crs";
	static public String HOME_URL = "https://github.com/husonlab/catrenet";

	public static final String VERSION = resolveVersion();
	public static final String SHORT_DESCRIPTION = NAME + " (version 1.0.1, built 22 Jul 2026) - License GPL v3";

	static public final String WEBSITE_URL = "https://husonlab.github.io/catrenet";

	public static String resolveVersion() {
		var pkg = Version.class.getPackage();
		var v = (pkg != null) ? pkg.getImplementationVersion() : null;
		return (v != null && !v.isBlank()) ? v : "dev";
	}
}
