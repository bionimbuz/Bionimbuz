package br.unb.cic.bionimbus.rest.request;

import br.unb.cic.bionimbus.rest.model.User;

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
