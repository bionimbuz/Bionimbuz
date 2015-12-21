package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class JobCancel implements Command {

    public static final String NAME = "cancel";

    private final SimpleShell shell;

    public JobCancel(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (!shell.isConnected())
            throw new IllegalStateException("This command should be used with an active connection!");

        String jobID = params[0];
        BioProto proxy = shell.getProxy();
        return proxy.cancelJob(jobID);
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
