package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.PaymentWay;

import java.util.Set;

/**
 * Created by lameroot on 02.06.16.
 */
public class GetBindingsRequest  {

    private Client client;
    private Set<PaymentWay> paymentWays;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Set<PaymentWay> getPaymentWays() {
        return paymentWays;
    }

    public void setPaymentWays(Set<PaymentWay> paymentWays) {
        this.paymentWays = paymentWays;
    }
}
