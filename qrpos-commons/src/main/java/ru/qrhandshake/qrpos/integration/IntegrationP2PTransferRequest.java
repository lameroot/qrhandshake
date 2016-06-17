package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 09.06.16.
 */
public class IntegrationP2PTransferRequest extends IntegrationRequest {

    private CardPaymentParams paymentParams;
    private String toCardPan;
    private Long amount;
    private String description;
    private String email;

    public IntegrationP2PTransferRequest(IntegrationSupport integrationSupport, CardPaymentParams paymentParams, String toCardPan, Long amount) {
        super(integrationSupport);
        this.paymentParams = paymentParams;
        this.toCardPan = toCardPan;
        this.amount = amount;
    }

    public CardPaymentParams getPaymentParams() {
        return paymentParams;
    }

    public String getToCardPan() {
        return toCardPan;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
