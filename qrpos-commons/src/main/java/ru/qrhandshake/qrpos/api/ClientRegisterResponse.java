package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public class ClientRegisterResponse extends ApiResponse {

    private ApiAuth auth;

    public ApiAuth getAuth() {
        return auth;
    }

    public void setAuth(ApiAuth auth) {
        this.auth = auth;
    }
}
