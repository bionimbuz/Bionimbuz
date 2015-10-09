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

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class ListCommands implements Command {

    public static final String NAME = "list";
    private SimpleShell shell;

    public ListCommands(SimpleShell shell) {
        this.shell = shell;
    }


    @Override
    public String execute(String... params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Command c : shell.getCommands()) {
            sb.append(c.usage()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String usage() {
        return NAME +"<Host>"+"<Port>" ;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
        // TODO Auto-generated method stub

    }


}
