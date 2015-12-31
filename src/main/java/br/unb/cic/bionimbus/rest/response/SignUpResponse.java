package br.unb.cic.bionimbus.rest.response;

import org.jboss.resteasy.core.ServerResponse;

/**
 * A SignUp response definition
 *
 * @author Vinicius
 */
public class SignUpResponse extends ServerResponse implements ResponseInfo {

    private boolean added;

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

}
