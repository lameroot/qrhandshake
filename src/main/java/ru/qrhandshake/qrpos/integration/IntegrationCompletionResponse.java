package ru.qrhandshake.qrpos.integration;

/**
 * Created by lameroot on 09.06.16.
 */
public class IntegrationCompletionResponse extends IntegrationResponse {

    private String orderId;
    private String externalOrderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
    }
}
