package br.unb.cic.bionimbus.persistence.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import br.unb.cic.bionimbus.model.User;
import br.unb.cic.bionimbus.persistence.EntityManagerProducer;
import java.util.ArrayList;

/**
 * Class that manages database operations for User class
 *
 * @author Vinicius
 */
public class UserDao extends AbstractDao<User> {

    /**
     * Persists a new user
     *
     * @param user
     */
    @Override
    public void persist(User user) {
        try {
            // Verifies if the manager is opened before persist
            if (!manager.isOpen()) {
                manager = EntityManagerProducer.getEntityManager();
            }
            
            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(user);
            manager.getTransaction().commit();

        } catch (Exception e) {
            manager.getTransaction().rollback();

            LOGGER.error("[Exception] " + e.getMessage());
        }
    }

    /**
     * Retrieves a List of Users
     *
     * @return List< User >
     */
    @Override
    public List<User> list() {
        TypedQuery<User> query = manager.createQuery("SELECT u FROM User u", User.class);

        return query.getResultList();
    }

    /**
     * Updates an User
     *
     * @param user
     */
    @Override
    public void update(User user) {
        // TODO Auto-generated method stub
    }

    /**
     * Deletes an User
     *
     * @param user
     */
    @Override
    public void delete(User user) {
        // TODO Auto-generated method stub
    }

    /**
     * Finds an user by ID
     *
     * @param id
     * @return
     */
    @Override
    public User findById(Long id) {
        return manager.find(User.class, id);
    }

    /**
     * Returns data from User identified by login
     *
     * @param login
     * @return
     * @throws java.lang.Exception
     */
    public User findByLogin(String login) throws Exception {
        TypedQuery<User> query = manager.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class);
        query.setParameter("login", login);

        User userFromDB = query.getSingleResult();

        // If it is not null, sets File list and Workflow list
        if (userFromDB != null) {
            userFromDB.setFiles(new FileDao().listByUserId(userFromDB.getId()));
            userFromDB.setWorkflows(new WorkflowDao().listByUserId(userFromDB.getId()));

            return userFromDB;
        } else {
            return null;
        }
    }

    /**
     * Checks if the user exists
     *
     * @param login
     * @return
     */
    public boolean exists(String login) {
        for (User u : list()) {
            if (u.getLogin().equals(login)) {
                return true;
            }
        }
        
        return false;
    }

}