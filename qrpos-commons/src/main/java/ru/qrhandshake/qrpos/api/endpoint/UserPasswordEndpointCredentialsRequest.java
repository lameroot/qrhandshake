package ru.qrhandshake.qrpos.api.endpoint;

/**
 * Created by lameroot on 24.09.16.
 */
public class UserPasswordEndpointCredentialsRequest {

    private String username;
    private String password;

    public UserPasswordEndpointCredentialsRequest() {
    }

    public UserPasswordEndpointCredentialsRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
