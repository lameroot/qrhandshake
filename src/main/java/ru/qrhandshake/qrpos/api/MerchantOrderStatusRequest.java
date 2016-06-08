package ru.qrhandshake.qrpos.api;


import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 20.05.16.
 */
public class MerchantOrderStatusRequest extends ApiAuth {

    @NotNull
    private String orderId;
    private boolean externalRequest;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isExternalRequest() {
        return externalRequest;
    }

    public void setExternalRequest(boolean externalRequest) {
        this.externalRequest = externalRequest;
    }
}
