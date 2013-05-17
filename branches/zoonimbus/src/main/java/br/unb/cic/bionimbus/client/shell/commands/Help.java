package br.unb.cic.bionimbus.client.shell.commands;

import java.util.Map;

import com.google.common.base.Joiner;

import br.unb.cic.bionimbus.client.shell.Command;

public class Help implements Command {
	
	public static final String NAME = "help";	
	private Map<String, Command> commands;

	public Help(Map<String,Command> commands) {
		this.commands = commands;
	}

	@Override
	public String execute(String... params) {
		return Joiner.on("\n").join(commands.values());
	}

	@Override
	public String usage() {
		return "help";
	}
	
	public String getName() {
		return NAME;
	}

	@Override
	public void setOriginalParamLine(String param) {
		// TODO Auto-generated method stub
		
	}

}
