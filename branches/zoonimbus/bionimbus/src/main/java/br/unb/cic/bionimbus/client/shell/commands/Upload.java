package br.unb.cic.bionimbus.client.shell.commands;

import java.io.File;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;

public class Upload implements Command {

	public static final String NAME = "upload";

	private final SimpleShell shell;

	public Upload(SimpleShell shell) {
		this.shell = shell;
	}

	@Override
	public String execute(String... params) throws Exception {
		if (!shell.isConnected())
			throw new IllegalStateException(
					"This command should be used with an active connection!");

		P2PService p2p = shell.getP2P();
		SyncCommunication comm = new SyncCommunication(p2p);

		shell.print("Uploading file...");

		File file = new File(params[0]);
		FileInfo info = new FileInfo();
		info.setName(params[0]);
		info.setSize(file.length());

		comm.sendReq(new StoreReqMessage(p2p.getPeerNode(), info, ""), P2PMessageType.STORERESP);
		StoreRespMessage resp = (StoreRespMessage) comm.getResp();
		PluginInfo pluginInfo = resp.getPluginInfo();
		p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
		
		return "File " + resp.getFileInfo().getName() + " succesfully uploaded.";
	}

	@Override
	public String usage() {
		return NAME + " <filepath>";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setOriginalParamLine(String param) {
	}

}
