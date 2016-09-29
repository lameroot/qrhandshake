package ru.qrhandshake.qrpos.service;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterParams;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResult;
import ru.qrhandshake.qrpos.api.endpoint.UserPasswordEndpointCredentialsRequest;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.UserPasswordEndpoint;
import ru.qrhandshake.qrpos.repository.EndpointRepository;

import javax.annotation.Resource;

/**
 * Created by lameroot on 24.09.16.
 */
@Service
public class EndpointService {

    @Resource
    private EndpointRepository endpointRepository;

    public EndpointRegisterResult register(EndpointRegisterParams endpointRegisterParams) {
        EndpointRegisterResult endpointRegisterResult = new EndpointRegisterResult();
        if (IntegrationSupport.isUserPasswordCredentials(endpointRegisterParams.getEndpointCatalog().getIntegrationSupport()) ) {
            UserPasswordEndpoint endpoint = new UserPasswordEndpoint();
            endpoint.setMerchant(endpointRegisterParams.getMerchant());
            endpoint.setEnabled(true);
            UserPasswordEndpointCredentialsRequest userPasswordEndpointCredentialsRequest = (UserPasswordEndpointCredentialsRequest)endpointRegisterParams.getCredentials();
            endpoint.setUsername(userPasswordEndpointCredentialsRequest.getUsername());
            endpoint.setPassword(userPasswordEndpointCredentialsRequest.getPassword());
            endpoint.setEndpointCatalog(endpointRegisterParams.getEndpointCatalog());

            endpointRepository.save(endpoint);

            endpointRegisterResult.setEndpoint(endpoint);
            endpointRegisterResult.setCode(1);
            endpointRegisterResult.setMessage("Endpoint was created successfully");
            return endpointRegisterResult;
        }

        endpointRegisterResult.setCode(0);
        endpointRegisterResult.setMessage("Unknown integration type: " + endpointRegisterParams.getEndpointCatalog().getIntegrationSupport());
        return endpointRegisterResult;
    }

}
