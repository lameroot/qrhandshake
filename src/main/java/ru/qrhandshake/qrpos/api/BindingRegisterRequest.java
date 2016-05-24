package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public class BindingRegisterRequest extends ApiAuth {

    private String paymentWay;
    private String paymentParams;

    public String getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(String paymentWay) {
        this.paymentWay = paymentWay;
    }

    public String getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(String paymentParams) {
        this.paymentParams = paymentParams;
    }
}
