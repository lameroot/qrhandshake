package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 09.06.16.
 */
public class IntegrationP2PTransferRequest extends IntegrationRequest {

    private PaymentParams paymentParams;
    private String to;
    private Long amount;
    private String description;
    private String email;

    public IntegrationP2PTransferRequest(IntegrationSupport integrationSupport, PaymentParams paymentParams, String to, Long amount) {
        super(integrationSupport);
        this.paymentParams = paymentParams;
        this.to = to;
        this.amount = amount;
    }

    public PaymentParams getPaymentParams() {
        return paymentParams;
    }

    public String getTo() {
        return to;
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
