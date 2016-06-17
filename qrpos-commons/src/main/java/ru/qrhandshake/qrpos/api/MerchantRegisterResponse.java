package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public class MerchantRegisterResponse extends ApiResponse {

    private String merchantId;
    private ApiAuth auth;//тот под который производился запрос
    private ApiAuth terminalAuth;
    private ApiAuth userAuth;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public ApiAuth getAuth() {
        return auth;
    }

    public void setAuth(ApiAuth auth) {
        this.auth = auth;
    }

    public ApiAuth getTerminalAuth() {
        return terminalAuth;
    }

    public void setTerminalAuth(ApiAuth terminalAuth) {
        this.terminalAuth = terminalAuth;
    }

    public ApiAuth getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(ApiAuth userAuth) {
        this.userAuth = userAuth;
    }
}
