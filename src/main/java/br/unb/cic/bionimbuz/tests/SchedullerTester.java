/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.tests;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import br.unb.cic.bionimbuz.avro.rpc.AvroClient;
import br.unb.cic.bionimbuz.avro.rpc.RpcClient;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginService;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService.Path;

/**
 *
 * @author will
 */
public class SchedullerTester {

    private static final Logger LOG = LoggerFactory.getLogger(SchedullerTester.class);
//    private static StringBuilder result = new  StringBuilder();

    private RpcClient rpcClient;
    private CloudMessageService cms;
    private RepositoryService rs;
    private static final Injector injector = Guice.createInjector(new TesterModule());

    public SchedullerTester() {
        initCommunication();
    }

    private void initCommunication() {
        cms = new CuratorMessageService();
        //try {
            //Enumeration<InetAddress> inet = NetworkInterface.getByName("eth0").getInetAddresses();
            String ip = "10.190.60.111";
//            if (ip.equals("")) {
//                while (inet.hasMoreElements()) {
//                    ip = inet.nextElement().toString();
//                }
//            }

            cms.connect(ip + ":2181");

//        } catch (SocketException ex) {
//            java.util.logging.Logger.getLogger(SchedullerTester.class.getName()).log(Level.SEVERE, null, ex);
//        }

        rs = injector.getInstance(RepositoryService.class);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            BioNimbusConfig.get().setZkConnString(InetAddress.getLocalHost().getHostAddress() + ":2181");
            BioNimbusConfig.get().setAddress(InetAddress.getLocalHost().getHostAddress());
            rpcClient = new AvroClient(BioNimbusConfig.get().getRpcProtocol(), BioNimbusConfig.get().getHost().getAddress(), BioNimbusConfig.get().getRpcPort());
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

    /**
     * Trazer o código do CMS foi uma medida paleativa pois o client do Curator
     * estava nulo
     *
     * @param services
     */
    public void addServices(List<PluginService> services) {
        for (PluginService service : services) {
            // create father node
            cms.createZNode(CreateMode.PERSISTENT, Path.NODE_SERVICE.getFullPath(String.valueOf(service.getId())), service.toString());

            // create history structure
            cms.createZNode(CreateMode.PERSISTENT, Path.MODES.getFullPath(String.valueOf(service.getId())), null);

            // add preset mode if there is one
            if (service.getPresetMode() != null) {
                cms.createZNode(CreateMode.PERSISTENT, Path.NODE_MODES.getFullPath(String.valueOf(service.getId()), "0"), service.getPresetMode().toString());
            }
        }
    }

    public void addResources(List<PluginInfo> resources) {
        for (PluginInfo resource : resources) {
            rs.addPeerToZookeeper(resource);
        }
    }

    public void sendJobs(Workflow pipeline) throws InterruptedException, IOException {
        List<br.unb.cic.bionimbuz.avro.gen.Job> listjob = new ArrayList<>();

        for (Job jobInfo : pipeline.getJobs()) {
            br.unb.cic.bionimbuz.avro.gen.Job job = new br.unb.cic.bionimbuz.avro.gen.Job();

            job.setArgs(jobInfo.getArgs());
            job.setId(jobInfo.getId());
            job.setLocalId(BioNimbusConfig.get().getHost().getAddress());
            job.setServiceId(jobInfo.getServiceId());
            job.setTimestamp(jobInfo.getTimestamp());

            ArrayList<br.unb.cic.bionimbuz.avro.gen.FileInfo> avroFiles = new ArrayList<>();
            for (FileInfo f : jobInfo.getInputFiles()) {
                br.unb.cic.bionimbuz.avro.gen.FileInfo file = new br.unb.cic.bionimbuz.avro.gen.FileInfo();
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

        br.unb.cic.bionimbuz.avro.gen.Workflow workflow = new br.unb.cic.bionimbuz.avro.gen.Workflow();
        workflow.setId(pipeline.getId());
        workflow.setJobs(listjob);
        workflow.setCreationDatestamp("00/00/00");
        workflow.setDescription("descricao");
        br.unb.cic.bionimbuz.avro.gen.Sla sla = new br.unb.cic.bionimbuz.avro.gen.Sla();
        sla.setId("SLA-"+pipeline.getId());
        sla.setIdWorkflow(pipeline.getId());
        workflow.setSla(sla);
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

        public SendPipeline(CloudMessageService cms, SchedullerTester st, List<Workflow> remaining, String prevId) {
            this.cms = cms;
            this.st = st;
            this.remaining = remaining;
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

        FromMockFileTestGenerator gen = new FromMockFileTestGenerator(1);
        List<Workflow> pipelines = gen.getPipelinesTemplates();

        List<PluginService> services = gen.getServicesTemplates();
//        List<PluginInfo> resources = gen.getResourceTemplates();

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

        System.out.println("[SchedTester] starting tests with " + pipelines.size() + " pipelines");

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
