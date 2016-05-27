package ru.qrhandshake.qrpos.integration;

import org.springframework.ui.Model;
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
        integrationOrderStatusResponse.setOrderStatus(toOrderStatus(integrationSupport, integrationOrderStatusResponse.getIntegrationOrderStatus()));
        return integrationOrderStatusResponse;
    }

    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        IntegrationSupport integrationSupport = integrationReverseRequest.getIntegrationSupport();
        return getFacade(integrationSupport).reverse(integrationReverseRequest);
    }

    public void back(Model model, HttpServletRequest request) {

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
