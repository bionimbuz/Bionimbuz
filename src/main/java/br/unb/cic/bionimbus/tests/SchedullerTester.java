package br.unb.cic.bionimbus.tests;

import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.model.Job;
import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.model.FileInfo;
import br.unb.cic.bionimbus.model.WorkflowStatus;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import br.unb.cic.bionimbus.services.RepositoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author will
 */
public class SchedullerTester {

    private static final Logger LOG = LoggerFactory.getLogger(SchedullerTester.class);
//    private static StringBuilder result = new  StringBuilder();

    private RpcClient rpcClient;
    private BioNimbusConfig config;
    private CloudMessageService cms;
    private RepositoryService rs;

    public SchedullerTester() {
        initCommunication();
    }

    private void initCommunication() {
        cms = new CuratorMessageService();
        try {
            Enumeration<InetAddress> inet = NetworkInterface.getByName("eth0").getInetAddresses();
            String ip = "164.41.209.89";
//            if (ip.equals("")) {
//                while (inet.hasMoreElements()) {
//                    ip = inet.nextElement().toString();
//                }
//            }

            cms.connect(ip + ":2181");
        } catch (SocketException ex) {
            java.util.logging.Logger.getLogger(SchedullerTester.class.getName()).log(Level.SEVERE, null, ex);
        }
        rs = new RepositoryService(cms);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            config = mapper.readValue(new File("conf/node.yaml"), BioNimbusConfig.class);
            config.setZkConnString(InetAddress.getLocalHost().getHostAddress() + ":2181");
            config.setAddress(InetAddress.getLocalHost().getHostAddress());
            rpcClient = new AvroClient(config.getRpcProtocol(), config.getHost().getAddress(), config.getRpcPort());
            if (rpcClient.getProxy().ping()) {
                LOG.info("client is connected.");
            }

        } catch (IOException ex) {
            LOG.error("IOException - SchedullerTester");
            ex.printStackTrace();
        } catch (Exception ex) {
            LOG.error("Exception - SchedullerTester");
            ex.printStackTrace();
        }

    }

    public void addServices(List<PluginService> services) {
        for (PluginService service : services) {
            rs.addServiceToZookeeper(service);
        }
    }

    public void addResources(List<PluginInfo> resources) {
        for (PluginInfo resource : resources) {
            rs.addPeerToZookeeper(resource);
        }
    }

    public void sendJobs(Workflow pipeline) throws InterruptedException, IOException {
        List<br.unb.cic.bionimbus.avro.gen.Job> listjob = new ArrayList<>();

        for (Job jobInfo : pipeline.getJobs()) {
            br.unb.cic.bionimbus.avro.gen.Job job = new br.unb.cic.bionimbus.avro.gen.Job();

            job.setArgs(jobInfo.getArgs());
            job.setId(jobInfo.getId());
            job.setLocalId(config.getHost().getAddress());
            job.setServiceId(jobInfo.getServiceId());
            job.setTimestamp(jobInfo.getTimestamp());

            ArrayList<br.unb.cic.bionimbus.avro.gen.FileInfo> avroFiles = new ArrayList<>();
            for (FileInfo f : jobInfo.getInputFiles()) {
                br.unb.cic.bionimbus.avro.gen.FileInfo file = new br.unb.cic.bionimbus.avro.gen.FileInfo();
                file.setHash(f.getHash());
                file.setId(f.getId());
                file.setName(f.getName());
                file.setUploadTimestamp(f.getUploadTimestamp());
                file.setUserId(f.getUserId());

                avroFiles.add(file);
            }

            job.setInputFiles(avroFiles);
            job.setOutputs(jobInfo.getOutputs());
            job.setDependencies(jobInfo.getDependencies());

            listjob.add(job);
        }

        br.unb.cic.bionimbus.avro.gen.Workflow workflow = new br.unb.cic.bionimbus.avro.gen.Workflow();
        workflow.setId(pipeline.getId());
        workflow.setJobs(listjob);
        workflow.setCreationDatestamp("00/00/00");
        workflow.setDescription("descricao");

        rpcClient.getProxy().startWorkflow(workflow);
    }

    public static class ShowSchedResults implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            System.out.println(event);
        }
    }

    public static class SendPipeline implements Watcher {

        private final CloudMessageService cms;
        private final SchedullerTester st;
        private final List<Workflow> remaining;
        private final String prevId;

        public SendPipeline(CloudMessageService cms, SchedullerTester st, List<Workflow> remaining, String prevId) {
            this.cms = cms;
            this.st = st;
            this.remaining = remaining;
            this.prevId = prevId;
        }

        /**
         * Recebe as notificações de evento do zookeeper.
         *
         * @param event evento que identifica a mudança realizada no zookeeper
         */
        @Override
        public void process(WatchedEvent event) {
            System.out.println("[SentPipeline] Event got: " + event.toString());
            switch (event.getType()) {
                case NodeDeleted:
                    try {
                        if (!remaining.isEmpty()) {
                            Workflow pipeline = remaining.get(0);

                            // send new pipeline
                            st.sendJobs(pipeline);

                            // set new watcher with remaining pipelines
                            remaining.remove(pipeline);
                            System.out.println("[SentPipeline] Pipeline " + pipeline.getId() + " sent, " + remaining.size() + " remaining");
                            while (!cms.getZNodeExist(Path.NODE_PIPELINE.getFullPath(pipeline.getId()), null)) {
                            }
                            cms.getChildren(Path.NODE_PIPELINE.getFullPath(pipeline.getId()), new SendPipeline(cms, st, remaining, pipeline.getId()));
                        } else {
                            System.out.println("[SentPipeline] No more pipelines");
                        }
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(SchedullerTester.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(SchedullerTester.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                default:
                    System.out.println("[SendPipeline] Received other event: " + event.getPath());

            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        SchedullerTester tester = new SchedullerTester();
        boolean fileTest = false;

        FromMockFileTestGenerator gen = new FromMockFileTestGenerator();
        List<Workflow> pipelines = gen.getPipelinesTemplates();
        List<PluginService> services = gen.getServicesTemplates();
        List<PluginInfo> resources = gen.getResourceTemplates();

        // flush test data
//            System.out.println("[SchedTester] flushing test data");
//            PrintWriter pwr = new PrintWriter("pipelines.txt", "UTF-8");
//            for (Workflow p : pipelines)
//                pwr.println(p.toString());
//            PrintWriter swr = new PrintWriter("services.txt", "UTF-8");
//            for (PluginService s : services)
//                swr.println(s.toString());
//            PrintWriter rwr = new PrintWriter("resources.txt", "UTF-8");
//            for (PluginInfo r : resources)
//                rwr.println(r.toString());
        // add data to zookeeper
        tester.addServices(services);
//        tester.addResources(resources);

        System.out.println("[SchedTester] starting testing with " + pipelines.size() + " pipelines");

        // perform all tests
        Workflow fst = pipelines.get(0);
        fst.setId("teste");
        fst.setCreationDatestamp("00/00/00");
        fst.setDescription("descricao");

        tester.sendJobs(fst);
        pipelines.remove(fst);
        System.out.println("[SchedTester] First pipeline " + fst.getId() + " with " + fst.getJobs().size() + " jobs sent, " + pipelines.size() + " remaining");

        // busy waiting to wait for node to exists
        while (!tester.cms.getZNodeExist(Path.NODE_PIPELINE.getFullPath(fst.getId()), null)) {
            System.out.println("[SchedTester] waiting node creation");
        }
        tester.cms.getChildren(Path.NODE_PIPELINE.getFullPath(fst.getId()), new SendPipeline(tester.cms, tester, pipelines, fst.getId()));

        System.out.println("[SchedTester] waiting forever");
        while (true) {
        }
    }
}
