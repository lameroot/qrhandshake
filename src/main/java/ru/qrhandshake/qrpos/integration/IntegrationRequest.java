package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 26.05.16.
 */
public class IntegrationRequest {

    @NotNull
    private IntegrationSupport integrationSupport;

    public IntegrationRequest(IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }

    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    public void setIntegrationSupport(IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }
}
