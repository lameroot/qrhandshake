package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.PaymentWay;

/**
 * Created by lameroot on 02.06.16.
 */
public class BindingDto {

    private String bindingId;
    private PaymentWay paymentWay;
    private PaymentParams paymentParams;

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    public PaymentParams getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(PaymentParams paymentParams) {
        this.paymentParams = paymentParams;
    }
}
