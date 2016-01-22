package br.unb.cic.bionimbus.persistence.dao;

import br.unb.cic.bionimbus.model.Log;
import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import java.util.List;
import javax.persistence.TypedQuery;

/**
 *
 * @author Vinicius
 */
public class WorkflowLoggerDao extends AbstractDao<Log> {

    @Override
    public void persist(Log log) {
    }

    /**
     * log() makes more sense than persist()
     *
     * @param log
     */
    public void log(Log log) {
        try {
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(log);
            manager.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            manager.getTransaction().rollback();
        }
    }

    @Override
    public List<Log> list() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(Log entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Log entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Log findById(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param workflowId
     * @return
     */
    public List<Log> listByWorkflowId(String workflowId) {
        // Creates Entity Manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<Log> query = manager.createQuery("SELECT l FROM Log l WHERE l.workflowId = :workflowId", Log.class);
        query.setParameter("workflowId", workflowId);
        List<Log> result = query.getResultList();

        // Closes manager
        manager.close();

        return result;
    }

}
