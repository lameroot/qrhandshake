package ru.qrhandshake.qrpos.api;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 17.08.16.
 */
public class FinishParams {

    @NotNull
    private String orderId;

    public FinishParams() {
    }

    public FinishParams(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
