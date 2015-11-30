package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class JobStart implements Command {

    public static final String NAME = "start";

    private final SimpleShell shell;

    public JobStart(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (!shell.isConnected())
            throw new IllegalStateException("This command should be used with an active connection!");

        StringBuilder param = new StringBuilder();
        for(String arg : params){
            param.append(arg);
            param.append(" ");
        }
        
        BioProto proxy = shell.getProxy();
        return proxy.startJobName(param.toString(), shell.getIp());
    }

    @Override
    public String usage() {
        return NAME + " <serviceId> [args [-i inputs] [-o outputs]]";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
