package br.unb.cic.bionimbus.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.p2p.P2PService;

import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.zookeeper.ZooKeeperService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ServiceManager {

    private final Set<Service> services = new HashSet<Service>();
    private final ZooKeeperService zkService;

    private final RpcServer rpcServer;

    @Inject
    public ServiceManager(final ZooKeeperService zkService, Set<Service> services, RpcServer rpcServer) {
        this.zkService = zkService;
        this.rpcServer = rpcServer;
        services.addAll(services);
    }

    public void connectZK(String hosts) throws IOException, InterruptedException {
        System.out.println("conectando ao ZooKeeperService...");
        if (zkService.getStatus() != ZooKeeperService.Status.CONNECTED
                && zkService.getStatus() != ZooKeeperService.Status.CONNECTING) {
            zkService.connect(hosts);
        }
    }

    public void register(Service service) {
        services.add(service);
    }

    public void startAll(P2PService p2p) {
        try {

            connectZK(p2p.getConfig().getZkHosts());
//            rpcServer.start();
            for (Service service : services) {
                service.start(p2p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}
