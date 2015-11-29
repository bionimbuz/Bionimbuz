package br.unb.cic.bionimbus.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Produces an Entity Manager for any Data Access Object
 *
 * @author Vinicius
 *
 */
public class EntityManagerProducer {

    private static EntityManagerFactory factory;
    private static EntityManager manager;

    /**
     * Initializes EntityManager to prevent lazy creation
     */
    public static void initialize() {
        factory = Persistence.createEntityManagerFactory("bionimbuz_pu");

        manager = factory.createEntityManager();
    }

    /**
     * Returns an Entity Manager
     *
     * @return
     */
    public static EntityManager getEntityManager() {
        factory = Persistence.createEntityManagerFactory("bionimbuz_pu");

        if (manager == null) {
            manager = factory.createEntityManager();
        }

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
