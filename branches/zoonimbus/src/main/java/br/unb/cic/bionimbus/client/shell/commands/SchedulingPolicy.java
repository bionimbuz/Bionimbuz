package br.unb.cic.bionimbus.client.shell.commands;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;

public class SchedulingPolicy implements Command {

    public static final String NAME = "policy";

    private final SimpleShell shell;

    public SchedulingPolicy(SimpleShell shell) {
        this.shell = shell;
    }

    @Override
    public String execute(String... params) throws Exception {

        if (!shell.isConnected())
            throw new IllegalStateException("This command should be used with an active connection!");

        BioProto proxy = shell.getProxy();
        Integer policy = new Integer(params[0]);
        
        if(policy!=-1 && (policy < -1 || policy >= SchedPolicy.getInstances().size())){
            return "\nNúmero da política de escalonamento inválido.\n\n"+proxy.schedPolicy(-1);
        }
        
            
        return proxy.schedPolicy(policy);
        

    }

    @Override
    public String usage() {
        return NAME + " <numeroPolicy> \n"+NAME+" <-1>";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOriginalParamLine(String param) {
    }

}
