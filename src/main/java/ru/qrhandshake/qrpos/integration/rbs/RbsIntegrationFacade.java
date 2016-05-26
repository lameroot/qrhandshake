package ru.qrhandshake.qrpos.integration.rbs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import ru.bpc.phoenix.proxy.api.MerchantServiceProvider;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.bpc.phoenix.web.api.merchant.soap.MerchantService;
import ru.paymentgate.engine.webservices.merchant.*;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.exception.IntegrationException;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by lameroot on 19.05.16.
 */
public class RbsIntegrationFacade implements IntegrationFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String language = "ru";
    private String currency = "643";

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
        rbsParams.setCurrency(currency);
        rbsParams.setLanguage(language);
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
        paymentOrderParams.setLanguage(language);
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
            response.setIntegrationOrderStatus(RbsOrderStatus.REDIRECTED_TO_ACS);
        }
        else {
            IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(new IntegrationOrderStatusRequest(orderId));
            //todo: check status если невалидный, то делать еще раз несколько раз
        }

        return response;
    }

    //todo: try-catch для всех блоков, где может вылезти ошибка
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
        integrationOrderStatusResponse.setIntegrationOrderStatus(rbsOrderStatus);

        return integrationOrderStatusResponse;
    }

    @Override
    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        ReversalOrderParams reversalOrderParams = new ReversalOrderParams();
        reversalOrderParams.setOrderId(integrationReverseRequest.getExternalId());
        try {
            IntegrationReverseResponse integrationReverseResponse = new IntegrationReverseResponse();
            integrationReverseResponse.setOrderId(integrationReverseRequest.getOrderId());
            integrationReverseResponse.setExternalId(integrationReverseRequest.getExternalId());
            OrderResult orderResult = getMerchantService().reverseOrder(reversalOrderParams);
            if ( 0 == orderResult.getErrorCode() ) {
                integrationReverseResponse.setSuccess(true);
            }
            integrationReverseResponse.setMessage(orderResult.getErrorMessage());
            return integrationReverseResponse;
        } catch (Exception e) {
            throw new IntegrationException("Error reverse orderId: " + integrationReverseRequest.getExternalId() + " by system: " + integrationSupport,e);
        }
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
