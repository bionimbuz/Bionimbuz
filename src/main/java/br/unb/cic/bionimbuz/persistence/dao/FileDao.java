package br.unb.cic.bionimbuz.persistence.dao;

import br.unb.cic.bionimbuz.model.FileInfo;
import java.util.List;

import javax.persistence.TypedQuery;

import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.persistence.EntityManagerProducer;

/**
 * Class that is responsible to operate over the database the UserFile elements
 * (CRUD operations). DAO stands for Data Access Object
 *
 * @author Vinicius
 *
 */
public class FileDao extends AbstractDao<FileInfo> {

    /**
     * Persists an user file on database
     *
     * @param fileInfo
     */
    @Override
    public void persist(FileInfo fileInfo) {
        try {
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(fileInfo);
            manager.getTransaction().commit();

            // Updates user storage usage
            User user = manager.find(User.class, fileInfo.getUserId());
            user.addStorageUsage(fileInfo.getSize());

        } catch (Exception e) {
            e.printStackTrace();
            manager.getTransaction().rollback();
        }
    }

    /**
     * Returns all user files on database. Use listByUserId to retrieve all
     * files from a specific user.
     *
     * @return
     */
    @Override
    public List<FileInfo> list() {
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<FileInfo> query = manager.createQuery("SELECT u FROM FileInfo u", FileInfo.class);
        List<FileInfo> result = query.getResultList();

        // Close connection
        manager.close();

        return result;
    }

    /**
     * Update an user file information
     *
     * @param fileInfo
     */
    @Override
    public void update(FileInfo fileInfo) {
        // TODO Auto-generated method stub

    }

    /**
     * Removes an user File
     *
     * @param fileInfo
     */
    @Override
    public void delete(FileInfo fileInfo) {
        try {
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            manager.getTransaction().begin();
            //em.remove(em.contains(entity) ? entity : em.merge(entity));
            manager.remove(manager.contains(fileInfo) ? fileInfo : manager.merge(fileInfo));
            //manager.remove(fileInfo);
            manager.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            manager.getTransaction().rollback();

        } finally {
            // Close connection
            manager.close();
        }
    }

    /**
     * Return one specific user file by its id
     *
     * @param id
     * @return
     */
    @Override
    public FileInfo findById(Long id) {
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<FileInfo> query = manager.createQuery("SELECT u FROM FileInfo u WHERE u.id = :id", FileInfo.class);
        query.setParameter("id", id);
        FileInfo result = query.getSingleResult();

        // Close connection
        manager.close();

        return result;
    }
    
    public FileInfo findByStringId (String id) {
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        // Find the file
        TypedQuery<FileInfo> query = manager.createQuery("SELECT u FROM FileInfo u WHERE u.id = :id", FileInfo.class);
        query.setParameter("id", id);
        FileInfo result = query.getSingleResult();
        
        // Close connection
        manager.close();
        
        return result;
    }

    /**
     * Returns specific User file's list
     *
     * @param userId
     * @return
     */
    public List<FileInfo> listByUserId(Long userId) {
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<FileInfo> query = manager.createQuery("SELECT u FROM FileInfo u WHERE u.userId = :userId", FileInfo.class);
        query.setParameter("userId", userId);
        List<FileInfo> result = query.getResultList();

        // Close connection
        manager.close();

        return result;
    }

}
