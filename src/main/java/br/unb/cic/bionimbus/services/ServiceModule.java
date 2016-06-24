/*
    BioNimbuZ is a federated cloud platform.
    Copyright (C) 2012-2015 Laboratory of Bioinformatics and Data (LaBiD), 
    Department of Computer Science, University of Brasilia, Brazil

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.unb.cic.bionimbus.services;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.rpc.AvroServer;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.config.ConfigurationRepository;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.services.monitor.MonitoringService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import br.unb.cic.bionimbus.toSort.CloudStorageService;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Configures injection parameters for Service
 */
public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {

        // bind(HttpServer.class);
        bind(BioProto.class).to(BioProtoImpl.class);
        bind(RpcServer.class).to(AvroServer.class);
        bind(MetricRegistry.class).asEagerSingleton();
        bind(HealthCheckRegistry.class).asEagerSingleton();

        // If someone changes CloudMessageService implementation, need to change to() method
        bind(CloudMessageService.class).to(CuratorMessageService.class);

        // This order defines the injection order
        Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);

        // 1st to be injected
        serviceBinder.addBinding().to(RepositoryService.class);

        // 2nd to be injected
        serviceBinder.addBinding().to(DiscoveryService.class);

        // 3rd to be injected
        serviceBinder.addBinding().to(StorageService.class);

        // 4th to be injected
        serviceBinder.addBinding().to(SchedService.class);
    
        // 5th to be injected
        serviceBinder.addBinding().to(MonitoringService.class);
        
        if (ConfigurationRepository.getConfig().getStorageMode().equalsIgnoreCase("1"))
            serviceBinder.addBinding().to(CloudStorageService.class);

    }

}
