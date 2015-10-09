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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Quit implements Command {

    public static final String NAME = "quit";
    private final SimpleShell shell;

    public Quit(SimpleShell shell) {
        this. shell = shell;
    }

    
    
    @Override
    public String execute(String... params) {
        System.exit(0);
        try {
            shell.close();
        } catch (IOException ex) {
            Logger.getLogger(Quit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String usage() {
        return "quit";
    }

    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
        // TODO Auto-generated method stub

    }
}
