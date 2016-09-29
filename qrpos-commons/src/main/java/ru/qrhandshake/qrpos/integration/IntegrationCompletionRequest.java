package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 09.06.16.
 */
public class IntegrationCompletionRequest extends IntegrationRequest{

    private final String externalOrderId;
    private String orderId;
    private Long amount;


    public IntegrationCompletionRequest(Endpoint endpoint, String externalOrderId) {
        super(endpoint);
        this.externalOrderId = externalOrderId;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
