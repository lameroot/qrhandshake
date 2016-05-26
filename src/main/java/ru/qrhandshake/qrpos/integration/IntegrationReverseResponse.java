package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 26.05.16.
 */
public class IntegrationReverseResponse {

    private String orderId;
    private String externalId;
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
