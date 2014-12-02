package br.unb.cic.bionimbus.client;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.config.BioNimbusConfigLoader;
import br.unb.cic.bionimbus.services.messaging.Message;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PEventType;
import br.unb.cic.bionimbus.p2p.P2PFileEvent;
import br.unb.cic.bionimbus.p2p.P2PListener;
import br.unb.cic.bionimbus.p2p.P2PMessageEvent;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.messages.CloudReqMessage;
import br.unb.cic.bionimbus.p2p.messages.GetRespMessage;
import br.unb.cic.bionimbus.p2p.messages.JobReqMessage;
import br.unb.cic.bionimbus.p2p.messages.ListReqMessage;
import br.unb.cic.bionimbus.p2p.messages.ListRespMessage;
import br.unb.cic.bionimbus.p2p.messages.PrepReqMessage;
import br.unb.cic.bionimbus.p2p.messages.PrepRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreRespMessage;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;

public class Client implements P2PListener {

    private P2PService p2p;

    public void setP2P(P2PService p2p) {
        this.p2p = p2p;
        if (p2p != null)
            p2p.addListener(this);
    }

    public void listServices() {
        Message message = new CloudReqMessage(p2p.getPeerNode());
        p2p.broadcast(message);
    }

    public void startJob(PluginFile file) {

        JobInfo job = new JobInfo();
        job.setId(null);
        job.setArgs("%I1 %O1");
        job.setServiceId(123456);
        job.addInput(file.getId(), file.getSize());
        job.addOutput("output.txt");

        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        jobList.add(job);
        JobReqMessage msg = new JobReqMessage(p2p.getPeerNode(), jobList);
        p2p.broadcast(msg);

    }

    public void uploadFile(String filePath) {
        File file = new File(filePath);

        FileInfo info = new FileInfo();
        info.setName(filePath);
        info.setSize(file.length());

        StoreReqMessage msg = new StoreReqMessage(p2p.getPeerNode(), info, "");
        p2p.broadcast(msg);
    }

    public void listFiles() {
        Message msg = new ListReqMessage(p2p.getPeerNode());
        p2p.broadcast(msg);
    }

    private void recvFile(File file) {
        System.out.println("Arquivo recebido no cliente: " + file.getAbsolutePath());
    }

    @Override
    public void onEvent(P2PEvent event) {
        if (event.getType().equals(P2PEventType.FILE)) {
            recvFile(((P2PFileEvent) event).getFile());
            return;
        } else if (!event.getType().equals(P2PEventType.MESSAGE))
            return;

        P2PMessageEvent msgEvent = (P2PMessageEvent) event;
        Message msg = msgEvent.getMessage();
        if (msg == null)
            return;

        switch (P2PMessageType.of(msg.getType())) {
            case STORERESP:
                StoreRespMessage resp = (StoreRespMessage) msg;
                PluginInfo pluginInfo = resp.getPluginInfo();
                p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
                break;
            case LISTRESP:
                ListRespMessage listMsg = (ListRespMessage) msg;
                System.out.println("TOTAL FILES: " + listMsg.values().size());

                for (PluginFile file : listMsg.values()) {
                    System.out.println("id: " + file.getId() + "\tname: " + file.getPath());
                    startJob(file);
                    break;
                }

                break;
            case GETRESP:
                GetRespMessage getMsg = (GetRespMessage) msg;
                p2p.sendMessage(getMsg.getPluginInfo().getHost(), new PrepReqMessage(p2p.getPeerNode(), getMsg.getPluginFile(), ""));
                break;
            case PREPRESP:
                PrepRespMessage prepMsg = (PrepRespMessage) msg;
                p2p.getFile(prepMsg.getPluginInfo().getHost(), prepMsg.getPluginFile().getPath());
                break;
        }
    }

    public static void main(String[] args) throws Exception {

        String configFile = System.getProperty("config.file", "conf/client.json");
        BioNimbusConfig config = BioNimbusConfigLoader.loadHostConfig(configFile);

        P2PService p2p = new P2PService(config);
        p2p.start();

        Client client = new Client();
        client.setP2P(p2p);

        while (p2p.getPeers().isEmpty()) {
        }

        //System.out.println("I am not alone in the dark anymore!");

        //client.uploadFileToProxy("teste.txt");

        TimeUnit.SECONDS.sleep(120);
        client.listFiles();
    }
}
