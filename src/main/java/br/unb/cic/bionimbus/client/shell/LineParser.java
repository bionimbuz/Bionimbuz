/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.unb.cic.bionimbus.client.shell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class LineParser {

    enum State {
        NEW, BUILDING, QUOTED
    }

    private static final Set<Character> spaceChars = new HashSet<Character>();

    static {
        spaceChars.add(' ');
        spaceChars.add('\t');
        spaceChars.add('\n');
    }

    private static final Set<Character> quoted = new HashSet<Character>();

    static {
        quoted.add('\"');
        quoted.add('\'');
    }

    private State state;

    private StringBuilder sb = new StringBuilder();

    public LineParser() {
        // estado inicial
        state = State.NEW;
    }

    public List<String> parse(String line) {

        final Stack<String> stack = new Stack<String>();

        char[] chars = line.toCharArray();

        for (char c : chars) {

            if (quoted.contains(c)) {
                if (state == State.QUOTED) {
                    state = State.NEW;
                    if (sb.length() > 0) {
                        stack.push(sb.toString());
                    }
                    sb = new StringBuilder();
                } else {
                    state = State.QUOTED;
                }

                continue;
            }

            if (spaceChars.contains(c)) {
                switch (state) {
                    case NEW:
                        continue;
                    case QUOTED:
                        sb.append(c);
                        break;
                    case BUILDING:
                        if (sb.length() > 0)
                            stack.push(sb.toString());
                        sb = new StringBuilder();
                        state = State.NEW;
                        continue;
                }
            } else {

                if (state == State.NEW) {
                    state = State.BUILDING;
                }
                sb.append(c);
            }
        }

        // armazena o resto
        if (sb.length() > 0)
            stack.push(sb.toString());

        return stack;
    }
}
