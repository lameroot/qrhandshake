package ru.qrhandshake.qrpos.api;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 28.05.16.
 */
public class FinishRequest {

    @NotNull
    private String orderId;

    public FinishRequest() {
    }

    public FinishRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
