package ru.qrhandshake.qrpos.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterParams;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterRequest;
import ru.qrhandshake.qrpos.api.endpoint.UserPasswordEndpointCredentialsRequest;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.repository.EndpointCatalogRepository;
import ru.qrhandshake.qrpos.repository.MerchantRepository;

import javax.annotation.Resource;

/**
 * Created by lameroot on 24.09.16.
 */
@Component
public class EndpointRegisterRequestToEndpointRegisterParams implements Converter<EndpointRegisterRequest,EndpointRegisterParams> {

    @Resource
    private EndpointCatalogRepository endpointCatalogRepository;
    @Resource
    private MerchantRepository merchantRepository;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public EndpointRegisterParams convert(EndpointRegisterRequest endpointRegisterRequest) {
        try {
            EndpointRegisterParams endpointRegisterParams = new EndpointRegisterParams();
            endpointRegisterParams.setCredentials(endpointRegisterRequest.getCredentials());
            endpointRegisterParams.setEndpointCatalog(endpointCatalogRepository.findOne(endpointRegisterRequest.getEndpointCatalogId()));
            endpointRegisterParams.setMerchant(merchantRepository.findOne(endpointRegisterRequest.getMerchantId()));
            if (IntegrationSupport.isUserPasswordCredentials(endpointRegisterParams.getEndpointCatalog().getIntegrationSupport())) {
                UserPasswordEndpointCredentialsRequest userPasswordEndpointCredentialsRequest = objectMapper.readValue(endpointRegisterRequest.getCredentials(), UserPasswordEndpointCredentialsRequest.class);
                endpointRegisterParams.setCredentials(userPasswordEndpointCredentialsRequest);
            }
            return endpointRegisterParams;
        } catch (Exception e) {
            throw new RuntimeException("Error convert endpointRegisterRequest to endpointRegisterParams",e);
        }
    }
}
