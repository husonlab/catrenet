/*
 * DisjunctiveNormalForm.java Copyright (C) 2022 Daniel H. Huson
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

package catlynet.model;

import jloda.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

public class DisjunctiveNormalForm {
    public static String compute(String expression) {
		final String result = StringUtils.toString(recurse(expression.replaceAll("\\s+", ",")), ",");
        return result;
    }

    private static Collection<String> recurse(String expression) {
        if (expression.startsWith("(")) {
            int other = findBalance(expression, 0);
            if (other == expression.length() - 1)
                return recurse(expression.substring(1, other));
            else {
                int operator = expression.charAt(other + 1);
                if (operator == ',') {
                    return union(recurse(expression.substring(1, other)), recurse(expression.substring(other + 2)));
                } else if (operator == '&') {
                    return product(recurse(expression.substring(1, other)), recurse(expression.substring(other + 2)));
                }
            }
        } else {
            int pos = nextOr(expression, 0);
            if (pos > 0) {
                return union(recurse(expression.substring(0, pos)), recurse(expression.substring(pos + 1)));
            }
            pos = nextAnd(expression, 0);
            if (pos > 0) {
                return product(recurse(expression.substring(0, pos)), recurse(expression.substring(pos + 1)));
            }
        }
        return Collections.singletonList(expression);
    }


    private static int findBalance(String expression, int i) {
        int depth = 0;
        while (i < expression.length()) {
            int ch = expression.charAt(i);
            if (ch == '(')
                depth++;
            else if (ch == ')')
                depth--;
            if (depth == 0)
                return i;
            i++;
        }
        return -1;
    }

    private static int nextOr(String expression, int i) {
        while (i < expression.length()) {
            int ch = expression.charAt(i);
            if (ch == ',')
                return i;
            else if (ch == '(')
                return -1;
            i++;

        }
        return -1;
    }

    private static int nextAnd(String expression, int i) {
        while (i < expression.length()) {
            int ch = expression.charAt(i);
            if (ch == '&')
                return i;
            else if (ch == '(')
                return -1;
            i++;

        }
        return -1;
    }

    public static Collection<String> union(Collection<String> a, Collection<String> b) {
        final TreeSet<String> set = new TreeSet<>(a);
        set.addAll(b);
        return set;
    }

    public static Collection<String> product(Collection<String> a, Collection<String> b) {
        final TreeSet<String> set = new TreeSet<>();
        for (String aString : a) {
            for (String bString : b) {
                if (aString.equals(bString))
                    set.add(aString);
                else
                    set.add(aString + "&" + bString);
            }
        }
        return set;
    }

    public static void main(String[] args) {
        final String expression = "(A,B)&(C,D)";

        System.err.println(expression + " -> " + compute(expression));
    }
}

