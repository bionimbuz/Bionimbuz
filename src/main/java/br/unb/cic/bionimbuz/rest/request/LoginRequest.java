package br.unb.cic.bionimbuz.rest.request;

import br.unb.cic.bionimbuz.model.User;

/**
 * Defines a Login Request to be used in a REST request
 * @author Vinicius
 */
public class LoginRequest extends BaseRequest {

    private User user;

    public LoginRequest() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
