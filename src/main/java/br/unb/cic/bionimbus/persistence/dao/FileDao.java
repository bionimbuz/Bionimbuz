package br.unb.cic.bionimbus.persistence.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import br.unb.cic.bionimbus.rest.model.UploadedFileInfo;

/**
 * Class that is responsible to operate over the database the UserFile elements
 * (CRUD operations). DAO stands for Data Access Object
 *
 * @author Vinicius
 *
 */
public class FileDao extends BaseDao<UploadedFileInfo> {

    /**
     * Persists an user file on database
     *
     * @param fileInfo
     */
    @Override
    public void persist(UploadedFileInfo fileInfo) {
        try {
            manager.getTransaction().begin();
            manager.persist(fileInfo);
            manager.getTransaction().commit();

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
    public List<UploadedFileInfo> list() {
        TypedQuery<UploadedFileInfo> query = manager.createQuery("SELECT u FROM FileInfo u", UploadedFileInfo.class);

        return query.getResultList();
    }

    /**
     * Update an user file information
     *
     * @param fileInfo
     */
    @Override
    public void update(UploadedFileInfo fileInfo) {
        // TODO Auto-generated method stub

    }

    /**
     * Removes an user File
     *
     * @param fileInfo
     */
    @Override
    public void delete(UploadedFileInfo fileInfo) {
        try {
            manager.getTransaction().begin();
            manager.remove(fileInfo);
            manager.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            manager.getTransaction().rollback();
        }
    }

    /**
     * Return one specific user file by its id
     *
     * @param id
     * @return
     */
    @Override
    public UploadedFileInfo findById(Long id) {
        TypedQuery<UploadedFileInfo> query = manager.createQuery("SELECT u FROM FileInfo u WHERE u.id = := id", UploadedFileInfo.class);
        query.setParameter("id", id);

        return query.getSingleResult();
    }

    /**
     * Returns specific User file's list
     *
     * @param userId
     * @return
     */
    public List<UploadedFileInfo> listByUserId(Long userId) {
        TypedQuery<UploadedFileInfo> query = manager.createQuery("SELECT u FROM FileInfo u WHERE u.userId = :userId", UploadedFileInfo.class);
        query.setParameter("userId", userId);

        return query.getResultList();
    }

}
