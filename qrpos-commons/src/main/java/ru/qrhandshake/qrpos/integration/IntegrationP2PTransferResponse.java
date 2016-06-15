package ru.qrhandshake.qrpos.integration;

/**
 * Created by lameroot on 09.06.16.
 */
public class IntegrationP2PTransferResponse extends IntegrationResponse {

    private String externalOrderId;
    private String orderNumber;

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
