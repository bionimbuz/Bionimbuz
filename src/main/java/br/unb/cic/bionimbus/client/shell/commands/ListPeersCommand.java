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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import java.util.List;

/**
 *
 * @author gabriel
 */

public class ListPeersCommand implements Command {
    public static final String NAME = "list-peers";

    private SimpleShell simpleShell;
    private List<NodeInfo> peers;
    
    public ListPeersCommand(SimpleShell simpleShell) {
        this.simpleShell = simpleShell;
    }
    
    @Override
    public String execute(String... params) throws Exception {
        peers = simpleShell.getProxy().getPeersNode();
        StringBuilder peersInfo = new StringBuilder();
        for(NodeInfo node: peers){
            peersInfo.append(node.getPeerId());
            peersInfo.append(node.getAddress());
            peersInfo.append("\n");
        }
        return peersInfo.toString();
    }

    @Override
    public String usage() {
        return "list-peers";
    }

    @Override
    public String getName() {
        return "list-peers";
    }

    @Override
    public void setOriginalParamLine(String param) {
    }
    
}
