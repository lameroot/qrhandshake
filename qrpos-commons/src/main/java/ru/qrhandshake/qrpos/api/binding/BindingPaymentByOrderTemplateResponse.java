package ru.qrhandshake.qrpos.api.binding;

import ru.qrhandshake.qrpos.api.ApiResponse;

/**
 * Created by lameroot on 09.08.16.
 */
public class BindingPaymentByOrderTemplateResponse extends ApiResponse {

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
