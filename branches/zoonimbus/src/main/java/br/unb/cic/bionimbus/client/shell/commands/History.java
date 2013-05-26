package br.unb.cic.bionimbus.client.shell.commands;

import java.util.Map;
import java.util.TreeMap;

import br.unb.cic.bionimbus.client.shell.Command;

import com.google.common.base.Joiner;

public class History implements Command {

    public static final String NAME = "history";

    private final Map<Long, String> history = new TreeMap<Long, String>();

    private long value = 1L;

    private int limit;

    public History(int size) {
        this.limit = size;
    }

    @Override
    public String execute(String... params) {
        return Joiner.on("\n").withKeyValueSeparator(" ").join(history);
    }

    public void add(String line) {
        history.put(value++, line);
        if (limit < history.keySet().size()) {
            history.remove(history.keySet().iterator().next());
        }
    }

    @Override
    public String usage() {
        return "history";
    }

    @Override
    public String getName() {
        return NAME;
    }

    public String get(Long number) {
        if (!history.containsKey(number))
            return "!" + number;
        return history.get(number);
    }

    @Override
    public void setOriginalParamLine(String param) {
        // TODO Auto-generated method stub

    }

}
