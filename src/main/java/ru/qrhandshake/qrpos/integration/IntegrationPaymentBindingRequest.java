package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lameroot on 31.05.16.
 */
public class IntegrationPaymentBindingRequest extends IntegrationPaymentRequest {

    private final String externalBindingId;
    private String bindingId;

    public IntegrationPaymentBindingRequest(IntegrationSupport integrationSupport, String externalBindingId) {
        super(integrationSupport);
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
}
