/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.rest.security;

import br.unb.cic.bionimbuz.BioNimbuZ;
import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.model.User;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rest Security Interceptor. This class filter all REST request.
 *
 * @author Vinicius
 */
@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);
    private final JobController jobController;

    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());
    private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("Nobody can access this resource", 403, new Headers<>());
    private static final ServerResponse SERVER_ERROR = new ServerResponse("INTERNAL SERVER ERROR", 500, new Headers<>());
    /**
     * Injects 
     */
    public SecurityInterceptor() {
        this.jobController = BioNimbuZ.controllerInjector.getInstance(JobController.class);

        LOGGER.info("SecurityInterceptor started");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();

        // Verifies if it isn't annotatted with @PermitAll
        if (!method.isAnnotationPresent(PermitAll.class)){ 
            
            if(method.isAnnotationPresent(DenyAll.class)) {
                LOGGER.info("Access denied!");
                requestContext.abortWith(Response.status(Status.FORBIDDEN).entity(new User()).build());
                
                return;
            }
        }
    }

}
