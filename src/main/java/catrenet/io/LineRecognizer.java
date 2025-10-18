/*
 * LineRecognizer.java Copyright (C) 2025 Daniel H. Huson
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

public record LineRecognizer(int lineNo, String raw, String trimmed) {
	enum LineType {COMMENT, FOODSET, REACTION}

	public LineRecognizer(int lineNo, String raw) {
		this(lineNo, raw, raw == null ? "" : raw.trim());
	}

	private static final Pattern COMMENT_P = Pattern.compile("^\\s*#");
	private static final Pattern FOOD_KEYWORD_P = Pattern.compile("^\\s*(?:FoodSet|Food)\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern F_COLON_P = Pattern.compile("^\\s*F:");

	boolean isBlank() {
		return trimmed.isEmpty();
	}

	boolean isComment() {
		return COMMENT_P.matcher(raw).find();
	}

	boolean isFoodKeyword() {
		return FOOD_KEYWORD_P.matcher(raw).find();
	}

	boolean isFColon() {
		return F_COLON_P.matcher(raw).find();
	}

	boolean isContent() {
		return !(isBlank() || isComment());
	}
}
