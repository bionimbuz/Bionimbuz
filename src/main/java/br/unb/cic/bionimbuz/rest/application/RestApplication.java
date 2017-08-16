/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.unb.cic.bionimbuz.rest.application;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import br.unb.cic.bionimbuz.BioNimbuZ;
import br.unb.cic.bionimbuz.controller.elasticitycontroller.ElasticityController;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.slacontroller.SlaController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.rest.resource.AbstractResource;
import br.unb.cic.bionimbuz.rest.resource.ConfigurationsResource;
import br.unb.cic.bionimbuz.rest.resource.ElasticityResource;
import br.unb.cic.bionimbuz.rest.resource.FileResource;
import br.unb.cic.bionimbuz.rest.resource.PingResource;
import br.unb.cic.bionimbuz.rest.resource.StartSlaResource;
import br.unb.cic.bionimbuz.rest.resource.UserResource;
import br.unb.cic.bionimbuz.rest.resource.WorkflowResource;

/**
 * javax.ws.rs.core.Application that defines the Rest Application for the
 * Application Server
 *
 * @author Vinicius
 */
public class RestApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApplication.class);
    private static final Set<Object> SERVICES = new HashSet<>();
    private static final Set<Class<?>> CLASSES;
    private static int resourceCounter = 0;

    static {
        final HashSet<Class<?>> tmp = new HashSet<>();
        tmp.add(Resource.class);

        CLASSES = Collections.unmodifiableSet(tmp);
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * Everytime a class that defines a resource is created, it's ESSENCIAL that
     * is added to Services Set using addResource(resource)
     */
    public RestApplication() {

        // Retrieves injector from BioNimbuZ class
        final Injector controllerInjector = BioNimbuZ.getControllerInjector();
        final JobController jobController = controllerInjector.getInstance(JobController.class);
        final UserController userController = controllerInjector.getInstance(UserController.class);
        final SlaController slaController = controllerInjector.getInstance(SlaController.class);
        final ElasticityController elasticityController = controllerInjector.getInstance(ElasticityController.class);
        this.addResource(new UserResource(jobController, userController));
        this.addResource(new FileResource(jobController));
        this.addResource(new WorkflowResource(jobController));
        this.addResource(new ConfigurationsResource(jobController));
        this.addResource(new PingResource());
        this.addResource(new StartSlaResource(slaController));
        this.addResource(new ElasticityResource(elasticityController));

        LOGGER.info("RestApplication started with " + resourceCounter + " Resources");
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // * @see javax.ws.rs.core.Application#getClasses()
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public Set<Class<?>> getClasses() {
        return CLASSES;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // * @see javax.ws.rs.core.Application#getSingletons()
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public Set<Object> getSingletons() {
        return SERVICES;
    }

    public static Set<Object> getServices() {
        return SERVICES;
    }

    /**
     * Adds a Rest Resource to the Set of Resources
     *
     * @param resource
     */
    private void addResource(AbstractResource resource) {
        SERVICES.add(resource);
        resourceCounter++;
        LOGGER.info("Rest Resource " + resource.getClass().getSimpleName() + " added");
    }
}
