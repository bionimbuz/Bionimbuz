package br.unb.cic.bionimbus.persistence.dao;

import br.unb.cic.bionimbus.model.Workflow;
import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import java.util.List;
import javax.persistence.TypedQuery;

/**
 * Class that manages database operations for Workflow class
 *
 * @author Vinicius
 */
public class WorkflowDao extends AbstractDao<Workflow> {

    /**
     * Persists a new workflow in database
     *
     * @param workflow
     */
    @Override
    public void persist(Workflow workflow) {
        try {
            // Verifies if the manager is opened before persist
            if (!manager.isOpen()) {
                manager = EntityManagerProducer.getEntityManager();
            }
            
            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(workflow);
            manager.getTransaction().commit();

        } catch (Exception e) {
            manager.getTransaction().rollback();
        }
    }

    /**
     * Lists all workflows in database
     *
     * @return
     */
    @Override
    public List<Workflow> list() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Lists all workflows of a given user
     *
     * @param userId
     * @return
     */
    public List<Workflow> listByUserId(Long userId) {
        TypedQuery<Workflow> query = manager.createQuery("SELECT w FROM Workflow w WHERE w.userId = :userId", Workflow.class);
        query.setParameter("userId", userId);

        return query.getResultList();
    }

    /**
     * Updates a Workflow
     *
     * @param workflow
     */
    @Override
    public void update(Workflow workflow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Deletes a workflow
     *
     * @param workflow
     */
    @Override
    public void delete(Workflow workflow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Fins a workflow for a given id
     *
     * @param id
     * @return
     */
    @Override
    public Workflow findById(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
