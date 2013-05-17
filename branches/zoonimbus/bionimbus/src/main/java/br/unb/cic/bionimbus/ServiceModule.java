package br.unb.cic.bionimbus;

import br.unb.cic.bionimbus.discovery.DiscoveryService;
import br.unb.cic.bionimbus.sched.SchedService;
import br.unb.cic.bionimbus.storage.StorageService;

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
