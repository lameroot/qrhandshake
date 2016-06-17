package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 19.05.16.
 */
public class PaymentRequest extends ApiAuth {

    @NotNull
    private String orderId;
    @NotNull
    private PaymentWay paymentWay = PaymentWay.CARD;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

}
