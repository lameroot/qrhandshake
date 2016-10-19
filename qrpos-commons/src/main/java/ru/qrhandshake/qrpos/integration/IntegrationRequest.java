package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

import javax.validation.constraints.NotNull;

public class IntegrationRequest {

    @NotNull
    private Endpoint endpoint;

    public IntegrationRequest() {
    }
    public IntegrationRequest(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
