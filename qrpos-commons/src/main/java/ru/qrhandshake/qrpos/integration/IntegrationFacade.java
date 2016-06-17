package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;

/**
 * Created by lameroot on 19.05.16.
 */
public interface IntegrationFacade {

    IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException;
    IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException;
    IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException;
    IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException;
    IntegrationSupport getIntegrationSupport();
    OrderStatus toOrderStatus(IntegrationOrderStatus integrationOrderStatus);
    IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException;
    IntegrationP2PTransferResponse p2pTransfer(IntegrationP2PTransferRequest integrationP2PTransferRequest) throws IntegrationException;
    boolean isApplicable();
}