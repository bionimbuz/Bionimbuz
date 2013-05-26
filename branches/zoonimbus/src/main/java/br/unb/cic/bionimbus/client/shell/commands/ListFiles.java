package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.ListReqMessage;
import br.unb.cic.bionimbus.p2p.messages.ListRespMessage;
import br.unb.cic.bionimbus.plugin.PluginFile;

public class ListFiles implements Command {

    public static final String NAME = "files";

    private final SimpleShell shell;

    public ListFiles(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (!shell.isConnected())
            throw new IllegalStateException(
                    "This command should be used with an active connection!");

        P2PService p2p = shell.getP2P();
        SyncCommunication comm = new SyncCommunication(p2p);

        shell.print("Listing files...");

        comm.sendReq(new ListReqMessage(p2p.getPeerNode()),
                P2PMessageType.LISTRESP);
        ListRespMessage resp = (ListRespMessage) comm.getResp();

        String list = "";
        if (!resp.values().isEmpty()) {
            for (PluginFile file : resp.values()) {
                list += "ID: " + file.getId() + "; NAME: " + file.getPath() + "; SIZE: " + file.getSize() + "\n";
            }
            list += resp.values().size() + " files found.\n";
        } else {
            list = "0 files found.\n";
        }
        list += "\n";

        return list;
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
