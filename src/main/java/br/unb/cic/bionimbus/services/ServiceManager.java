package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
<<<<<<< HEAD
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
=======
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
>>>>>>> 8b0d432507615fb11db91bbbe5d36eab89aba0a4
import br.unb.cic.bionimbus.toSort.Listeners;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

@Singleton
public class ServiceManager {
    
    private final Set<Service> services = new LinkedHashSet<Service> ();
    private final CloudMessageService cms;
    private final RepositoryService rs;
    
    private final RpcServer rpcServer;
    
<<<<<<< HEAD
    private static final String ROOT_PEER = CuratorMessageService.Path.PEERS.toString();
    
    private static final String SEPARATOR = CuratorMessageService.Path.SEPARATOR.toString();
    private static final String PREFIX_PEERS = ROOT_PEER+SEPARATOR+CuratorMessageService.Path.PREFIX_PEER.toString();
    private static final String STATUS = CuratorMessageService.Path.STATUS.toString();
    
    private static final String ROOT_REPOSITORY = CuratorMessageService.Path.HISTORY.toString();
    private static final String PREFIX_TASK = ROOT_REPOSITORY + CuratorMessageService.Path.PREFIX_TASK.toString();
    
=======
>>>>>>> 8b0d432507615fb11db91bbbe5d36eab89aba0a4
    private final HttpServer httpServer;
    
    @Inject
    private MetricRegistry metricRegistry;
    
    @Inject
    public ServiceManager(Set<Service> services, CloudMessageService cms, RepositoryService rs, RpcServer rpcServer, HttpServer httpServer) {
        this.cms = cms;
        this.rs = rs;
        this.rpcServer = rpcServer;
        this.httpServer = httpServer;
        
        this.services.addAll(services);
    }
    
    public void connectZK(String hosts) throws IOException, InterruptedException {
        System.out.println("conectando ao ZooKeeperService...");
        cms.connect(hosts);
    }
    
    public void createZnodeZK(String id) throws IOException, InterruptedException, KeeperException {
<<<<<<< HEAD
        // create root peer node if does not exists
        if (!cms.getZNodeExist(ROOT_PEER, false))
            cms.createZNode(CreateMode.PERSISTENT, ROOT_PEER, "10");
        
        // add current instance as a peer
        cms.createZNode(CreateMode.PERSISTENT, PREFIX_PEERS+id, null);
        cms.createZNode(CreateMode.EPHEMERAL, PREFIX_PEERS+id+SEPARATOR+STATUS, null);
        
        // create history repository nodes
        if(!cms.getZNodeExist(ROOT_REPOSITORY, false)) {
            // create history root
            cms.createZNode(CreateMode.PERSISTENT, ROOT_REPOSITORY, "10");
            
            // insert all tasks
//            for each task t
//                PluginTask t = new PluginTask();
//                cms.createZNode(CreateMode.PERSISTENT, PREFIX_TASK+t.getId(), null);
//            endfor
        }
        
        
=======
        //create root bionimbuz if does not exists
        if (!cms.getZNodeExist(Path.ROOT.getFullPath(), null))
            cms.createZNode(CreateMode.PERSISTENT, Path.ROOT.getFullPath(), "");
        
        // create root peer node if does not exists
        if (!cms.getZNodeExist(Path.PEERS.getFullPath(), null))
            cms.createZNode(CreateMode.PERSISTENT, Path.PEERS.getFullPath(), "");
        
        // add current instance as a peer
        rs.addPeerToZookeeper(new PluginInfo(id));
        
        // create services repository node
        if(!cms.getZNodeExist(Path.SERVICES.getFullPath(), null)) {
            // create history root
            cms.createZNode(CreateMode.PERSISTENT, Path.SERVICES.getFullPath(), "");
        }
        
        // create finished tasks node if it doesn't exists
        if(!cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.FINISHED_TASKS.getFullPath(), "");
        }
>>>>>>> 8b0d432507615fb11db91bbbe5d36eab89aba0a4
    }
    
    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper(){
        
        if (cms.getZNodeExist(Path.ROOT.getFullPath(), null))
            cms.delete(Path.ROOT.getFullPath());
//        if (cms.getZNodeExist(Path.PIPELINES.getFullPath(), null))
//            cms.delete(Path.PIPELINES.getFullPath());
//        if (cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(), null))
//            cms.delete(Path.PENDING_SAVE.getFullPath());
//        if (cms.getZNodeExist(Path.PEERS.getFullPath(), null))
//            cms.delete(Path.PEERS.getFullPath());
//        if (cms.getZNodeExist(Path.SERVICES.getFullPath(), null))
//            cms.delete(Path.SERVICES.getFullPath());
//        if (cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null))
//            cms.delete(Path.FINISHED_TASKS.getFullPath());
    }
    
    public void register(Service service) {
        services.add(service);
    }
    
    public void startAll(BioNimbusConfig config, List<Listeners> listeners) {
        try {
            rpcServer.start();
            httpServer.start();
            connectZK(config.getZkHosts());
            //limpando o servicor zookeeper caso não tenha peer on-line ao inciar servidor zooNimbus
            if (!config.isClient())
                clearZookeeper();
            
            createZnodeZK(config.getId());
            
            for (Service service : services) {
                service.start(config, listeners);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        
    }
}
