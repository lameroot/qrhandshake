package ru.qrhandshake.qrpos.api.endpoint;

import ru.qrhandshake.qrpos.api.AbstractResult;
import ru.qrhandshake.qrpos.domain.Endpoint;

/**
 * Created by lameroot on 24.09.16.
 */
public class EndpointRegisterResult extends AbstractResult {

    private Endpoint endpoint;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
