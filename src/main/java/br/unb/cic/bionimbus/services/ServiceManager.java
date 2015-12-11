package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService.Path;
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
        //create root bionimbuz if does not exists
        if (!cms.getZNodeExist(Path.ROOT.getFullPath(), null))
            cms.createZNode(CreateMode.PERSISTENT, Path.ROOT.getFullPath(), "");
        
        // create root peer node if does not exists
        if (!cms.getZNodeExist(Path.PEERS.getFullPath(), null))
            cms.createZNode(CreateMode.PERSISTENT, Path.PEERS.getFullPath(), "");
        
        // add current instance as a peer
        cms.createZNode(CreateMode.PERSISTENT, Path.NODE_PEER.getFullPath(id), null);
        cms.createZNode(CreateMode.EPHEMERAL, Path.STATUS.getFullPath(id), null);
        
        // create services repository node
        if(!cms.getZNodeExist(Path.SERVICES.getFullPath(), null)) {
            // create history root
            cms.createZNode(CreateMode.PERSISTENT, Path.SERVICES.getFullPath(), "");
        }
        
        // create finished tasks node if it doesn't exists
        if(!cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null)) {
            cms.createZNode(CreateMode.PERSISTENT, Path.FINISHED_TASKS.getFullPath(), "");
        }
    }
    
    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper(){
        
        if (cms.getZNodeExist(Path.PIPELINES.getFullPath(), null))
            cms.delete(Path.PIPELINES.getFullPath());
        if (cms.getZNodeExist(Path.PENDING_SAVE.getFullPath(), null))
            cms.delete(Path.PENDING_SAVE.getFullPath());
        if (cms.getZNodeExist(Path.PEERS.getFullPath(), null))
            cms.delete(Path.PEERS.getFullPath());
        if (cms.getZNodeExist(Path.SERVICES.getFullPath(), null))
            cms.delete(Path.SERVICES.getFullPath());
        if (cms.getZNodeExist(Path.FINISHED_TASKS.getFullPath(), null))
            cms.delete(Path.FINISHED_TASKS.getFullPath());
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
