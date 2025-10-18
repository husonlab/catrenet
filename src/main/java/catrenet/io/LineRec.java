/*
 * LineRec.java Copyright (C) 2025 Daniel H. Huson
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

package catrenet.io;

import java.util.regex.Pattern;

public record LineRec(int idx, int lineNo, String raw, String trimmed) {
	public LineRec(int idx, String raw) {
		this(idx, idx + 1, raw, raw == null ? "" : raw.trim());
	}

	public enum LineType {COMMENT, FOODSET, REACTION}

	// --------- patterns ---------
	public static final Pattern COMMENT_P = Pattern.compile("^\\s*#");
	public static final Pattern F_COLON_P = Pattern.compile("^\\s*F:\\s*");
	public static final Pattern FOOD_BLOCK_START_P = Pattern.compile("^\\s*(?:FoodSet|Food)\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	public static final Pattern FOOD_SIMPLE_P = Pattern.compile("^\\s*(?:FoodSet|Food)\\b(?!\\s*:)", Pattern.CASE_INSENSITIVE);
	public static final Pattern ANY_COLON_P = Pattern.compile(":"); // used to end a Food: block


	public boolean isBlank() {
		return trimmed.isEmpty();
	}

	public boolean isComment() {
		return COMMENT_P.matcher(raw).find();
	}

	public boolean isContent() {
		return !(isBlank() || isComment());
	}

	public boolean startsWithF() {
		return F_COLON_P.matcher(raw).find();
	}

	public boolean startsFoodBlk() {
		return FOOD_BLOCK_START_P.matcher(raw).find();
	}

	public boolean startsFoodSimple() {
		return FOOD_SIMPLE_P.matcher(raw).find();
	}

	public boolean containsColon() {
		return ANY_COLON_P.matcher(raw).find();
	}
}
