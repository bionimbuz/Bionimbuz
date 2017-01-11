package br.unb.cic.bionimbuz.rest.response;

import br.unb.cic.bionimbuz.model.User;

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
