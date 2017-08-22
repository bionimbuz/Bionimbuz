package br.unb.cic.bionimbuz.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.unb.cic.bionimbuz.config.DatabaseConfig;

/**
 * Produces an Entity Manager for any Data Access Object
 *
 * @author Vinicius
 *
 */
public class EntityManagerProducer {

    private static EntityManagerFactory factory;
    private static EntityManager manager;
    private static final String PERSIST_UNIT = "bionimbuz_pu";    
    private static final String DB_URL = "javax.persistence.jdbc.url";    
    private static final String DB_USER = "javax.persistence.jdbc.user";    
    private static final String DB_PASS = "javax.persistence.jdbc.password";    
    
    private static Map<String, String> getDatabaseConnectionProperties() {

        Map<String, String> properties = new HashMap<String, String>();                
        properties.put(DB_URL, DatabaseConfig.get().getDatabaseUrl());
        properties.put(DB_USER, DatabaseConfig.get().getDatabaseUser());
        properties.put(DB_PASS, DatabaseConfig.get().getDatabasePass());
            
        return properties;
    }

    public static EntityManager getEntityManager() {
        
        factory = Persistence.createEntityManagerFactory(
                PERSIST_UNIT, 
                getDatabaseConnectionProperties());    
        manager = factory.createEntityManager();
        return manager;
    }

    /**
     * Closes entity manager and factory to avoid message HHH000436 (Entity
     * manager factory already registered)
     */
    public static void closeEntityManager() {
        factory.close();
        manager.clear();
        manager.close();

        manager = null;
    }
}
