package ru.qrhandshake.qrpos.converter;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import java.beans.PropertyEditorSupport;

/**
 * Created by lameroot on 03.06.16.
 */
public class PaymentWayConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String cap = text.toUpperCase();
        PaymentWay paymentWay = PaymentWay.valueOf(cap);
        setValue(paymentWay);
    }


}
