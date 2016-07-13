package br.unb.cic.bionimbus.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.jpa.HibernatePersistenceProvider;

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
     *
     * @throws java.lang.Exception
     */
    public static void initialize() throws Exception {
        factory = Persistence.createEntityManagerFactory("bionimbuz_pu");
        
        if (manager == null) {
            manager = factory.createEntityManager();

            return;
        }

        // It shouldn't be null at this time... If it is = Exception
        throw new Exception();
    }

    /**
     * Returns an Entity Manager
     *
     * @return
     */
    public static EntityManager getEntityManager() {
        // HibernatePersistenceProvider p = new HibernatePersistenceProvider();
        // factory = p.createEntityManagerFactory("bionimbuz_pu", null);
        
        factory = Persistence.createEntityManagerFactory("bionimbuz_pu");
        

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
