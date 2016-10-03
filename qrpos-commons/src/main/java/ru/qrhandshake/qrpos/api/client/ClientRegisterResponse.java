package ru.qrhandshake.qrpos.api.client;

import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.api.ApiResponse;

/**
 * Created by lameroot on 24.05.16.
 */
public class ClientRegisterResponse extends ApiResponse {

    private ApiAuth auth;
    private String confirmCode;

    public ApiAuth getAuth() {
        return auth;
    }

    public void setAuth(ApiAuth auth) {
        this.auth = auth;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }
}
