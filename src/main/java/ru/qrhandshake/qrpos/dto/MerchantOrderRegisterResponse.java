package ru.qrhandshake.qrpos.dto;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterResponse extends MerchantResponse {

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
