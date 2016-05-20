package ru.qrhandshake.qrpos.integration;

import org.apache.commons.lang.StringUtils;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.IntegrationOrderStatusRequest;
import ru.qrhandshake.qrpos.dto.IntegrationOrderStatusResponse;
import ru.qrhandshake.qrpos.dto.IntegrationPaymentRequest;
import ru.qrhandshake.qrpos.dto.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.exception.IllegalOrderStatusException;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;
import ru.qrhandshake.qrpos.service.MerchantOrderService;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationService {

    @Resource
    private MerchantOrderService merchantOrderService;
    private final Map<IntegrationSupport, IntegrationFacade> integrationFacades;

    public IntegrationService(Map<IntegrationSupport, IntegrationFacade> integrationFacades) {
        this.integrationFacades = integrationFacades;
    }

    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException, IllegalOrderStatusException {
        OrderStatus orderStatus = integrationPaymentRequest.getOrderStatus();
        if ( null == orderStatus ) {
            throw new IntegrationException("Unknown status of order:" + integrationPaymentRequest.getOrderId());
        }
        if ( !orderStatus.equals(OrderStatus.CREATED) ) {
            throw new IllegalOrderStatusException("Illegal order status for payment order with orderId: " + integrationPaymentRequest.getOrderId(), orderStatus);
        }
        IntegrationSupport integrationSupport = checkIntegrationSupport(integrationPaymentRequest);
        IntegrationPaymentResponse integrationPaymentResponse = Optional.ofNullable(integrationFacades.get(integrationSupport))
                .orElseThrow(() -> new IntegrationException("Unknown integration type: " + integrationSupport))
                .payment(integrationPaymentRequest);
        return integrationPaymentResponse;
    }

    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        if ( StringUtils.isBlank(integrationOrderStatusRequest.getOrderId()) ) {
            throw new IntegrationException("Unknown orderId");
        }
        if (StringUtils.isBlank(integrationOrderStatusRequest.getExternalId()) ) {
            throw new IntegrationException("Unknown externalId");
        }
        MerchantOrder merchantOrder = null;
        try {
            merchantOrder = merchantOrderService.findMerchantOrderByGeneratedId(integrationOrderStatusRequest.getOrderId());
        } catch (MerchantOrderNotFoundException e) {
            throw new IntegrationException("Unknown order with id:" + integrationOrderStatusRequest.getOrderId(),e);
        }
        IntegrationSupport integrationSupport = merchantOrder.getIntegrationSupport();
        if ( null == integrationSupport ) {
            throw new IntegrationException("Unknown integration type for order with id: " + integrationOrderStatusRequest.getOrderId());
        }
        IntegrationOrderStatusResponse integrationOrderStatusResponse = Optional.ofNullable(integrationFacades.get(integrationSupport))
                .orElseThrow(()-> new IntegrationException("Unknown integration type:" + integrationSupport))
                .getOrderStatus(integrationOrderStatusRequest);
        return integrationOrderStatusResponse;
    }

    private IntegrationSupport checkIntegrationSupport(IntegrationPaymentRequest paymentRequest) {
        return IntegrationSupport.RBS_SBRF;//тут может быть проверка на тип карты и взависимости от этого выбор, через кого проводить операцию
    }

}
