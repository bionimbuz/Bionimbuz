package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.JobCancel;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.Plugin;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.avro.AvroRemoteException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;

public class BioProtoImpl implements BioProto {

    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final SchedService schedService;
    private final ZooKeeperService zkService;
    private  List<NodeInfo> node;
    @Inject
    public BioProtoImpl(DiscoveryService discoveryService, StorageService storageService, SchedService schedService, ZooKeeperService zkservice) {
        this.discoveryService = discoveryService;
        this.storageService = storageService;
        this.schedService = schedService;
        this.zkService =  zkservice;
    }

    public boolean ping() throws AvroRemoteException {
        return true;
    }

    @Override
    public List<String> listFiles() throws AvroRemoteException {
        File dataFolder = storageService.getDataFolder();
        return Arrays.asList(dataFolder.list());
    }

    @Override
    public List<String> listServices() throws AvroRemoteException {
        //TODO: call storageService
        return asList("blast", "interpro", "bowtie");
    }

    @Override
    public String startJob(String jobID) throws AvroRemoteException {

        JobInfo job = new JobInfo();
        job.setId(null);
        job.setServiceId(Long.parseLong(jobID));

        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        jobList.add(job);
        
        schedService.getPolicy().schedule(jobList, zkService);
        return "Job Executado";
    }

    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
        //TODO: call schedService
        return "OK";
    }

    @Override
    public List<NodeInfo> getPeers() throws AvroRemoteException {
        NodeInfo nodeaux =new NodeInfo();
        //for(PluginInfo info : discoveryService.getPeers())
        for(PluginInfo info : discoveryService.getPeers().values()){
           //esta setando nulo
           if(info!=null){
                nodeaux.setAddress(info.getHost().getAddress());

                nodeaux.setPeerId(info.getId());
                node.add(nodeaux);
           }
        }
        return node;
    }
    //Set the nodes from the clients with latency
    @Override
    public Void setNodes(List<NodeInfo> list) throws AvroRemoteException {
        this.node=list;       
            PluginInfo plugin;
               for(NodeInfo nodeSeted : this.node){
                    //setar o valor na map com o retorno do node
                     plugin=discoveryService.getPeers().get(nodeSeted.getAddress());
                     if(nodeSeted.getLatency()!=null)
                        plugin.setLatency(nodeSeted.getLatency());
                }                       
      return null;
    }

//        public Void sendPlugins(List<NodeInfo> plugins) throws AvroRemoteException {
//        
//        List<? extends Plugin> pluginList = Lists.transform(plugins, new Function<NodeInfo, Plugin>() {
//            
//            private ObjectMapper objectMapper = new ObjectMapper();
//            
//            @Nullable
//            @Override
//            public Plugin apply(@Nullable NodeInfo input) {
//                try {
//                    return objectMapper.readValue(input, Plugin.class);
//                } catch (IOException e) {
//                    return null;
//                }
//
//            }
//        });
        
        // **************************************************
        // Passa PluginList para StorageService aqui
        // **************************************************
//        
//        return null;  
//   }
}
