package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 26.05.16.
 */
public class MerchantOrderReverseResponse extends ApiResponse {

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
