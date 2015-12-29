package br.unb.cic.bionimbus.jobcontroller;

import br.unb.cic.bionimbus.rest.model.User;
import java.util.HashMap;

/**
 * Controls logged user map
 *
 * @author Vinicius
 */
public class LoggedUserList {

    // Logged user Hash Map <id, user>
    private final HashMap<Long, User> loggedUserMap;

    public LoggedUserList() {
        loggedUserMap = new HashMap<>();
    }

    /**
     * Adds an user to the Logged User Map
     *
     * @param user
     */
    public void add(User user) {
        loggedUserMap.put(user.getId(), user);
    }

    /**
     * Removes and user from Logged User Map
     *
     * @param userId
     */
    public void remove(Long userId) {
        loggedUserMap.remove(userId);
    }

    /**
     * Verify if an user is logged or not
     *
     * @param userId
     * @return
     */
    public boolean verify(Long userId) {
        return loggedUserMap.containsKey(userId);
    }

    /**
     * Retrieves Logged User Map
     *
     * @return
     */
    public HashMap<Long, User> getLoggedUserMap() {
        return loggedUserMap;
    }

}
