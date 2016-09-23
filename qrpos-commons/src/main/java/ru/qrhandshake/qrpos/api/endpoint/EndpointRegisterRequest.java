package ru.qrhandshake.qrpos.api.endpoint;

import ru.qrhandshake.qrpos.api.ApiAuth;

/**
 * Created by lameroot on 24.09.16.
 */
public class EndpointRegisterRequest extends ApiAuth {

    private Long endpointCatalogId;
    private Long merchantId;
    private String credentials;

    public Long getEndpointCatalogId() {
        return endpointCatalogId;
    }

    public void setEndpointCatalogId(Long endpointCatalogId) {
        this.endpointCatalogId = endpointCatalogId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
}
