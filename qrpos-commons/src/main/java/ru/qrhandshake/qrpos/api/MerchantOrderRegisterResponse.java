package ru.qrhandshake.qrpos.api;


/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterResponse extends ApiResponse {

    private String orderId;
    private String paymentUrl;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

}
