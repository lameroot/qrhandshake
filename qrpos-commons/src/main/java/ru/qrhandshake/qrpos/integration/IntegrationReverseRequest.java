package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 26.05.16.
 */
public class IntegrationReverseRequest extends IntegrationRequest {

    private String orderId;
    private final String externalId;

    public IntegrationReverseRequest(Endpoint endpoint, String externalId) {
        super(endpoint);
        this.externalId = externalId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExternalId() {
        return externalId;
    }

}
