/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.avro.gen.Pair;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.experiments.MscTool;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final Logger LOG = LoggerFactory.getLogger(MscTool.class);
//    private static StringBuilder result = new  StringBuilder();

    private RpcClient rpcClient;
    private BioNimbusConfig config;

    public SchedullerTester() {
        initCommunication();
    }

    private void initCommunication() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            config = mapper.readValue(new File("conf/node.yaml"), BioNimbusConfig.class);
            rpcClient = new AvroClient(config.getRpcProtocol(), config.getHost().getAddress(), config.getRpcPort());
            if (rpcClient.getProxy().ping()) {
                LOG.info("client is connected.");
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MscTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MscTool.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        SchedullerTester tester = new SchedullerTester();
        try {
            //tool.uploadFiles();
            tester.runJobs();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        tester.printResult();
    }

    public void runJobs() throws IOException, InterruptedException {

        long timeInit = System.currentTimeMillis();
        CuratorMessageService cms = new CuratorMessageService();

        // get pipeline from file
        List<JobInfo> pipeline = getPipelineFromFile();
        
        for(JobInfo j : pipeline) {
            System.out.println(j.toString());
        }

        // send pipeline for execution
        sendJobs(pipeline);

        // get results when ready
        List<String> peers = cms.getChildren(cms.getPath().PEERS.toString(), null);
        for (String path : peers) {
            cms.getChildren(path + Path.SCHED, new ShowSchedResults());
        }

        wait();
    }
    
    private List<JobInfo> getPipelineFromFile() throws FileNotFoundException, IOException {
        
        JobInfo taskList[];
        
        // get pipeline file path
        String pathHome = System.getProperty("user.dir");
        String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
        BufferedReader br = new BufferedReader(new FileReader(path+"pipelineSample.txt"));
        
        // get first line: number of tasks
        String line = br.readLine();
        int tasksNumber = Integer.parseInt(line);
        taskList = new JobInfo[tasksNumber];
        
        // get next tasksNumber lines: each task
        for (int i=0; i<tasksNumber; i++) {
            // generate a new jobInfo from json
            line = br.readLine();
            JobInfo jobInfo = new JobInfo();
            jobInfo.setTimestamp(0l);
            
            // set serviceId from json
            int lastComa = line.indexOf(",");
            jobInfo.setServiceId(Long.parseLong(line.substring(line.indexOf("serviceId:")+10, lastComa)));
            
            // set args from json
            lastComa = line.indexOf(",", lastComa+1);
            jobInfo.setArgs(line.substring(line.indexOf("args:")+5, lastComa));
            
            // get input list from json
            int lastBracket = line.indexOf("]");
            String io = line.substring(line.indexOf("inputs:[")+8, lastBracket);
            String inputs[] = io.split(",");
            
            // set inputs
            // TODO: change addInput to receive the filename instead of its zookeeper id
            for (String inp : inputs)
                jobInfo.addInput(inp, 0l);
            
            // get output list from json
            lastBracket = line.indexOf("]", lastBracket+1);
            io = line.substring(line.indexOf("outputs:[")+9, lastBracket);
            String outputs[] = io.split(",");
            
            // set outputs
            for (String out : outputs)
                jobInfo.addOutput(out);
            
            // put it into the map to, furthermore, set the dependencies
            taskList[i] = jobInfo;
        }
        
        // get the remaining lines: dependency matrix
        for (int i=0; i<tasksNumber; i++) {
            String deps[] = br.readLine().split(",");
            for (int j=0; j<tasksNumber; j++) {
                if (Integer.parseInt(deps[j]) == 1) {
                    taskList[i].addDependency(taskList[j].getId());
                }
            }
        }
        
        // get task list with dependencies        
        return new ArrayList<JobInfo>(Arrays.asList(taskList));
    }
    
    private void sendJobs(List<JobInfo> jobs) throws InterruptedException, IOException {
//        communication.sendReq(new JobReqMessage(p2p.getPeerNode(), jobs), P2PMessageType.JOBRESP);
//        JobRespMessage resp = (JobRespMessage) communication.getResp();
        List<br.unb.cic.bionimbus.avro.gen.JobInfo> listjob = new ArrayList<br.unb.cic.bionimbus.avro.gen.JobInfo>();
        for (JobInfo jobInfo : jobs) {
            br.unb.cic.bionimbus.avro.gen.JobInfo job = new br.unb.cic.bionimbus.avro.gen.JobInfo();
            job.setArgs(jobInfo.getArgs());
            job.setId(jobInfo.getId());
            job.setLocalId(config.getHost().getAddress());
            job.setServiceId(jobInfo.getServiceId());
            job.setTimestamp(jobInfo.getTimestamp());
            List<Pair> listPair = new ArrayList<Pair>();
            for (br.unb.cic.bionimbus.utils.Pair<String, Long> pairInfo : jobInfo.getInputs()) {
                Pair pair = new Pair();
                pair.first = pairInfo.first;
                pair.second = pairInfo.second;
                listPair.add(pair);
            }
            job.setInputs(listPair);
            job.setOutputs(jobInfo.getOutputs());

            listjob.add(job);
        }

        rpcClient.getProxy().startJob(listjob, "");
    }

    public static class ShowSchedResults implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            System.out.println(event);
        }
    }
}
