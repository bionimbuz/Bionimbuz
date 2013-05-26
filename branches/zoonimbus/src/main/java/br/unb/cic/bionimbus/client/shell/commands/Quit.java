package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;

public class Quit implements Command {

    public static final String NAME = "quit";

    @Override
    public String execute(String... params) {
        System.exit(0);
        return null;
    }

    @Override
    public String usage() {
        return "quit";
    }

    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
        // TODO Auto-generated method stub

    }
}
