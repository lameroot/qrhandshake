package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 31.05.16.
 */
public class IntegrationPaymentBindingRequest extends IntegrationPaymentRequest {

    private final String externalBindingId;
    private String bindingId;

    public IntegrationPaymentBindingRequest(Endpoint endpoint, String externalBindingId) {
        super(endpoint);
        this.externalBindingId = externalBindingId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public String getExternalBindingId() {
        return externalBindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IntegrationPaymentBindingRequest{");
        sb.append(super.toString()).append('\'');
        sb.append("externalBindingId='").append(externalBindingId).append('\'');
        sb.append(", bindingId='").append(bindingId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
