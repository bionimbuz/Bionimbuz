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

/*        if (!shell.isConnected())
            throw new IllegalStateException(
                    "This command should be used with an active connection!");

        P2PService p2p = shell.getP2P();
        SyncCommunication comm = new SyncCommunication(p2p);
        shell.print("Canceling job...");

        comm.sendReq(new JobCancelReqMessage(p2p.getPeerNode(), params[0]), P2PMessageType.JOBCANCELRESP);
        JobCancelRespMessage resp = (JobCancelRespMessage) comm.getResp();

        return "Job " + resp.getJobId() + " succesfully canceled";*/
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
