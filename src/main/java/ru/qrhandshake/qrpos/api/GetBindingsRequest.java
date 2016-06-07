package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import java.util.Set;

/**
 * Created by lameroot on 02.06.16.
 */
public class GetBindingsRequest  {

    private Set<PaymentWay> paymentWays;

    public Set<PaymentWay> getPaymentWays() {
        return paymentWays;
    }

    public void setPaymentWays(Set<PaymentWay> paymentWays) {
        this.paymentWays = paymentWays;
    }
}
