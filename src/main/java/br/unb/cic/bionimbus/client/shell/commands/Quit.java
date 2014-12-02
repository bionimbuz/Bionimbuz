package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Quit implements Command {

    public static final String NAME = "quit";
    private final SimpleShell shell;

    public Quit(SimpleShell shell) {
        this. shell = shell;
    }

    
    
    @Override
    public String execute(String... params) {
        System.exit(0);
        try {
            shell.close();
        } catch (IOException ex) {
            Logger.getLogger(Quit.class.getName()).log(Level.SEVERE, null, ex);
        }
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
