package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;

public class Echo implements Command {

    public static final String NAME = "echo";
    private String line;

    @Override
    public String execute(String... params) throws Exception {
        int index = line.trim().indexOf(NAME) + NAME.length();
        return line.substring(index).trim();
    }

    @Override
    public String usage() {
        return NAME;
    }

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public void setOriginalParamLine(String param) {
        this.line = param;
    }

}
