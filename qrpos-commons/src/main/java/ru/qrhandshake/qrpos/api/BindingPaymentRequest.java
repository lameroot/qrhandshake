package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 01.06.16.
 */
public class BindingPaymentRequest extends PaymentRequest {

    @NotNull
    private String bindingId;

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    @Override
    public PaymentWay getPaymentWay() {
        return PaymentWay.BINDING;
    }
}
