package ru.qrhandshake.qrpos.api.endpoint;

import ru.qrhandshake.qrpos.api.ApiResponse;

/**
 * Created by lameroot on 24.09.16.
 */
public class EndpointRegisterResponse extends ApiResponse {

    private Long endpointId;

    public Long getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(Long endpointId) {
        this.endpointId = endpointId;
    }
}
