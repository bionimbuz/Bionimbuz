package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.p2p.P2PService;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

@Singleton
public class ServiceManager {

    private final Set<Service> services = new LinkedHashSet<Service> ();
    private final ZooKeeperService zkService;

    private final RpcServer rpcServer;
    
    private static final String ROOT_PEER = "/peers";
    private static final String LATENCY = "/latency";

    private static final String SEPARATOR = "/";
    private static final String PREFIX_PEERS = ROOT_PEER+SEPARATOR+"peer_";
    private static final String STATUS = "STATUS";

    private final HttpServer httpServer;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public ServiceManager(Set<Service> services, ZooKeeperService zkService, RpcServer rpcServer, HttpServer httpServer) {
        this.zkService = zkService;
        this.rpcServer = rpcServer;
        this.httpServer = httpServer;

        this.services.addAll(services);
    }

    public void connectZK(String hosts) throws IOException, InterruptedException {
        System.out.println("conectando ao ZooKeeperService...");
        if (zkService.getStatus() != ZooKeeperService.Status.CONNECTED
                && zkService.getStatus() != ZooKeeperService.Status.CONNECTING) {
            zkService.connect(hosts);
        }
    }
    
    public void createZnodeZK(String id) throws IOException, InterruptedException, KeeperException {
        if (zkService.getStatus() == ZooKeeperService.Status.CONNECTED) {
           
            zkService.createPersistentZNode(ROOT_PEER, null);
            
            zkService.createPersistentZNode(LATENCY, null);
            
           //criando zNode persistente para cada novo peer
           zkService.createPersistentZNode(PREFIX_PEERS+ id, null);
          
           //criando status efemera para verificar se o servidor esta rodando
           zkService.createEphemeralZNode(PREFIX_PEERS+ id+SEPARATOR+STATUS, null);
        
        }
    }

    /**
     * Responsável pela limpeza do servidor a cada nova conexão onde o todos os plug-ins havia ficado indisponíveis.
     */
    private void clearZookeeper(){
        List<String> peers;
//        boolean existPeer=false;
        try {
            if((zkService.getStatus() == ZooKeeperService.Status.CONNECTED) && zkService.getZNodeExist(ROOT_PEER, false)){
                peers = zkService.getChildren(ROOT_PEER, null);
                if(!(peers==null) && !peers.isEmpty()){
                    for(String peer : peers){
                        if(zkService.getZNodeExist(ROOT_PEER+SEPARATOR+peer+SEPARATOR+STATUS, false))
                            return;
                    }
                    zkService.delete(ROOT_PEER);
                    zkService.delete(LATENCY);
                    zkService.delete(zkService.getPath().PENDING_SAVE.toString());
                    zkService.delete(zkService.getPath().JOBS.toString());
                }
//                if(!existPeer){
//                    //apaga os znodes que haviam no servidor
//                }
            }
        } catch (KeeperException ex) {
            Logger.getLogger(ServiceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServiceManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServiceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void register(Service service) {
        services.add(service);
    }

    public void startAll(P2PService p2p) {
        try {
            rpcServer.start();
            httpServer.start();
            connectZK(p2p.getConfig().getZkHosts());
            //limpando o servicor zookeeper caso não tenha peer on-line ao inciar servidor zooNimbus
            clearZookeeper();
            createZnodeZK(p2p.getConfig().getId());
            for (Service service : services) {
                service.start(p2p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}
