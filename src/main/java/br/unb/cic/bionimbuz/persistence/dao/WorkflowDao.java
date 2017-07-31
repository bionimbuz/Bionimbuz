package br.unb.cic.bionimbuz.persistence.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.model.WorkflowStatus;
import br.unb.cic.bionimbuz.persistence.EntityManagerProducer;

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
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(workflow);
            manager.getTransaction().commit();

        } catch (Exception e) {

            // Rollback
            manager.getTransaction().rollback();
        } finally {

            // Close connection
            manager.close();
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
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<Workflow> query = manager.createQuery("SELECT w FROM Workflow w WHERE w.userId = :userId", Workflow.class);
        query.setParameter("userId", userId);

        List<Workflow> result = query.getResultList();

        // Close connection
        manager.close();

        return result;
    }

    /**
     * Updates a workflow status.
     *
     * @param workflowId
     * @param newStatus
     */
    public void updateStatus(String workflowId, WorkflowStatus newStatus) {
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        // Finds in database
        Workflow w = manager.find(Workflow.class, workflowId);

        // Update it (as it is in managed state, it works)
        w.setStatus(newStatus);

        // Close connection
        manager.close();
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
