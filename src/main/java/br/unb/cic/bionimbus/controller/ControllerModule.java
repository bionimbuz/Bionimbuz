/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.controller;

import br.unb.cic.bionimbus.jobcontroller.JobController;
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
 *
 * @author zoonimbus
 */
public class ControllerModule extends AbstractModule {

    @Override
    protected void configure() {
        // Binds Controller classes
        Multibinder<Controller> controllerBinder = Multibinder.newSetBinder(binder(), Controller.class);
        controllerBinder.addBinding().to(JobController.class);

        // Binds Services classes
        Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);
        serviceBinder.addBinding().to(DiscoveryService.class);
        serviceBinder.addBinding().to(StorageService.class);
        serviceBinder.addBinding().to(SchedService.class);
        serviceBinder.addBinding().to(MonitoringService.class);
        serviceBinder.addBinding().to(RepositoryService.class);
        bind(CloudMessageService.class).to(CuratorMessageService.class);
    }

}
