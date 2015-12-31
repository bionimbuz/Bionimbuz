package br.unb.cic.bionimbus.rest.response;

import br.unb.cic.bionimbus.rest.model.User;

/**
 * A Login response definition
 *
 * @author Vinicius
 */
public class LoginResponse implements ResponseInfo {

    private User user;

    public LoginResponse() {
    }

    public LoginResponse(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
