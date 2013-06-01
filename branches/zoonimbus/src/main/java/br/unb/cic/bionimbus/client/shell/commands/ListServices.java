package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.CloudReqMessage;
import br.unb.cic.bionimbus.p2p.messages.CloudRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;

public class ListServices implements Command {

    public static final String NAME = "services";

    private final SimpleShell shell;

    public ListServices(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        return null;
/*        if (!shell.isConnected())
            throw new IllegalStateException("This command should be used with an active connection!");

        P2PService p2p = shell.getP2P();
        SyncCommunication comm = new SyncCommunication(p2p);

        shell.print("Listing services...");

        comm.sendReq(new CloudReqMessage(p2p.getPeerNode()), P2PMessageType.CLOUDRESP);
        CloudRespMessage cloudMsg = (CloudRespMessage) comm.getResp();

        String list = "";
        if (!cloudMsg.values().isEmpty()) {
            for (PluginInfo info : cloudMsg.values()) {
                for (PluginService service : info.getServices()) {
                    list += "ID: " + service.getId() + "; NAME: " + service.getName() + "\n";
                }
            }
        } else {
            list = "0 services found.\n";
        }
        list += "\n";

        return list;*/
    }

    @Override
    public String usage() {
        return NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
