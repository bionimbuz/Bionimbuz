package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.p2p.P2PService;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.zookeeper.KeeperException;

@Singleton
public class ServiceManager {

    private final Set<Service> services = new LinkedHashSet<Service> ();
    private final ZooKeeperService zkService;

    private final RpcServer rpcServer;
    
    private static final String ROOT_PEER = "/peers";
    private static final String LATENCY = "/latency";

    private static final String SEPARATOR = "/";
    private static final String PEERS = ROOT_PEER+SEPARATOR+"peer_";
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
           zkService.createPersistentZNode(PEERS+ id, null);
          
           //criando status efemera para verificar se o servidor esta rodando
           zkService.createEphemeralZNode(PEERS+ id+SEPARATOR+STATUS, null);
        
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
