package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;

public class JobStatus implements Command {
	
	public static final String NAME = "status";

	@Override
	public String execute(String... params) throws Exception {
		return "not implemented yet";
	}

	@Override
	public String usage() {
		return NAME + " <jobId>";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setOriginalParamLine(String param) {
	}

}
