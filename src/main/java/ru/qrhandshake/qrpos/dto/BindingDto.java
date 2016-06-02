package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.PaymentWay;

/**
 * Created by lameroot on 02.06.16.
 */
public class BindingDto {

    private String bindingId;
    private PaymentWay paymentWay;
    private String paymentParams;

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

    public String getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(String paymentParams) {
        this.paymentParams = paymentParams;
    }
}
