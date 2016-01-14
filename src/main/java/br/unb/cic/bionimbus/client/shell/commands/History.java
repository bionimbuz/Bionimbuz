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
