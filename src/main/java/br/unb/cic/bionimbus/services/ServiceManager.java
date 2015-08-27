package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.toSort.Listeners;
import br.unb.cic.bionimbus.toSort.RepositoryService;
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
        // create root peer node if does not exists
        if (!cms.getZNodeExist(CuratorMessageService.Path.PEERS.toString(), false))
            cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.PEERS.toString(), "");
        
        // add current instance as a peer
        cms.createZNode(CreateMode.PERSISTENT, CuratorMessageService.Path.PREFIX_PEER.getFullPath(id), null);
        cms.createZNode(CreateMode.EPHEMERAL, CuratorMessageService.Path.STATUS.getFullPath(id), null);
        
        // create services repository node
        if(!cms.getZNodeExist(cms.getPath().SERVICES.getFullPath(), false)) {
            // create history root
            cms.createZNode(CreateMode.PERSISTENT, cms.getPath().SERVICES.getFullPath(), "");
        }
    }
    
    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper(){
        
        if (cms.getZNodeExist(cms.getPath().PIPELINES.getFullPath(), false))
            cms.delete(cms.getPath().PIPELINES.getFullPath());
        if (cms.getZNodeExist(cms.getPath().PENDING_SAVE.getFullPath(), false))
            cms.delete(cms.getPath().PENDING_SAVE.toString());
        if (cms.getZNodeExist(cms.getPath().PEERS.getFullPath(), false))
            cms.delete(cms.getPath().PEERS.toString());
        if (cms.getZNodeExist(cms.getPath().SERVICES.getFullPath(), false))
            cms.delete(cms.getPath().SERVICES.toString());
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
