package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import com.google.common.base.Joiner;

public class ListServices implements Command {

    public static final String NAME = "services";

    private final SimpleShell shell;

    public ListServices(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {
        if (!shell.isConnected())
            throw new IllegalStateException("This command should be used with an active connection!");

        BioProto proxy = shell.getProxy();
        return Joiner.on("\n").join(proxy.listServices());

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
