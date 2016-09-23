package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 20.05.16.
 */
public class IntegrationOrderStatusRequest extends IntegrationRequest {

    private String orderId;
    @NotNull
    private String externalId;

    public IntegrationOrderStatusRequest(Endpoint endpoint) {
        super(endpoint);

    }
    public IntegrationOrderStatusRequest(Endpoint endpoint, String externalId) {
        super(endpoint);
        this.externalId = externalId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
