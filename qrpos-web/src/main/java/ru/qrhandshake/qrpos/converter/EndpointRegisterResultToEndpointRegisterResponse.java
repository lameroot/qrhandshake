package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResponse;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResult;

/**
 * Created by lameroot on 24.09.16.
 */
@Component
public class EndpointRegisterResultToEndpointRegisterResponse implements Converter<EndpointRegisterResult, EndpointRegisterResponse> {

    @Override
    public EndpointRegisterResponse convert(EndpointRegisterResult endpointRegisterResult) {
        EndpointRegisterResponse endpointRegisterResponse = new EndpointRegisterResponse();
        endpointRegisterResponse.setStatus(endpointRegisterResult.getCode() == 1 ? ResponseStatus.SUCCESS : ResponseStatus.FAIL);
        endpointRegisterResponse.setMessage(endpointRegisterResult.getMessage());
        endpointRegisterResponse.setEndpointId(null != endpointRegisterResult.getEndpoint() ? endpointRegisterResult.getEndpoint().getId() : null);
        return endpointRegisterResponse;
    }
}
