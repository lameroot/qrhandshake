package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;


/**
 * Created by lameroot on 30.05.16.
 */
public class CardPaymentRequest extends PaymentRequest<CardPaymentParams> {

    @Override
    public PaymentWay getPaymentWay() {
        return PaymentWay.card;
    }
}
