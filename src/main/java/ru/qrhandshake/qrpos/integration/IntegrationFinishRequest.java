package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 28.05.16.
 */
public class IntegrationFinishRequest extends IntegrationRequest {

    private String orderId;
    private String externalId;

    public IntegrationFinishRequest(IntegrationSupport integrationSupport, String externalId) {
        super(integrationSupport);
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
