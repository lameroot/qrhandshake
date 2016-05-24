package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public class MerchantRegisterResponse extends ApiResponse {

    private String merchantId;
    private ApiAuth auth;

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
}
