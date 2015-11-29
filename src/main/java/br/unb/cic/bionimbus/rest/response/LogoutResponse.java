package br.unb.cic.bionimbus.rest.response;

/**
 * A Logout response definition
 * @author Vinicius
 */
public class LogoutResponse implements ResponseInfo {

    private boolean logoutSuccess;

    public LogoutResponse() {

    }

    public boolean isLogoutSuccess() {
        return logoutSuccess;
    }

    public void setLogoutSuccess(boolean logoutSuccess) {
        this.logoutSuccess = logoutSuccess;
    }
}
