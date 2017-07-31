package br.unb.cic.bionimbuz.rest.response;

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

    public LogoutResponse(boolean logoutSuccess) {
        this.logoutSuccess = logoutSuccess;
    }

    public boolean isLogoutSuccess() {
        return logoutSuccess;
    }

    public void setLogoutSuccess(boolean logoutSuccess) {
        this.logoutSuccess = logoutSuccess;
    }
}
