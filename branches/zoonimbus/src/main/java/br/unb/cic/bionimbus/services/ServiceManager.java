package br.unb.cic.bionimbus.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import br.unb.cic.bionimbus.p2p.P2PService;

import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.zookeeper.ZooKeeperService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ServiceManager {

    private final Set<Service> _services;
    private final ZooKeeperService zkService;

    @Inject
    public ServiceManager(final ZooKeeperService service, Set<Service> services) {
        zkService = service;
        _services = new HashSet<Service>();
        _services.addAll(services);
    }

    public void connectZK(String hosts) throws IOException, InterruptedException {
        System.out.println("conectando ao ZooKeeperService...");
        if (zkService.getStatus() != ZooKeeperService.Status.CONNECTED
                && zkService.getStatus() != ZooKeeperService.Status.CONNECTING) {
            zkService.connect(hosts);
        }
    }

    public void register(Service service) {
        _services.add(service);
    }

    public void startAll(P2PService p2p) {
        try {
            connectZK(p2p.getConfig().getZkHosts());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        for (Service service : _services) {
            service.start(p2p);
        }
    }
}
