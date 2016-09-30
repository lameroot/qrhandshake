package ru.qrhandshake.qrpos.api.binding;

import ru.qrhandshake.qrpos.api.PaymentRequest;
import ru.qrhandshake.qrpos.domain.PaymentWay;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 01.06.16.
 */
public class BindingPaymentRequest extends PaymentRequest {

    @NotNull
    private String bindingId;
    private String confirmValue;

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getConfirmValue() {
        return confirmValue;
    }

    public void setConfirmValue(String confirmValue) {
        this.confirmValue = confirmValue;
    }

    @Override
    public PaymentWay getPaymentWay() {
        return PaymentWay.BINDING;
    }
}
