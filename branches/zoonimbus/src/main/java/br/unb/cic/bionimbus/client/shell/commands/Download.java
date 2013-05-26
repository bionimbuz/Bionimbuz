package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;

public class Download implements Command {

    public static final String NAME = "download";

    @Override
    public String execute(String... params) throws Exception {
        return "not implemented yet";
    }

    @Override
    public String usage() {
        return NAME + " <fileId>";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
