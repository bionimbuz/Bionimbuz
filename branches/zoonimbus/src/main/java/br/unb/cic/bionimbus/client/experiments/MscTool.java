package br.unb.cic.bionimbus.client.experiments;

import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.gen.Pair;
import br.unb.cic.bionimbus.avro.rpc.AvroClient;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.services.storage.Ping;
import br.unb.cic.bionimbus.utils.Put;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MscTool {

    private static final Logger LOG = LoggerFactory.getLogger(MscTool.class);

    RpcClient rpcClient = new AvroClient("http", "localhost", 9999);
//    private P2PService p2p;
//
//    private SyncCommunication communication;

    private List<String> readFileNames() throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("/home/zoonimbus/NetBeansProjects/zoonimbus/data-folder/inputfiles.txt"));
        String line;
        while ((line = br.readLine()) != null)
            list.add(line);
        return list;
    }

    private void uploadFile(String name) throws IOException, InterruptedException, Exception{
        File file = new File(name);
        FileInfo info = new FileInfo();
        info.setName(file.getName());
        info.setSize(file.length());

        LOG.info("uploading " + name + ": " + file.length() + " bytes");

//        rpcClient.getProxy().
//        communication.sendReq(new StoreReqMessage(p2p.getPeerNode(), info, ""), P2PMessageType.STORERESP);
//        StoreRespMessage resp = (StoreRespMessage) communication.getResp();
//        PluginInfo pluginInfo = resp.getPluginInfo();
//
//        LOG.info("uploading to plugin " + pluginInfo.getId() + " at " + pluginInfo.getHost().getAddress());
//        p2p.sendFile(pluginInfo.getHost(), resp.getFileInfo().getName());
        List<NodeInfo> nodesdisp = new ArrayList<NodeInfo>();

        List<NodeInfo> pluginList = rpcClient.getProxy().getPeersNode();
        rpcClient.getProxy().setFileInfo(info,"uploadTesteMscTool");
        for (Iterator<NodeInfo> it = pluginList.iterator(); it.hasNext();) {
            NodeInfo plugin = it.next();
            Float prioridade = plugin.getFreesize()*new Float("0,9");
            if (prioridade>info.getSize()){
                plugin.setLatency(Ping.calculo(plugin.getAddress()));
                nodesdisp.add(plugin);
            }    
        }
        //Retorna a lista dos nos ordenados como melhores, passando a latência calculada
        nodesdisp = new ArrayList<NodeInfo>(rpcClient.getProxy().callStorage(nodesdisp)); 

        NodeInfo no=null;
        Iterator<NodeInfo> it = nodesdisp.iterator();
        while (it.hasNext() && no == null) {
                NodeInfo node = (NodeInfo)it.next();

            Put conexao = new Put(node.getAddress(),file.getPath());                
                if(conexao.startSession()){
                    no = node;
                }
            }
        if(no != null){
            List<String> dest = new ArrayList<String>();
            dest.add(no.getPeerId());
            nodesdisp.remove(no);

            rpcClient.getProxy().fileSent(info,dest);
            rpcClient.getProxy().transferFile(nodesdisp,info.getName(),2,dest);
        }
    }

    public void uploadFiles() throws IOException, InterruptedException,Exception {
        List<String> fileNames = readFileNames();
        for (String name : fileNames) {
            uploadFile(name);
        }
    }

    private PluginFile getPluginFile(String file, Collection<PluginFile> cloudFiles) throws FileNotFoundException {
        for (PluginFile pluginFile : cloudFiles)
            if (pluginFile.getName().equals(file))
                return pluginFile;
        throw new FileNotFoundException(file);
    }

    private List<Pipeline> getPipelines() throws IOException, InterruptedException {
        Collection<PluginFile> cloudFiles = listCloudFiles();
        List<Pipeline> list = new ArrayList<Pipeline>();
        for (String file : readFileNames()) {
            Pipeline pipeline = new MscPipeline(getPluginFile(file, cloudFiles));
            list.add(pipeline);
        }
        return list;
    }

    public void runJobs() throws IOException, InterruptedException {
        
        List<Pipeline> list = getPipelines();
        List<Pipeline> sending = new ArrayList<Pipeline>(list);


        while (!list.isEmpty()) {

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

            TimeUnit.SECONDS.sleep(5);
        }

        LOG.info("test concluded!");
    }

    private Collection<PluginFile> listCloudFiles() throws InterruptedException ,IOException{
//        communication.sendReq(new ListReqMessage(p2p.getPeerNode()), P2PMessageType.LISTRESP);
//        ListRespMessage listResp = (ListRespMessage) communication.getResp();
        Collection<PluginFile> collection = new ArrayList<PluginFile> ();
        for(br.unb.cic.bionimbus.avro.gen.PluginFile info : rpcClient.getProxy().listFiles()){
            PluginFile file = new PluginFile();
            file.setId(info.getId());
            file.setName(info.getName());
            file.setPath(info.getPath());
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
            job.setLocalId("");
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
        rpcClient.getProxy().startJob(listjob);
//        LOG.info("job " + resp.getJobInfo().getId() + " sent succesfully...");
    }

    public void printResult() {

    }

    public static void main(String[] args) {
        MscTool tool = new MscTool();
        try {
            //tool.uploadFiles();
            tool.runJobs();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tool.printResult();
    }
}
