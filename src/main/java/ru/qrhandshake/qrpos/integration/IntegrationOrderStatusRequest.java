package ru.qrhandshake.qrpos.integration;

/**
 * Created by lameroot on 20.05.16.
 */
public class IntegrationOrderStatusRequest {

    private String orderId;
    private String externalId;

    public IntegrationOrderStatusRequest() {

    }
    public IntegrationOrderStatusRequest(String externalId) {
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
