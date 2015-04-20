package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.BioNimbusConfig;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
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
    
    private final RpcServer rpcServer;
    
    private static final String ROOT_PEER = "/peers";
    
    private static final String SEPARATOR = "/";
    private static final String PREFIX_PEERS = ROOT_PEER+SEPARATOR+"peer_";
    private static final String STATUS = "STATUS";
    
    private final HttpServer httpServer;
    
    @Inject
    private MetricRegistry metricRegistry;
    
    @Inject
    public ServiceManager(Set<Service> services, CloudMessageService cms, RpcServer rpcServer, HttpServer httpServer) {
        this.cms = cms;
        this.rpcServer = rpcServer;
        this.httpServer = httpServer;
        
        this.services.addAll(services);
    }
    
    public void connectZK(String hosts) throws IOException, InterruptedException {
        System.out.println("conectando ao ZooKeeperService...");
        cms.connect(hosts);
    }
    
    public void createZnodeZK(String id) throws IOException, InterruptedException, KeeperException {
        if (!cms.getZNodeExist(ROOT_PEER, false))
            cms.createZNode(CreateMode.PERSISTENT, ROOT_PEER, "10");
        cms.createZNode(CreateMode.PERSISTENT, PREFIX_PEERS+id, null);
        cms.createZNode(CreateMode.EPHEMERAL, PREFIX_PEERS+id+SEPARATOR+STATUS, null);
    }
    
    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper(){
        List<String> peers;
        boolean existPeer=false;
        
        if(cms.getZNodeExist(ROOT_PEER, false)){
            peers = cms.getChildren(ROOT_PEER, null);
            if(!(peers==null) && !peers.isEmpty()){
                for(String peer : peers){
                    if(cms.getZNodeExist(ROOT_PEER+SEPARATOR+peer+SEPARATOR+STATUS, false))
                        return;
                }
                cms.delete(ROOT_PEER);
                cms.delete(cms.getPath().PENDING_SAVE.toString());
                cms.delete(cms.getPath().JOBS.toString());
            }
            if(!existPeer){
                //apaga os znodes que haviam no servidor
            }
        }
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
