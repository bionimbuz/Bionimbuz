/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.gen.Pair;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.experiments.MscPipeline;
import br.unb.cic.bionimbus.client.experiments.MscTool;
import br.unb.cic.bionimbus.client.experiments.Pipeline;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Put;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
    private BioNimbusConfig config ;

    public SchedullerTester() {
        initCommunication();
    }

    private void initCommunication() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        
        try {
            config = mapper.readValue(new File("conf/node.yaml"), BioNimbusConfig.class);
            rpcClient = new AvroClient(config.getRpcProtocol(), config.getHost().getAddress(), config.getRpcPort());
            if(rpcClient.getProxy().ping())
                LOG.info("client is connected.");
            
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
        tester.printResult();
    }
    
    public void runJobs() throws IOException, InterruptedException {
        long timeInit = System.currentTimeMillis();
        List<Pipeline> list = getPipelines();
        List<Pipeline> sending = new ArrayList<Pipeline>(list);


        while (!list.isEmpty()) {

            // put 4 jobs in a job list
            // the jobs are the first job of the 4 first pipelines of sending
            // remove pipeline after first job is added
            int count = 0;
            List<JobInfo> jobs = new ArrayList<JobInfo>();
            List<Pipeline> sendAux = new ArrayList<Pipeline>(sending);
            for (Pipeline pipeline : sendAux) {
                JobInfo job = pipeline.firstJob();
                if (job != null) {
                    jobs.add(job);
                    sending.remove(pipeline);
                    if (++count >= 4) {
                        sendJobs(jobs);
                        TimeUnit.MINUTES.sleep(1);
                        break;
                    }
                }
            }

            if ((count > 0) && (count < 4))
                sendJobs(jobs);

            Collection<PluginFile> files = listCloudFiles();
            List<Pipeline> auxList = new ArrayList<Pipeline>(list);
            
            // send next job of a pipeline after the first is done
            for (Pipeline pipeline : auxList) {
                String file = pipeline.getCurrentOutput();
                if (file == null)
                    continue;
                for (PluginFile pluginFile : files) {
                    if (!pluginFile.getName().equals(file))
                        continue;
                    JobInfo job = pipeline.nextJob(pluginFile);
                    if (job != null) {
                        List<JobInfo> jobList = new ArrayList<JobInfo>();
                        jobList.add(job);
                        sendJobs(jobList);
                    } else {
                        LOG.info("pipeline " + pipeline.getInput() + " finalized...");
                        list.remove(pipeline);
                    }
                }
            }

            TimeUnit.SECONDS.sleep(10);
        }
        long timeExec = (System.currentTimeMillis() - timeInit)/1000;
//        result.append("\nTempo de execução :").append(result);
        LOG.info("Pipeline - Tempo de envio para execução: "+timeExec);
        try {
            rpcClient.close();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MscTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // generate pipeline list
    private List<Pipeline> getPipelines() throws IOException, InterruptedException {
        Collection<PluginFile> cloudFiles = listCloudFiles();
        List<Pipeline> list = new ArrayList<Pipeline>();
        for (String file : readFileNames()) {
            Pipeline pipeline = new MscPipeline(getPluginFile(file, cloudFiles));
            list.add(pipeline);
        }
        return list;
    }
    
    // get some sort of list of strings needed to create a pipeline
    private List<String> readFileNames() throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        String pathHome = System.getProperty("user.dir");
        String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
        here------------------------------------------------------------vvv
        BufferedReader br = new BufferedReader(new FileReader(path+"inputfiles.txt"));
        String line;
        while ((line = br.readLine()) != null)
            list.add(line);
        return list;
    }

    private PluginFile getPluginFile(String file, Collection<PluginFile> cloudFiles) throws FileNotFoundException {
        for (PluginFile pluginFile : cloudFiles)
            if (pluginFile.getName().equals(file))
                return pluginFile;
        throw new FileNotFoundException(file);
    }

    // list all files on the cloud, provided by the storage service
    private Collection<PluginFile> listCloudFiles() throws InterruptedException ,IOException{
//        communication.sendReq(new ListReqMessage(p2p.getPeerNode()), P2PMessageType.LISTRESP);
//        ListRespMessage listResp = (ListRespMessage) communication.getResp();
        Collection<PluginFile> collection = new ArrayList<PluginFile> ();
        for(br.unb.cic.bionimbus.avro.gen.PluginFile info : rpcClient.getProxy().listFiles()){
            PluginFile file = new PluginFile();
            file.setId(info.getId());
            file.setName(info.getName());
            file.setPath("data-folder/"+info.getPath());
            file.setPluginId(info.getPluginId());
            file.setSize(info.getSize());
            collection.add(file);
        }
        return collection;
    }

    private void sendJobs(List<JobInfo> jobs) throws InterruptedException,IOException {
//        communication.sendReq(new JobReqMessage(p2p.getPeerNode(), jobs), P2PMessageType.JOBRESP);
//        JobRespMessage resp = (JobRespMessage) communication.getResp();
        List<br.unb.cic.bionimbus.avro.gen.JobInfo> listjob = new ArrayList<br.unb.cic.bionimbus.avro.gen.JobInfo>();
        for(JobInfo jobInfo : jobs){
            br.unb.cic.bionimbus.avro.gen.JobInfo job = new br.unb.cic.bionimbus.avro.gen.JobInfo();
            job.setArgs(jobInfo.getArgs());
            job.setId(jobInfo.getId());
            job.setLocalId(config.getHost().getAddress());
            job.setServiceId(jobInfo.getServiceId());
            job.setTimestamp(jobInfo.getTimestamp());
            List<Pair> listPair =  new ArrayList<Pair>();
            for(br.unb.cic.bionimbus.utils.Pair<String,Long> pairInfo : jobInfo.getInputs()){
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

    public void printResult() {

    }
}
