package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;

public class JobList implements Command {

    public static final String NAME = "jobs";

    @Override
    public String execute(String... params) throws Exception {
        return "not implemented yet";
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
