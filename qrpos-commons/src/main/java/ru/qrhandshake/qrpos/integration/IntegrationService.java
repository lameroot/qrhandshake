package ru.qrhandshake.qrpos.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;

import java.util.Map;
import java.util.Optional;

public class IntegrationService {

    private final static Logger logger = LoggerFactory.getLogger(IntegrationService.class);

    private final Map<IntegrationSupport, IntegrationFacade> integrationFacades;
    private final Map<IntegrationSupport, P2pIntegrationFacade> p2pIntegrationFacades;

    public IntegrationService(Map<IntegrationSupport, IntegrationFacade> integrationFacades, Map<IntegrationSupport, P2pIntegrationFacade> p2pIntegrationFacades) {
        this.integrationFacades = integrationFacades;
        this.p2pIntegrationFacades = p2pIntegrationFacades;
    }

    public IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
        logger.trace("Integration, paymentBinding: {}", integrationPaymentBindingRequest);
        IntegrationPaymentResponse integrationPaymentResponse = getFacade(integrationPaymentBindingRequest).paymentBinding(integrationPaymentBindingRequest);
        if ( !integrationPaymentResponse.isSuccess() ) throw new IntegrationException(integrationPaymentResponse.getMessage());
        if ( null == integrationPaymentResponse.getOrderStatus() ) integrationPaymentResponse.setOrderStatus(toOrderStatus(integrationPaymentBindingRequest,integrationPaymentResponse.getIntegrationOrderStatus()));
        return integrationPaymentResponse;
    }

    //todo: убрать все выбросы if ( !integrationPaymentResponse.isSuccess() ) throw new IntegrationException - сделать лучше заполнение статуса
    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException {
        IntegrationPaymentResponse integrationPaymentResponse = getFacade(integrationPaymentRequest).payment(integrationPaymentRequest);
        integrationPaymentResponse.setOrderStatus(toOrderStatus(integrationPaymentRequest, integrationPaymentResponse.getIntegrationOrderStatus()));
        return integrationPaymentResponse;
    }

    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationOrderStatusResponse integrationOrderStatusResponse = getFacade(integrationOrderStatusRequest).getOrderStatus(integrationOrderStatusRequest);
        integrationOrderStatusResponse.setOrderId(integrationOrderStatusRequest.getOrderId());
        if ( !integrationOrderStatusResponse.isSuccess() ) throw new IntegrationException(integrationOrderStatusResponse.getMessage());
        integrationOrderStatusResponse.setOrderStatus(toOrderStatus(integrationOrderStatusRequest, integrationOrderStatusResponse.getIntegrationOrderStatus()));
        return integrationOrderStatusResponse;
    }

    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        IntegrationReverseResponse integrationReverseResponse = getFacade(integrationReverseRequest).reverse(integrationReverseRequest);
        if ( !integrationReverseResponse.isSuccess() ) throw new IntegrationException(integrationReverseResponse.getMessage());
        return integrationReverseResponse;
    }

    public IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException {
        IntegrationCompletionResponse integrationCompletionResponse = getFacade(integrationCompletionRequest).completion(integrationCompletionRequest);
        integrationCompletionResponse.setOrderId(integrationCompletionRequest.getOrderId());
        if ( !integrationCompletionResponse.isSuccess() ) throw new IntegrationException(integrationCompletionResponse.getMessage());
        return integrationCompletionResponse;
    }

    public IntegrationP2PTransferResponse p2pTransfer(IntegrationP2PTransferRequest integrationP2PTransferRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationP2PTransferRequest.getEndpoint().getEndpointCatalog().getIntegrationSupport();
        IntegrationP2PTransferResponse integrationP2PTransferResponse = getP2pFacade(integrationSupport).p2pTransfer(integrationP2PTransferRequest);
        if ( !integrationP2PTransferResponse.isSuccess() ) throw new IntegrationException(integrationP2PTransferResponse.getMessage());
        return integrationP2PTransferResponse;
    }

    private IntegrationFacade getFacade(IntegrationRequest integrationRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationRequest.getEndpoint().getEndpointCatalog().getIntegrationSupport();
        if ( null == integrationRequest ) throw new IntegrationException("Unknown integration support");
        return Optional.ofNullable(integrationFacades.get(integrationSupport))
                .orElseThrow(() -> new IntegrationException("Unknown integration type: " + integrationSupport));
    }

    private P2pIntegrationFacade getP2pFacade(IntegrationSupport integrationSupport) throws IntegrationException {
        return Optional.ofNullable(p2pIntegrationFacades.get(integrationSupport))
                .orElseThrow(() -> new IntegrationException("Unknown integration type: " + integrationSupport));
    }

    private OrderStatus toOrderStatus(IntegrationRequest integrationRequest, IntegrationOrderStatus integrationOrderStatus) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationRequest.getEndpoint().getEndpointCatalog().getIntegrationSupport();
        if ( null == integrationRequest ) throw new IntegrationException("Unknown integration support");
        return Optional.ofNullable(integrationFacades.get(integrationSupport))
                .orElseThrow(()-> new IntegrationException("Unknown integration type: " + integrationSupport))
                .toOrderStatus(integrationOrderStatus);
    }



}
