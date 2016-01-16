/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.application;

import br.unb.cic.bionimbus.BioNimbus;
import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.controller.usercontroller.UserController;
import br.unb.cic.bionimbus.rest.resource.AbstractResource;
import br.unb.cic.bionimbus.rest.resource.FileResource;
import br.unb.cic.bionimbus.rest.resource.PingResource;
import br.unb.cic.bionimbus.rest.resource.ServicesResource;
import br.unb.cic.bionimbus.rest.resource.WorkflowResource;
import br.unb.cic.bionimbus.rest.resource.UserResource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * javax.ws.rs.core.Application that defines the Rest Application for the
 * Application Server
 *
 * @author Vinicius
 */
public class RestApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApplication.class);
    private static int resourceCounter = 0;

    @SuppressWarnings("rawtypes")
    private static final Set SERVICES = new HashSet();

    private static final Set<Class<?>> CLASSES;

    static {
        HashSet<Class<?>> tmp = new HashSet<>();
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
    @SuppressWarnings("unchecked")
    public RestApplication() {
        // Retrieves injector from BioNimbus class
        JobController jobController = BioNimbus.controllerInjector.getInstance(JobController.class);
        UserController userController = BioNimbus.controllerInjector.getInstance(UserController.class);

        addResource(new UserResource(jobController, userController));
        addResource(new FileResource(jobController));
        addResource(new WorkflowResource(jobController));
        addResource(new ServicesResource(jobController));
        addResource(new PingResource());

        LOGGER.info("RestApplication started with " + resourceCounter + " Resources");

    }

    @Override
    public Set<Class<?>> getClasses() {

        return CLASSES;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set getSingletons() {
        return SERVICES;
    }

    @SuppressWarnings("rawtypes")
    public static Set getServices() {
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
