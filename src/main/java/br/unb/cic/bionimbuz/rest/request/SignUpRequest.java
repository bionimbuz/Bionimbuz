package br.unb.cic.bionimbuz.rest.request;

import br.unb.cic.bionimbuz.model.User;

/**
 * Defines a sign up information request
 *
 * @author Vinicius
 */
public class SignUpRequest extends BaseRequest {

    private User user;

    public SignUpRequest() {
    }

    public SignUpRequest(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
