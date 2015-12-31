package br.unb.cic.bionimbus.rest.response;

import org.jboss.resteasy.core.ServerResponse;

/**
 * A Logout response definition
 *
 * @author Vinicius
 */
public class LogoutResponse extends ServerResponse implements ResponseInfo {

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
