/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.controller;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import br.unb.cic.bionimbuz.avro.gen.BioProto;
import br.unb.cic.bionimbuz.avro.rpc.AvroServer;
import br.unb.cic.bionimbuz.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbuz.avro.rpc.RpcServer;
import br.unb.cic.bionimbuz.config.BioNimbusConfig;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.services.Service;
import br.unb.cic.bionimbuz.services.discovery.DiscoveryService;
import br.unb.cic.bionimbuz.services.messaging.CloudMessageService;
import br.unb.cic.bionimbuz.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbuz.services.monitor.MonitoringService;
import br.unb.cic.bionimbuz.services.sched.SchedService;
import br.unb.cic.bionimbuz.services.storage.StorageService;
import br.unb.cic.bionimbuz.services.storage.bucket.CloudStorageService;

/**
 * Guice Module to Inject on ControllerManager
 *
 * @author Vinicius
 */
public class ControllerModule extends AbstractModule {

    @Override
    protected void configure() {
        // If someone changes CloudMessageService implementation, need to change here too
        bind(CloudMessageService.class).to(CuratorMessageService.class);

        bind(BioProto.class).to(BioProtoImpl.class);
        bind(RpcServer.class).to(AvroServer.class);

        // Binds Controller classes
        Multibinder<Controller> controllerBinder = Multibinder.newSetBinder(binder(), Controller.class);
        controllerBinder.addBinding().to(JobController.class);
        controllerBinder.addBinding().to(UserController.class);
        controllerBinder.addBinding().to(SlaController.class);
        controllerBinder.addBinding().to(ElasticityController.class);

        // Marks the controllers as EagerSingleton
        bind(JobController.class).asEagerSingleton();
        bind(UserController.class).asEagerSingleton();
        bind(SlaController.class).asEagerSingleton();
        bind(ElasticityController.class).asEagerSingleton();
        
        // Binds Services classes
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
        
        if (BioNimbusConfig.get().getStorageMode().equalsIgnoreCase("1"))
            serviceBinder.addBinding().to(CloudStorageService.class);
    }

}
