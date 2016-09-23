package ru.qrhandshake.qrpos.api.endpoint;

import ru.qrhandshake.qrpos.domain.EndpointCatalog;
import ru.qrhandshake.qrpos.domain.Merchant;

/**
 * Created by lameroot on 24.09.16.
 */
public class EndpointRegisterParams {

    private EndpointCatalog endpointCatalog;
    private Merchant merchant;
    private Object credentials;

    public EndpointCatalog getEndpointCatalog() {
        return endpointCatalog;
    }

    public void setEndpointCatalog(EndpointCatalog endpointCatalog) {
        this.endpointCatalog = endpointCatalog;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Object getCredentials() {
        return credentials;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }
}
