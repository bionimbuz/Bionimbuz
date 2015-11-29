package br.unb.cic.bionimbus.rest.response;

/**
 * A SignUp response definition
 * @author Vinicius
 */
public class SignUpResponse implements ResponseInfo {

    private boolean added;

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

}
