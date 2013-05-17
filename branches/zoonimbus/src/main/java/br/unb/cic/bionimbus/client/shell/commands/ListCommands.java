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
		return NAME;
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
