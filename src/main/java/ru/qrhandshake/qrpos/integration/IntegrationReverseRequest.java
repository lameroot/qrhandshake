package ru.qrhandshake.qrpos.integration;

/**
 * Created by lameroot on 26.05.16.
 */
public class IntegrationReverseRequest {

    private String orderId;
    private String externalId;

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
