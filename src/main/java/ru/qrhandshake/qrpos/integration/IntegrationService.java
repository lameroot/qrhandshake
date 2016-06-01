package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;

import java.util.Map;
import java.util.Optional;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationService {


    private final Map<IntegrationSupport, IntegrationFacade> integrationFacades;

    public IntegrationService(Map<IntegrationSupport, IntegrationFacade> integrationFacades) {
        this.integrationFacades = integrationFacades;
    }


    public IntegrationPaymentBindingResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
        return null;
    }
    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationPaymentRequest.getIntegrationSupport();
        IntegrationPaymentResponse integrationPaymentResponse = getFacade(integrationSupport).payment(integrationPaymentRequest);
        if ( !integrationPaymentResponse.isSuccess() ) throw new IntegrationException(integrationPaymentResponse.getMessage());
        integrationPaymentResponse.setOrderStatus(toOrderStatus(integrationSupport,integrationPaymentResponse.getIntegrationOrderStatus()));
        return integrationPaymentResponse;
    }

    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationOrderStatusRequest.getIntegrationSupport();
        IntegrationOrderStatusResponse integrationOrderStatusResponse = getFacade(integrationSupport).getOrderStatus(integrationOrderStatusRequest);
        integrationOrderStatusResponse.setOrderId(integrationOrderStatusRequest.getOrderId());
        if ( !integrationOrderStatusResponse.isSuccess() ) throw new IntegrationException(integrationOrderStatusResponse.getMessage());
        integrationOrderStatusResponse.setOrderStatus(toOrderStatus(integrationSupport, integrationOrderStatusResponse.getIntegrationOrderStatus()));
        return integrationOrderStatusResponse;
    }

    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationReverseRequest.getIntegrationSupport();
        IntegrationReverseResponse integrationReverseResponse = getFacade(integrationSupport).reverse(integrationReverseRequest);
        if ( !integrationReverseResponse.isSuccess() ) throw new IntegrationException(integrationReverseResponse.getMessage());
        return integrationReverseResponse;
    }

    private IntegrationFacade getFacade(IntegrationSupport integrationSupport) throws IntegrationException {
        return Optional.ofNullable(integrationFacades.get(integrationSupport))
                .orElseThrow(() -> new IntegrationException("Unknown integration type: " + integrationSupport));
    }

    private OrderStatus toOrderStatus(IntegrationSupport integrationSupport, IntegrationOrderStatus integrationOrderStatus) throws IntegrationException {
        return Optional.ofNullable(integrationFacades.get(integrationSupport))
                .orElseThrow(()-> new IntegrationException("Unknown integration type: " + integrationSupport))
                .toOrderStatus(integrationOrderStatus);
    }



}
