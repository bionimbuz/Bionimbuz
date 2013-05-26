package br.unb.cic.bionimbus;

import java.util.HashSet;
import java.util.Set;

import br.unb.cic.bionimbus.p2p.P2PService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ServiceManager {

    private final Set<Service> _services;

    @Inject
    public ServiceManager(Set<Service> services) {
        _services = new HashSet<Service>();
        _services.addAll(services);
    }

    public void register(Service service) {
        _services.add(service);
    }

    public void startAll(P2PService p2p) {
        for (Service service : _services) {
            service.start(p2p);
        }
    }
}
