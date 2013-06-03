package br.unb.cic.bionimbus.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.p2p.P2PService;

import br.unb.cic.bionimbus.services.storage.StorageService;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ServiceManager {

    private final Set<Service> services = new HashSet<Service>();
    private final ZooKeeperService zkService;

    private final RpcServer rpcServer;
    
    private static final String ROOT_PEER = "/peers";
    private static final String SEPARATOR = "/";
    private static final String PREFIX_PEER = "peer_";
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
    public void createZnodeZK(String id) throws IOException, InterruptedException {
        if (zkService.getStatus() == ZooKeeperService.Status.CONNECTED) {

            zkService.createPersistentZNode("/peers", null);

            //criando zNode persistente para cada novo peer
            zkService.createPersistentZNode(ROOT_PEER + SEPARATOR + PREFIX_PEER+ id, null);
            

            //criando status efemera para verificar se o servidor esta rodando
            zkService.createEphemeralZNode(ROOT_PEER + SEPARATOR + PREFIX_PEER+ id+SEPARATOR+STATUS, null);

            System.out.println("Criado e registrado peer com path " + ROOT_PEER + SEPARATOR + PREFIX_PEER+ id);
        
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
                if (service instanceof StorageService) {
                    ((StorageService) service).run();
                }
                service.start(p2p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}
