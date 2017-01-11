package br.unb.cic.bionimbuz.utils;

import br.unb.cic.bionimbuz.model.User;
import java.util.HashMap;

/**
 * Controls logged user map
 *
 * @author Vinicius
 */
public class LoggedUserList {

    // Logged user Hash Map <id, user>
    private static final HashMap<Long, User> loggedUserMap = new HashMap<>();

    /**
     * Adds an user to the Logged User Map
     *
     * @param user
     */
    public static void add(User user) {
        loggedUserMap.put(user.getId(), user);
    }

    /**
     * Removes and user from Logged User Map
     *
     * @param userId
     */
    public static void remove(Long userId) {
        loggedUserMap.remove(userId);
    }

    /**
     * Verify if an user is logged or not
     *
     * @param userId
     * @return
     */
    public static boolean verify(Long userId) {
        return loggedUserMap.containsKey(userId);
    }

    /**
     * Retrieves Logged User Map
     *
     * @return
     */
    public static HashMap<Long, User> getLoggedUserMap() {
        return loggedUserMap;
    }

}
