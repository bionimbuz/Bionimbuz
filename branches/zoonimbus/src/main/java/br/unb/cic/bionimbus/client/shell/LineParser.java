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
