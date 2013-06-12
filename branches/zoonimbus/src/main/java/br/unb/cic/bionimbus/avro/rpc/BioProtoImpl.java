package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.google.inject.Inject;
import org.apache.avro.AvroRemoteException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;

public class BioProtoImpl implements BioProto {

    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final SchedService schedService;
    private final ZooKeeperService zkService;
    
//    private static final Object LOCK = new Object();
    
    private Map<String, NodeInfo> nodes = new HashMap<String, NodeInfo>();

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
    public synchronized List<NodeInfo> getPeersNode() throws AvroRemoteException {

        NodeInfo nodeaux;
        //for(PluginInfo info : discoveryService.getPeers())
        nodes.clear();
        for(PluginInfo info : discoveryService.getPeers().values()){
           nodeaux= new NodeInfo();
            //esta setando nulo
           if(info!=null){
                String address = info.getHost().getAddress();
                nodeaux.setAddress(address);

                nodeaux.setPeerId(info.getId());                
                nodes.put(address, nodeaux);
           }
        }
        
        return new ArrayList<NodeInfo>(nodes.values());
    }
    //Set the nodes from the clients with latency
    @Override
    public synchronized Void setNodes(List<NodeInfo> list) throws AvroRemoteException {
           for (NodeInfo node : list) {
              this.nodes.put(node.getAddress(), node);
            }

               for(NodeInfo nodeSeted : this.nodes.values()){
                    //setar o valor na map com o retorno do node
                   PluginInfo plugin = this.discoveryService.getPeers().get(nodeSeted.getPeerId());
                    
                   if(nodeSeted.getLatency()!=null)
                       plugin.setLatency(nodeSeted.getLatency());
               try {
                   zkService.setData(plugin.getPath_zk(), plugin.toString());
               } catch (KeeperException ex) {
                   Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
               } catch (InterruptedException ex) {
                   Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
               }
                        
                    
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

    @Override
    public List<NodeInfo> callStorage() throws AvroRemoteException {
        
        String bestnode = storageService.bestNode();
        return null;
    }

    /*@Override
    public NodeInfo callStorage() throws AvroRemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

}
