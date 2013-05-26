package br.unb.cic.bionimbus.client.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.shell.commands.SyncCommunication;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.JobReqMessage;
import br.unb.cic.bionimbus.p2p.messages.JobRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.plugin.PluginInfo;

public class CloserTool {

    private static final Logger LOG = LoggerFactory.getLogger(CloserTool.class);

    private int getFileNumLines(File file) throws Exception {
        int numLines = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        while (br.readLine() != null) {
            numLines++;
        }
        br.close();
        return numLines;
    }

    private void writeSmallerFiles(File file, int numLines) throws Exception {
        int numReads = numLines / 4;
        int readChunk = (numReads / 3);
        BufferedWriter w1 = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath() + ".1")));
        BufferedWriter w2 = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath() + ".2")));

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        int count = 0;
        int readsWritten = 0;
        while ((readsWritten < readChunk * 2)
                && ((line = br.readLine()) != null)) {
            if (readsWritten < readChunk)
                w1.write(line + "\n");
            w2.write(line + "\n");

            count++;
            if (count == 4) {
                readsWritten++;
                count = 0;
            }
        }
        br.close();
        w1.close();
        w2.close();
    }

    private void sendFile(P2PService p2p, SyncCommunication comm, File file) throws Exception {
        FileInfo info = new FileInfo();
        info.setName(file.getName());
        info.setSize(file.length());
        LOG.info("Enviando arquivo " + info.getName() + " de tamanho " + info.getSize() + " bytes.");

        comm.sendReq(new StoreReqMessage(p2p.getPeerNode(), info, ""), P2PMessageType.STORERESP);
        StoreRespMessage resp = (StoreRespMessage) comm.getResp();
        PluginInfo pluginInfo = resp.getPluginInfo();
        p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
    }

    public void uploadFiles(String filename) throws Exception {
        File file = new File(filename);
        int numLines = getFileNumLines(file);
        writeSmallerFiles(file, numLines);

        String configFile = System.getProperty("config.file", "conf/client.json");
        BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);

        P2PService p2p = new P2PService(config);
        p2p.start();
        TimeUnit.SECONDS.sleep(40);
        SyncCommunication comm = new SyncCommunication(p2p);

        File file1 = new File(file.getAbsolutePath() + ".1");
        File file2 = new File(file.getAbsolutePath() + ".2");

        sendFile(p2p, comm, file);
        sendFile(p2p, comm, file1);
        sendFile(p2p, comm, file2);
    }

    public void startJobs(int numJobs, String idFull, String idMedium, String idSmall) throws Exception {
        String configFile = System.getProperty("config.file", "conf/client.json");
        BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);

        P2PService p2p = new P2PService(config);
        p2p.start();
        TimeUnit.SECONDS.sleep(40);
        SyncCommunication comm = new SyncCommunication(p2p);

        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        for (int i = 0; i < numJobs; i++) {
            JobInfo job = new JobInfo();
            job.setId(null);
            job.setServiceId(1001);
            job.setArgs("%O1 e_coli %I1");
            if ((i % 3) == 0)
                job.addInput(idFull, Long.valueOf(0));
            else if ((i % 3) == 1)
                job.addInput(idMedium, Long.valueOf(0));
            else if ((i % 3) == 2)
                job.addInput(idSmall, Long.valueOf(0));
            job.addOutput("output-" + i + ".txt");
            jobList.add(job);
        }

        LOG.info("Enviando " + jobList.size() + " jobs.");
        comm.sendReq(new JobReqMessage(p2p.getPeerNode(), jobList), P2PMessageType.JOBRESP);
        JobRespMessage resp = (JobRespMessage) comm.getResp();
        LOG.info("Job " + resp.getJobInfo().getId() + " started succesfully");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CloserTool tool = new CloserTool();

        if (args[0].equals("upload")) {
            tool.uploadFiles(args[1]);
        } else if (args[0].equals("run")) {
            int numJobs = Integer.parseInt(args[1]);
            String id1 = args[2];
            String id2 = args[3];
            String id3 = args[4];
            tool.startJobs(numJobs, id1, id2, id3);
        }
    }

}
