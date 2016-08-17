package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 17.08.16.
 */
public class BindingCreateRequest extends ApiAuth {

    @NotNull
    private PaymentWay paymentWay = PaymentWay.CARD;

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }
}
