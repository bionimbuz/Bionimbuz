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

import com.google.common.base.Joiner;

import br.unb.cic.bionimbus.client.shell.Command;

public class Help implements Command {

    public static final String NAME = "help";
    private Map<String, Command> commands;

    public Help(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public String execute(String... params) {
        return Joiner.on("\n").join(commands.values());
    }

    @Override
    public String usage() {
        return "help";
    }

    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
        // TODO Auto-generated method stub

    }

}
