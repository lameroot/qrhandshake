package ru.qrhandshake.qrpos.api.binding;

import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.domain.PaymentWay;

import java.util.Set;

/**
 * Created by lameroot on 02.06.16.
 */
public class GetBindingsRequest extends ApiAuth {

    private Set<PaymentWay> paymentWays;

    public Set<PaymentWay> getPaymentWays() {
        return paymentWays;
    }

    public void setPaymentWays(Set<PaymentWay> paymentWays) {
        this.paymentWays = paymentWays;
    }
}
