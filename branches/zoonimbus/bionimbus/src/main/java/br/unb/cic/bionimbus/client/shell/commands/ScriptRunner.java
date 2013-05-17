package br.unb.cic.bionimbus.client.shell.commands;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class ScriptRunner implements Command {

	private static final String NAME = "script";
	private final SimpleShell shell;

	public ScriptRunner(SimpleShell shell) {
		this.shell = shell;
	}

	@Override
	public String execute(String... params) throws Exception {
		for (String script : params) {
			if (script.endsWith(".nimbus")) {
				shell.print(String.format("executing %s\n", script));
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(script)));
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.length() != 0 && !line.startsWith("#")) {
						shell.executeCommand(line, false);
					}
				}
			}
		}
		return "";
	}

	@Override
	public String usage() {
		return "script file.nimbus";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void setOriginalParamLine(String param) {
	}

}
