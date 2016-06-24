package ru.qrhandshake.qrpos.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lameroot on 30.05.16.
 */
public class PaymentParams {
    private String orderId;
    private String ip;
    private String returnUrl;
    private String paymentAccount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    @JsonProperty
    public String getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount = paymentAccount;
    }
}
