package br.unb.cic.bionimbuz.rest.request;

public abstract class BaseRequest implements RequestInfo {

    private String securityToken;

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

}
