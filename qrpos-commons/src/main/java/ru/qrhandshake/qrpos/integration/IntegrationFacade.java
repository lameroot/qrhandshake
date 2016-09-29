package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;

public interface IntegrationFacade {

    IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException;
    IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException;
    IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException;
    IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException;
    IntegrationSupport getIntegrationSupport();
    OrderStatus toOrderStatus(IntegrationOrderStatus integrationOrderStatus);
    IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException;
    boolean isApplicable();

}
