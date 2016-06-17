package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

/**
 * Created by lameroot on 01.06.16.
 */
public class BindingPaymentRequest extends PaymentRequest<BindingPaymentParams> {

    @Override
    public PaymentWay getPaymentWay() {
        return PaymentWay.BINDING;
    }
}
