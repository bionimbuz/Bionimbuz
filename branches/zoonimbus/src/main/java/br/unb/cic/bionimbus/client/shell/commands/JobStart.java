package br.unb.cic.bionimbus.client.shell.commands;

import java.util.ArrayList;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.shell.Command;
import br.unb.cic.bionimbus.client.shell.SimpleShell;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.JobReqMessage;
import br.unb.cic.bionimbus.p2p.messages.JobRespMessage;
import com.google.common.base.Joiner;

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

        String jobID = params[0];

        BioProto proxy = shell.getProxy();
        return proxy.startJob(jobID);

/*        if (!shell.isConnected())
            throw new IllegalStateException(
                    "This command should be used with an active connection!");

        P2PService p2p = shell.getP2P();
        SyncCommunication comm = new SyncCommunication(p2p);
        int i = 0;

        shell.print("Starting job...");

        JobInfo job = new JobInfo();
        job.setId(null);
        job.setServiceId(Long.parseLong(params[0]));
        i++;

        while (i < params.length) {
            if (i == 1) {
                job.setArgs(params[i]);
                i++;
            } else if (params[i].equals("-i")) {
                i++;
                while (i < params.length && !params[i].equals("-o")) {
                    job.addInput(params[i], Long.valueOf(0));
                    i++;
                }
            } else if (params[i].equals("-o")) {
                i++;
                while (i < params.length) {
                    job.addOutput(params[i]);
                    i++;
                }
            }
        }

        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        jobList.add(job);
        comm.sendReq(new JobReqMessage(p2p.getPeerNode(), jobList), P2PMessageType.JOBRESP);
        JobRespMessage resp = (JobRespMessage) comm.getResp();

        if (resp.getJobInfo() == null) {
            return "Unavailable service for job.";
        }

        return "Job " + resp.getJobInfo().getId() + " started succesfully";*/
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
