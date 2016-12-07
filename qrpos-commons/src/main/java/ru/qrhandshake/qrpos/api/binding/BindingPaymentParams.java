package ru.qrhandshake.qrpos.api.binding;

import ru.qrhandshake.qrpos.api.PaymentParams;

/**
 * Created by lameroot on 01.06.16.
 */
public class BindingPaymentParams extends PaymentParams {

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
    public String toString() {
        final StringBuilder sb = new StringBuilder("BindingPaymentParams{");
        sb.append("bindingId='").append(bindingId).append('\'');
        sb.append(", confirmValue='").append(confirmValue).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
