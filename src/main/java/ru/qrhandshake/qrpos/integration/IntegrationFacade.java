package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.IntegrationPaymentRequest;
import ru.qrhandshake.qrpos.dto.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.exception.IntegrationException;

/**
 * Created by lameroot on 19.05.16.
 */
public interface IntegrationFacade {

    IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException;
    IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException;
    IntegrationSupport getIntegrationSupport();
    OrderStatus toOrderStatus(IntegrationOrderStatus integrationOrderStatus);
    boolean isApplicable();
}
