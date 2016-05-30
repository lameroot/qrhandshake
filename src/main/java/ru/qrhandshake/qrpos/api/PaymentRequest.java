package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 19.05.16.
 */
public class PaymentRequest<P extends PaymentParams> {

    @NotNull
    private String orderId;
    @NotNull
    private PaymentWay paymentWay = PaymentWay.card;
    @NotNull
    @Valid
    private P paymentParams;
    private String returnUrl;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    public P getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(P paymentParams) {
        this.paymentParams = paymentParams;
    }
}