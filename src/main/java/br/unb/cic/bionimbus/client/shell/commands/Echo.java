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

public class Echo implements Command {

    public static final String NAME = "echo";
    private String line;

    @Override
    public String execute(String... params) throws Exception {
        int index = line.trim().indexOf(NAME) + NAME.length();
        return line.substring(index).trim();
    }

    @Override
    public String usage() {
        return NAME;
    }

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public void setOriginalParamLine(String param) {
        this.line = param;
    }

}
