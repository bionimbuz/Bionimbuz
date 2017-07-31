package br.unb.cic.bionimbuz.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSH credentials to be used by GET and PUT operations (upload and download).
 *
 * @author Vinicius
 */
public class SSHCredentials {
    @JsonProperty("user")
    private String user;

    @JsonProperty("password")
    private String password;

    @JsonProperty("ssh-port")
    private String port;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
