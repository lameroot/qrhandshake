package ru.qrhandshake.qrpos.api;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 21.06.16.
 */
public class SessionStatusRequest {

    @NotNull
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
