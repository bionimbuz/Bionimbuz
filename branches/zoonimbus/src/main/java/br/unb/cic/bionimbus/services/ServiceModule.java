package br.unb.cic.bionimbus.services;


import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);

        serviceBinder.addBinding().to(DiscoveryService.class);
        serviceBinder.addBinding().to(StorageService.class);
        serviceBinder.addBinding().to(SchedService.class);
//		serviceBinder.addBinding().to(MonitorService.class);		
    }

}
