package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;

public class JobStatus implements Command {

    public static final String NAME = "status";

    private final SimpleShell shell;

    public JobStatus(SimpleShell shell) {
        this.shell = shell;
    }
    
    @Override
    public String execute(String... params) throws Exception {
        
        BioProto rpc = shell.getProxy();
        
        // verifica se é a solicitação de status para todos os jobs
        if(params[0].equalsIgnoreCase("all")){
            return rpc.statusAllJob();
        }else{
            return rpc.statusJob(params[0]);
        }
        
        
    }

    @Override
    public String usage() {
        return NAME + " <jobId> \n"+NAME+" ALL";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
