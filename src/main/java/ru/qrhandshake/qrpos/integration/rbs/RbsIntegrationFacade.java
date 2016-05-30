package ru.qrhandshake.qrpos.integration.rbs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import ru.bpc.phoenix.proxy.api.MerchantServiceProvider;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.bpc.phoenix.web.api.merchant.soap.MerchantService;
import ru.paymentgate.engine.webservices.merchant.*;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
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

    private final static String language = "en";
    private final static String currency = "643";
    private final static String ACS_REDIRECT_PAGE = "rbs/acs_redirect";

    @Resource
    private Environment environment;
    private MerchantServiceProvider merchantServiceProvider;
    private final NamePasswordToken namePasswordToken;
    private MerchantService merchantService;
    private IntegrationSupport integrationSupport;

    public RbsIntegrationFacade(NamePasswordToken namePasswordToken, String wsdlLocation, IntegrationSupport integrationSupport) {
        this.namePasswordToken = namePasswordToken;
        this.merchantServiceProvider = new MerchantServiceProvider();
        this.merchantServiceProvider.setDebugSoap(true);
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
        rbsParams.setReturnUrl(integrationPaymentRequest.getReturnUrl());
        rbsParams.setMerchantOrderNumber(integrationPaymentRequest.getOrderId());
        for (Map.Entry<String, String> entry : integrationPaymentRequest.getParams().entrySet()) {
            ServiceParam serviceParam = new ServiceParam();
            serviceParam.setName(entry.getKey());
            serviceParam.setValue(entry.getValue());
            rbsParams.getParams().add(serviceParam);
        }
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentRequest.getOrderId());

        String externalOrderId = null;
        try {
            RegisterOrderResponse registerOrderResponse = getMerchantService().registerOrder(rbsParams);
            integrationPaymentResponse.setMessage(registerOrderResponse.getErrorMessage());
            if ( 0 != registerOrderResponse.getErrorCode() ) {
                logger.error("Error register order: " + integrationPaymentRequest.getOrderId() + " because : " + registerOrderResponse.getErrorMessage());
                integrationPaymentResponse.setSuccess(false);
                return integrationPaymentResponse;
            }
            integrationPaymentResponse.setSuccess(true);
            externalOrderId = registerOrderResponse.getOrderId();
            integrationPaymentResponse.setExternalId(externalOrderId);
        } catch (Exception e) {
            throw new IntegrationException("Error integration register order by orderId:" + integrationPaymentRequest.getOrderId(),e);
        }

        CardPaymentParams paymentParams = (CardPaymentParams)integrationPaymentRequest.getPaymentParams();
        if ( null == paymentParams || !paymentParams.isNotBlank() ) {
            integrationPaymentResponse.setSuccess(false);
            integrationPaymentResponse.setMessage("Invalid payment params");
            return integrationPaymentResponse;
        }

        PaymentOrderParams paymentOrderParams = new PaymentOrderParams();
        paymentOrderParams.setPan(paymentParams.getPan());
        paymentOrderParams.setCardholderName(paymentParams.getCardHolderName());
        paymentOrderParams.setCvc(paymentParams.getCvc());
        paymentOrderParams.setLanguage(language);
        paymentOrderParams.setMonth(paymentParams.getMonth());
        paymentOrderParams.setYear(paymentParams.getYear());
        paymentOrderParams.setOrderId(externalOrderId);
        if ( null != integrationPaymentRequest.getClient() ) {
            paymentOrderParams.setEmail(integrationPaymentRequest.getClient().getEmail());
            paymentOrderParams.setIp(integrationPaymentRequest.getClient().getIp());
        }
        try {
            PaymentOrderResult paymentOrderResult = getMerchantService().paymentOrder(paymentOrderParams);
            integrationPaymentResponse.setMessage(paymentOrderResult.getErrorMessage());
            if ( 0 != paymentOrderResult.getErrorCode() ) {
                logger.error("Error payment order: " + integrationPaymentRequest.getOrderId() + " because: " + paymentOrderResult.getErrorMessage());
                integrationPaymentResponse.setSuccess(false);
                return integrationPaymentResponse;
            }
            integrationPaymentResponse.setSuccess(true);
            Model model = integrationPaymentRequest.getModel();
            if ( StringUtils.isNotBlank(paymentOrderResult.getAcsUrl()) && StringUtils.isNotBlank(paymentOrderResult.getPaReq()) ) {
                model.addAttribute("acsUrl", paymentOrderResult.getAcsUrl());
                model.addAttribute("mdOrder", externalOrderId);
                model.addAttribute("paReq", paymentOrderResult.getPaReq());
                model.addAttribute("termUrl", paymentOrderResult.getRedirect());
                model.addAttribute("language", language);
                integrationPaymentResponse.setRedirectUrlOrPagePath(ACS_REDIRECT_PAGE);
                integrationPaymentResponse.setIntegrationOrderStatus(RbsOrderStatus.REDIRECTED_TO_ACS);
            }
            else {
                IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(new IntegrationOrderStatusRequest(integrationPaymentRequest.getIntegrationSupport(), externalOrderId));
                integrationPaymentResponse.setIntegrationOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus());
                integrationOrderStatusResponse.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
                integrationPaymentResponse.setRedirectUrlOrPagePath("redirect:" + paymentOrderResult.getRedirect());
            }
        } catch (Exception e) {
            throw new IntegrationException("Error integration payment order by orderId: " + integrationPaymentRequest.getOrderId(),e);
        }

        return integrationPaymentResponse;
    }

    @Override
    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationOrderStatusResponse integrationOrderStatusResponse = new IntegrationOrderStatusResponse();
        integrationOrderStatusResponse.setExternalId(integrationOrderStatusRequest.getExternalId());

        GetOrderStatusExtendedRequest getOrderStatusExtendedRequest = new GetOrderStatusExtendedRequest();
        getOrderStatusExtendedRequest.setOrderId(integrationOrderStatusRequest.getExternalId());
        try {
            GetOrderStatusExtendedResponse getOrderStatusExtendedResponse = getMerchantService().getOrderStatusExtended(getOrderStatusExtendedRequest);
            integrationOrderStatusResponse.setMessage(getOrderStatusExtendedResponse.getErrorMessage());
            if ( !"0".equals(getOrderStatusExtendedResponse.getErrorCode()) ) {
                logger.error("Error get order status: {}, because: {}, code: {}",
                        new Object[]{integrationOrderStatusRequest.getExternalId(),
                                getOrderStatusExtendedResponse.getErrorMessage(),
                                getOrderStatusExtendedResponse.getErrorCode()});
                integrationOrderStatusResponse.setOrderStatus(null);
                integrationOrderStatusResponse.setSuccess(false);
            }
            integrationOrderStatusResponse.setSuccess(true);
            RbsOrderStatus rbsOrderStatus = RbsOrderStatus.valueOf(getOrderStatusExtendedResponse.getOrderStatus());
            integrationOrderStatusResponse.setIntegrationOrderStatus(rbsOrderStatus);
            return integrationOrderStatusResponse;
        } catch (Exception e) {
            throw new IntegrationException("Error integration get order status by orderId:" + integrationOrderStatusRequest.getExternalId(),e);
        }
    }

    @Override
    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        ReversalOrderParams reversalOrderParams = new ReversalOrderParams();
        reversalOrderParams.setOrderId(integrationReverseRequest.getExternalId());
        reversalOrderParams.setLanguage(language);
        try {
            IntegrationReverseResponse integrationReverseResponse = new IntegrationReverseResponse();
            integrationReverseResponse.setOrderId(integrationReverseRequest.getOrderId());
            integrationReverseResponse.setExternalId(integrationReverseRequest.getExternalId());
            OrderResult orderResult = getMerchantService().reverseOrder(reversalOrderParams);
            integrationReverseResponse.setMessage(orderResult.getErrorMessage());
            if ( 0 == orderResult.getErrorCode() ) {
                integrationReverseResponse.setSuccess(true);
            }
            else {
                logger.error("Error integration reverse by orderId: {}, cause: {}", integrationReverseRequest.getExternalId(), orderResult.getErrorMessage());
            }
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
