package ru.qrhandshake.qrpos.integration;

import org.springframework.ui.Model;
import ru.qrhandshake.qrpos.api.FinishRequest;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.service.IntegrationSupportService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationPaymentRequest.getIntegrationSupport();
        IntegrationPaymentResponse integrationPaymentResponse = getFacade(integrationSupport).payment(integrationPaymentRequest);
        integrationPaymentResponse.setOrderStatus(toOrderStatus(integrationSupport,integrationPaymentResponse.getIntegrationOrderStatus()));
        return integrationPaymentResponse;
    }

    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationOrderStatusRequest.getIntegrationSupport();
        IntegrationOrderStatusResponse integrationOrderStatusResponse = getFacade(integrationSupport).getOrderStatus(integrationOrderStatusRequest);
        integrationOrderStatusResponse.setOrderId(integrationOrderStatusRequest.getOrderId());
        integrationOrderStatusResponse.setOrderStatus(toOrderStatus(integrationSupport, integrationOrderStatusResponse.getIntegrationOrderStatus()));
        return integrationOrderStatusResponse;
    }

    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationReverseRequest.getIntegrationSupport();
        return getFacade(integrationSupport).reverse(integrationReverseRequest);
    }

    public IntegrationFinishResponse finish(IntegrationFinishRequest integrationFinishRequest) throws IntegrationException {
        IntegrationOrderStatusRequest integrationOrderStatusRequest = new IntegrationOrderStatusRequest(integrationFinishRequest.getIntegrationSupport(), integrationFinishRequest.getExternalId());
        IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(integrationOrderStatusRequest);
        IntegrationFinishResponse integrationFinishResponse = new IntegrationFinishResponse();
        integrationFinishResponse.setOrderId(integrationOrderStatusResponse.getOrderId());
        integrationFinishResponse.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
        integrationFinishResponse.setSuccess(true);
        integrationFinishResponse.setMessage("Finish payment for orderId: " + integrationFinishRequest.getOrderId() + " success");

        return integrationFinishResponse;
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
