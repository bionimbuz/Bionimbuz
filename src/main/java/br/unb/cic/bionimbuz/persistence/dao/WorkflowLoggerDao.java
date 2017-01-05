package br.unb.cic.bionimbuz.persistence.dao;

import br.unb.cic.bionimbuz.model.Log;
import br.unb.cic.bionimbuz.model.WorkflowOutputFile;
import br.unb.cic.bionimbuz.persistence.EntityManagerProducer;
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
     * Log something to the user. (log() makes more sense than persist())
     *
     * @param log
     */
    public void log(Log log) {
        if (log.getWorkflowId() != null) {
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
    }

    /**
     * Persists a workflow output file to link it to the workflow.
     *
     * @param output
     */
    public void logOutputFile(WorkflowOutputFile output) {
        try {
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(output);
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
     * Persists a list of workflow output files to link it to the workflow.
     *
     * @param workflowId
     * @param outputs
     */
    public void logOutputFile(String workflowId, List<String> outputs) {

        try {
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            for (String output : outputs) {
                WorkflowOutputFile file = new WorkflowOutputFile(workflowId, output);

                // Get a Transaction, persist and commit
                manager.getTransaction().begin();
                manager.persist(file);
                manager.getTransaction().commit();
            }

        } catch (Exception e) {

            // Rollback
            manager.getTransaction().rollback();
        } finally {

            // Close connection
            manager.close();
        }
    }

    /**
     * List all output files of a workflow id
     * @param workflowId
     * @return 
     */
    public List<WorkflowOutputFile> listAllOutputFilesByWorkflowId(String workflowId) {
        // Creates Entity Manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<WorkflowOutputFile> query = manager.createQuery("SELECT o FROM WorkflowOutputFile o WHERE o.workflowId = :workflowId", WorkflowOutputFile.class);
        query.setParameter("workflowId", workflowId);
        
        List<WorkflowOutputFile> result = query.getResultList();

        // Closes manager
        manager.close();

        return result;
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
