/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.rpc.AvroServer;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbus.avro.rpc.RpcServer;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.controller.usercontroller.UserController;
import br.unb.cic.bionimbus.services.RepositoryService;
import br.unb.cic.bionimbus.services.Service;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.messaging.CloudMessageService;
import br.unb.cic.bionimbus.services.messaging.CuratorMessageService;
import br.unb.cic.bionimbus.services.monitor.MonitoringService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

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

        // Marks the controllers as EagerSingleton
        bind(JobController.class).asEagerSingleton();
        bind(UserController.class).asEagerSingleton();

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
    }

}
