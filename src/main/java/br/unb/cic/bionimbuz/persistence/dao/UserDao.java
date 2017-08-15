package br.unb.cic.bionimbuz.persistence.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.persistence.EntityManagerProducer;

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
            // Creates entity manager
            manager = EntityManagerProducer.getEntityManager();

            // Get a Transaction, persist and commit
            manager.getTransaction().begin();
            manager.persist(user);
            manager.getTransaction().commit();

        } catch (Exception e) {
            manager.getTransaction().rollback();
            LOGGER.error("[Exception] " + e.getMessage());

        } finally {
            // Close connection
            manager.close();
        }
    }

    /**
     * Retrieves a List of Users
     *
     * @return List< User >
     */
    @Override
    public List<User> list() {
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        TypedQuery<User> query = manager.createQuery("SELECT u FROM User u", User.class);
        List<User> result = query.getResultList();

        // Close connection
        manager.close();

        return result;
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
        // Creates entity manager
        manager = EntityManagerProducer.getEntityManager();

        User u = manager.find(User.class, id);

        // Close connection
        manager.close();

        return u;
    }

    /**
     * Returns data from User identified by login
     *
     * @param login
     * @return
     * @throws java.lang.Exception
     */
    public User findByLogin(String login) {

        manager = EntityManagerProducer.getEntityManager();
        TypedQuery<User> query = manager.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class);
        query.setParameter("login", login);
        User userFromDB = null;
        try {
            userFromDB = query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            // Noting to do
        }
        manager.close();
        return userFromDB;
    }

    /**
     * Checks if the user exists
     *
     * @param login
     * @return
     */
    public boolean exists(String login) {
        return this.findByLogin(login) != null;
    }

}
