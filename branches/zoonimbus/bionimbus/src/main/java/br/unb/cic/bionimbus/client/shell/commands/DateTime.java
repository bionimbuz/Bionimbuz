package br.unb.cic.bionimbus.client.shell.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.unb.cic.bionimbus.client.shell.Command;

public class DateTime implements Command {
	
	public static final String NAME = "date";

	@Override
	public String execute(String... params) {
		String currDate = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date());
		return currDate;
	}

	@Override
	public String usage() {
		return "date";
	}
	
	public String getName() {
		return NAME;
	}

	@Override
	public void setOriginalParamLine(String param) {
		
	}
	
}
