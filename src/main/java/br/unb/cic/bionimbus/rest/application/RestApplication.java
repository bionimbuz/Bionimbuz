/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.rest.application;

import br.unb.cic.bionimbus.rest.resource.FileResource;
import br.unb.cic.bionimbus.rest.resource.PingResource;
import br.unb.cic.bionimbus.rest.resource.UserResource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.eclipse.jetty.util.resource.Resource;

/**
 *
 * @author Vinicius
 */
public class RestApplication extends Application {

    @SuppressWarnings("rawtypes")
    private static Set services = new HashSet();

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
     * !!!                  IMPORTANT               !!!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
     * Everytime a class that defines a resource is created, 
     * it's ESSENCIAL that is added to Services Set
     */
    @SuppressWarnings("unchecked")
    public RestApplication() {
        services.add(new UserResource());
        services.add(new FileResource());
        services.add(new PingResource());
    }

    private static final Set<Class<?>> CLASSES;

    static {
        HashSet<Class<?>> tmp = new HashSet<Class<?>>();
        tmp.add(Resource.class);

        CLASSES = Collections.unmodifiableSet(tmp);
    }

    @Override
    public Set<Class<?>> getClasses() {

        return CLASSES;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set getSingletons() {
        return services;
    }

    @SuppressWarnings("rawtypes")
    public static Set getServices() {
        return services;
    }
}
