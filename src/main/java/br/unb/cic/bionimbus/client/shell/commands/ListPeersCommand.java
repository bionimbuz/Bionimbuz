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
