package ru.qrhandshake.qrpos.integration.rbs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import ru.bpc.phoenix.proxy.api.MerchantServiceProvider;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.bpc.phoenix.web.api.merchant.soap.MerchantService;
import ru.paymentgate.engine.webservices.merchant.*;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.IntegrationOrderStatusRequest;
import ru.qrhandshake.qrpos.dto.IntegrationOrderStatusResponse;
import ru.qrhandshake.qrpos.dto.IntegrationPaymentRequest;
import ru.qrhandshake.qrpos.dto.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.exception.IllegalOrderStatusException;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.integration.IntegrationOrderStatus;
import ru.qrhandshake.qrpos.integration.IntegrationSupport;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by lameroot on 19.05.16.
 */
public class RbsIntegrationFacade implements IntegrationFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Environment environment;
    private MerchantServiceProvider merchantServiceProvider;
    private final NamePasswordToken namePasswordToken;
    private MerchantService merchantService;
    private IntegrationSupport integrationSupport;

    public RbsIntegrationFacade(NamePasswordToken namePasswordToken, String wsdlLocation, IntegrationSupport integrationSupport) {
        this.namePasswordToken = namePasswordToken;
        this.merchantServiceProvider = new MerchantServiceProvider();
        this.merchantServiceProvider.setWsdlLocation(wsdlLocation);
        this.integrationSupport = integrationSupport;
    }

    MerchantService getMerchantService() {
        if ( null == merchantService ) {
            merchantService = merchantServiceProvider.getMerchantService(namePasswordToken);
        }
        return merchantService;
    }

    @Override
    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException{
        OrderParams rbsParams = new OrderParams();
        //rbsParams.setCurrency(integrationPaymentRequest.getCurrency());
        rbsParams.setCurrency("643");
        rbsParams.setLanguage(integrationPaymentRequest.getLanguage());
        rbsParams.setDescription(integrationPaymentRequest.getDescription());
        rbsParams.setAmount(integrationPaymentRequest.getAmount());
        //rbsParams.setReturnUrl(integrationPaymentRequest.getReturnUrl());
        rbsParams.setReturnUrl("http://ya.ru");
        rbsParams.setMerchantOrderNumber(integrationPaymentRequest.getOrderId());
        for (Map.Entry<String, String> entry : integrationPaymentRequest.getParams().entrySet()) {
            ServiceParam serviceParam = new ServiceParam();
            serviceParam.setName(entry.getKey());
            serviceParam.setValue(entry.getValue());
            rbsParams.getParams().add(serviceParam);
        }
        RegisterOrderResponse registerOrderResponse = getMerchantService().registerOrder(rbsParams);
        if ( 0 != registerOrderResponse.getErrorCode() ) {
            throw new IntegrationException("Error register order: " + integrationPaymentRequest.getOrderId() + " because : " + registerOrderResponse.getErrorMessage());
        }
        String orderId = registerOrderResponse.getOrderId();

        PaymentOrderParams paymentOrderParams = new PaymentOrderParams();
        paymentOrderParams.setPan(integrationPaymentRequest.getPan());
        paymentOrderParams.setCardholderName(integrationPaymentRequest.getCardHolderName());
        paymentOrderParams.setCvc(integrationPaymentRequest.getCvc());
        paymentOrderParams.setLanguage(integrationPaymentRequest.getLanguage());
        paymentOrderParams.setMonth(integrationPaymentRequest.getMonth());
        paymentOrderParams.setYear(integrationPaymentRequest.getYear());
        paymentOrderParams.setOrderId(orderId);
        if ( null != integrationPaymentRequest.getClient() ) {
            paymentOrderParams.setEmail(integrationPaymentRequest.getClient().getEmail());
            paymentOrderParams.setIp(integrationPaymentRequest.getClient().getIp());
        }
        PaymentOrderResult paymentOrderResult = getMerchantService().paymentOrder(paymentOrderParams);
        if ( 0 != paymentOrderResult.getErrorCode() ) {
            throw new IntegrationException("Error payment order: " + integrationPaymentRequest.getOrderId() + " because: " + paymentOrderResult.getErrorMessage());
        }

        IntegrationPaymentResponse response = new IntegrationPaymentResponse();
        response.setOrderId(orderId);
        response.setAcsUrl(paymentOrderResult.getAcsUrl());
        response.setPaReq(paymentOrderResult.getPaReq());
        response.setTermUrl(paymentOrderResult.getRedirect());
        if (StringUtils.isNotBlank(paymentOrderResult.getAcsUrl()) ) {
            response.setOrderStatus(RbsOrderStatus.REDIRECTED_TO_ACS);
        }
        else {
            IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(new IntegrationOrderStatusRequest(orderId));
            //todo: check status если невалидный, то делать еще раз несколько раз
        }

        return response;
    }

    @Override
    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationOrderStatusResponse integrationOrderStatusResponse = new IntegrationOrderStatusResponse();
        integrationOrderStatusResponse.setExternalId(integrationOrderStatusRequest.getExternalId());

        GetOrderStatusExtendedRequest getOrderStatusExtendedRequest = new GetOrderStatusExtendedRequest();
        getOrderStatusExtendedRequest.setOrderId(integrationOrderStatusRequest.getExternalId());
        GetOrderStatusExtendedResponse getOrderStatusExtendedResponse = getMerchantService().getOrderStatusExtended(getOrderStatusExtendedRequest);
        if ( !"0".equals(getOrderStatusExtendedResponse.getErrorCode()) ) {
            integrationOrderStatusResponse.setOrderStatus(null);
        }
        RbsOrderStatus rbsOrderStatus = RbsOrderStatus.valueOf(getOrderStatusExtendedResponse.getOrderStatus());
        integrationOrderStatusResponse.setOrderStatus(rbsOrderStatus);

        return integrationOrderStatusResponse;
    }

    @Override
    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    @Override
    public boolean isApplicable() {
        return environment.acceptsProfiles(RbsIntegrationConfig.RBS_PROFILE);
    }

    @Override
    public OrderStatus toOrderStatus(IntegrationOrderStatus integrationOrderStatus) {
        if ( integrationOrderStatus instanceof RbsOrderStatus ) {
            RbsOrderStatus rbsOrderStatus = (RbsOrderStatus)integrationOrderStatus;
            switch (rbsOrderStatus) {
                case CREATED: return OrderStatus.REGISTERED;
                case DEPOSITED: return OrderStatus.PAID;
                case APPROVED: return OrderStatus.PAID;
                case REFUNDED: return OrderStatus.REFUNDED;
                case REVERSED: return OrderStatus.REVERSED;
                case REDIRECTED_TO_ACS: return OrderStatus.REGISTERED;
            }
            return OrderStatus.REGISTERED;
        }
        throw new IllegalArgumentException("Unknown integration order status: " + integrationOrderStatus);
    }

}
