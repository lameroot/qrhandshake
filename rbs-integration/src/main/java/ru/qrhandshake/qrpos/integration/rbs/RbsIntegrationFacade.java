package ru.qrhandshake.qrpos.integration.rbs;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import ru.bpc.phoenix.proxy.api.MerchantServiceProvider;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.bpc.phoenix.web.api.merchant.soap.MerchantService;
import ru.paymentgate.engine.webservices.merchant.*;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentParams;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.util.Util;
import ru.rbs.commons.cluster.retry.RetriableExecutor;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class RbsIntegrationFacade implements IntegrationFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String language = "ru";
    private final static String currency = "643";

    @Resource
    private Environment environment;
    private MerchantServiceProvider merchantServiceProvider;
    private IntegrationSupport integrationSupport;

    private Map<Endpoint, MerchantService> merchantServiceMap = new HashMap<>();

    @Value("${integration.rbs.paymentType:PURCHASE}")
    private String sPaymentType;
    @Value("${integration.rbs.paymentBinding.maxAmountForAsync:10000}")
    private Long paymentBindingMaxAmountForAsync;

    public RbsIntegrationFacade(@NotNull IntegrationSupport integrationSupport) {
        Assert.notNull(integrationSupport,"integrationSupport must not be null");
        this.integrationSupport = integrationSupport;
    }

    @NotNull
    MerchantService getMerchantService(Endpoint endpoint) {
        Assert.notNull(endpoint,"endpoint must not be null");
        MerchantService merchantService = null;
        UserPasswordEndpoint userPasswordEndpoint = (UserPasswordEndpoint)endpoint;
        if ( null == (merchantService = merchantServiceMap.get(userPasswordEndpoint)) ) {
            if ( null == merchantServiceProvider ) {
                this.merchantServiceProvider = new MerchantServiceProvider();
                this.merchantServiceProvider.setDebugSoap(true);
                this.merchantServiceProvider.setWsdlLocation(endpoint.getEndpointCatalog().getAddress());
            }
            merchantService = merchantServiceProvider.getMerchantService(new NamePasswordToken(userPasswordEndpoint.getUsername(),userPasswordEndpoint.getPassword()));
            merchantServiceMap.put(userPasswordEndpoint,merchantService);
        }
        if ( null == merchantService ) throw new IllegalArgumentException("Can't create merchantService");
        return merchantService;
    }

    @Resource
    private RetriableExecutor retriableExecutor;
    @Resource
    private PaymentBindingRetryTask paymentBindingRetryTask;

    @Override
    public IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentBindingRequest.getOrderId());
        integrationPaymentResponse.setPaymentType(PaymentType.PURCHASE);

        try {
            if (integrationPaymentBindingRequest.isAsync() && integrationPaymentBindingRequest.getClient().isAccountNonLocked() && integrationPaymentBindingRequest.getAmount() <= paymentBindingMaxAmountForAsync) {
                integrationPaymentResponse.setSuccess(true);
                integrationPaymentResponse.setPaymentSecureType(PaymentSecureType.SSL);
                integrationPaymentResponse.setMessage("Order is pending");
                integrationPaymentResponse.setOrderStatus(OrderStatus.PENDING);
                integrationPaymentResponse.setIntegrationOrderStatus(RbsOrderStatus.CREATED);
                ReturnUrlObject returnUrlObject = new ReturnUrlObject();
                returnUrlObject.setUrl("async url");
                returnUrlObject.setAction("redirect");
                integrationPaymentResponse.setReturnUrlObject(returnUrlObject);
                integrationPaymentBindingRequest.setAsync(false);

                retriableExecutor.execute(integrationPaymentBindingRequest, paymentBindingRetryTask);

                //rabbitTemplate.send("RBS", "paymentBinding", MessageBuilder.withBody(objectMapper.writeValueAsBytes(integrationPaymentBindingRequest)).build());
                logger.debug("Async paymentBinding for {}, sent by RBS.paymentBinding routingKey");
                return integrationPaymentResponse;
            }
        } catch (Exception e) {
            logger.error("Error paymentBinding for {} use rabbitMQ, try to send as sync",integrationPaymentBindingRequest,e);
        }
        OrderParams rbsParams = new OrderParams();
        rbsParams.setCurrency(currency);
        rbsParams.setLanguage(language);
        rbsParams.setDescription(integrationPaymentBindingRequest.getDescription());
        rbsParams.setAmount(integrationPaymentBindingRequest.getAmount());
        rbsParams.setReturnUrl(integrationPaymentBindingRequest.getReturnUrl());
        rbsParams.setMerchantOrderNumber(integrationPaymentBindingRequest.getOrderId());
        for (Map.Entry<String, String> entry : integrationPaymentBindingRequest.getParams().entrySet()) {
            ServiceParam serviceParam = new ServiceParam();
            serviceParam.setName(entry.getKey());
            serviceParam.setValue(entry.getValue());
            rbsParams.getParams().add(serviceParam);
        }
        if ( null != integrationPaymentBindingRequest.getClient() ) {
            Client client = integrationPaymentBindingRequest.getClient();
            rbsParams.setClientId(client.getClientId());
            if ( null != integrationPaymentBindingRequest.getIp() ) rbsParams.getParams().add(Util.createServiceParam(Client.IP_PARAM,integrationPaymentBindingRequest.getIp()));
            if ( null != client.getEmail() ) rbsParams.getParams().add(Util.createServiceParam(Client.EMAIL_PARAM,client.getEmail()));
            if ( null != client.getPhone() ) rbsParams.getParams().add(Util.createServiceParam(Client.PHONE_PARAM, client.getPhone()));
        }

        String externalOrderId = null;
        try {
            RegisterOrderResponse registerOrderResponse = getMerchantService(integrationPaymentBindingRequest.getEndpoint()).registerOrder(rbsParams);
            integrationPaymentResponse.setMessage(registerOrderResponse.getErrorMessage());
            if ( 0 != registerOrderResponse.getErrorCode() ) {
                logger.error("Error register order: " + integrationPaymentBindingRequest.getOrderId() + " because : " + registerOrderResponse.getErrorMessage());
                integrationPaymentResponse.setSuccess(false);
                return integrationPaymentResponse;
            }
            integrationPaymentResponse.setSuccess(true);
            externalOrderId = registerOrderResponse.getOrderId();
            integrationPaymentResponse.setExternalId(externalOrderId);
        } catch (Exception e) {
            throw new IntegrationException("Error integration register order by orderId:" + integrationPaymentBindingRequest.getOrderId(),e);
        }

        BindingPaymentParams paymentParams = (BindingPaymentParams)integrationPaymentBindingRequest.getPaymentParams();
        if ( null == paymentParams ) {
            integrationPaymentResponse.setSuccess(false);
            integrationPaymentResponse.setMessage("Invalid payment params");
            return integrationPaymentResponse;
        }

        PaymentOrderBindingParams paymentOrderBindingParams = new PaymentOrderBindingParams();
        paymentOrderBindingParams.setBindingId(integrationPaymentBindingRequest.getExternalBindingId());
        paymentOrderBindingParams.setCvc(paymentParams.getConfirmValue());
        paymentOrderBindingParams.setEmail(integrationPaymentBindingRequest.getClient().getEmail());
        paymentOrderBindingParams.setIp(integrationPaymentBindingRequest.getIp());
        paymentOrderBindingParams.setLanguage(language);
        paymentOrderBindingParams.setMdOrder(externalOrderId);

        try {
            PaymentOrderResult paymentOrderResult = getMerchantService(integrationPaymentBindingRequest.getEndpoint()).paymentOrderBinding(paymentOrderBindingParams);
            handlePaymentResult(integrationPaymentResponse, integrationPaymentBindingRequest, externalOrderId, paymentOrderResult);
        } catch (Exception e) {
            throw new IntegrationException("Error integration BINDING payment order by orderId: " + integrationPaymentBindingRequest.getOrderId(),e);
        }
        return integrationPaymentResponse;
    }

    @Override
    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException {
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentRequest.getOrderId());
        PaymentType paymentType = null != integrationPaymentRequest.getPaymentType() ? integrationPaymentRequest.getPaymentType() : PaymentType.valueOf(sPaymentType);
        integrationPaymentResponse.setPaymentType(paymentType);

        String externalOrderId = integrationPaymentRequest.getExternalId();
        if ( StringUtils.isBlank(externalOrderId) ) {
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
            if (null != integrationPaymentRequest.getClient()) {
                Client client = integrationPaymentRequest.getClient();
                rbsParams.setClientId(client.getClientId());
                if (null != integrationPaymentRequest.getIp())
                    rbsParams.getParams().add(Util.createServiceParam(Client.IP_PARAM, integrationPaymentRequest.getIp()));
                if (null != client.getEmail())
                    rbsParams.getParams().add(Util.createServiceParam(Client.EMAIL_PARAM, client.getEmail()));
                if (null != client.getPhone())
                    rbsParams.getParams().add(Util.createServiceParam(Client.PHONE_PARAM, client.getPhone()));
            }


            try {
                RegisterOrderResponse registerOrderResponse = null;
                if (PaymentType.PURCHASE == paymentType) {
                    registerOrderResponse = getMerchantService(integrationPaymentRequest.getEndpoint()).registerOrder(rbsParams);
                } else if (PaymentType.DEPOSIT == paymentType) {
                    registerOrderResponse = getMerchantService(integrationPaymentRequest.getEndpoint()).registerOrderPreAuth(rbsParams);
                } else {
                    integrationPaymentResponse.setSuccess(false);
                    integrationPaymentResponse.setMessage("Unknown paymentTye: " + paymentType);
                    return integrationPaymentResponse;
                }

                integrationPaymentResponse.setMessage(registerOrderResponse.getErrorMessage());
                if (0 != registerOrderResponse.getErrorCode()) {
                    logger.error("Error register order: " + integrationPaymentRequest.getOrderId() + " because : " + registerOrderResponse.getErrorMessage());
                    integrationPaymentResponse.setSuccess(false);
                    return integrationPaymentResponse;
                }
                integrationPaymentResponse.setSuccess(true);
                externalOrderId = registerOrderResponse.getOrderId();
                integrationPaymentResponse.setExternalId(externalOrderId);
            } catch (Exception e) {
                throw new IntegrationException("Error integration register order by orderId:" + integrationPaymentRequest.getOrderId(), e);
            }
        }
        else {
            integrationPaymentResponse.setExternalId(externalOrderId);
            logger.debug("Order with orderId: {} was already registered",integrationPaymentRequest.getOrderId());
            IntegrationOrderStatusRequest integrationOrderStatusRequest = new IntegrationOrderStatusRequest(integrationPaymentRequest.getEndpoint(),externalOrderId);
            IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(integrationOrderStatusRequest);

            if ( !(integrationOrderStatusResponse.getIntegrationOrderStatus().getStatus().equals(RbsOrderStatus.CREATED.getStatus())
                    || integrationOrderStatusResponse.getIntegrationOrderStatus().getStatus().equals(RbsOrderStatus.REDIRECTED_TO_ACS.getStatus()))) {
                integrationPaymentResponse.setSuccess(false);
                integrationPaymentResponse.setMessage("Order with orderId: " + integrationPaymentRequest.getOrderId() + " has already paid");
                integrationPaymentResponse.setOrderStatus(toOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus()));
                integrationPaymentResponse.setIntegrationOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus());
                return integrationPaymentResponse;
            }
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
        paymentOrderParams.setIp(integrationPaymentRequest.getIp());
        if ( null != integrationPaymentRequest.getClient() ) {
            paymentOrderParams.setEmail(integrationPaymentRequest.getClient().getEmail());
        }
        try {
            PaymentOrderResult paymentOrderResult = getMerchantService(integrationPaymentRequest.getEndpoint()).paymentOrder(paymentOrderParams);
            handlePaymentResult(integrationPaymentResponse,integrationPaymentRequest,externalOrderId,paymentOrderResult);
        } catch (Exception e) {
            throw new IntegrationException("Error integration payment order by orderId: " + integrationPaymentRequest.getOrderId(),e);
        }

        return integrationPaymentResponse;
    }

    private void handlePaymentResult(IntegrationPaymentResponse integrationPaymentResponse, IntegrationPaymentRequest integrationPaymentRequest, String externalOrderId, PaymentOrderResult paymentOrderResult) throws IntegrationException {
        integrationPaymentResponse.setMessage(paymentOrderResult.getErrorMessage());
        if ( 0 != paymentOrderResult.getErrorCode() ) {
            logger.error("Error payment order: " + integrationPaymentRequest.getOrderId() + " because: " + paymentOrderResult.getErrorMessage());
            integrationPaymentResponse.setSuccess(false);
            IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(new IntegrationOrderStatusRequest(integrationPaymentRequest.getEndpoint(), externalOrderId));
            integrationPaymentResponse.setIntegrationOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus());
            integrationPaymentResponse.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
            return;
        }
        integrationPaymentResponse.setSuccess(true);
        ReturnUrlObject returnUrlObject = new ReturnUrlObject();
        integrationPaymentResponse.setReturnUrlObject(returnUrlObject);
        if ( StringUtils.isNotBlank(paymentOrderResult.getAcsUrl()) && StringUtils.isNotBlank(paymentOrderResult.getPaReq()) ) {
            returnUrlObject.setUrl(paymentOrderResult.getAcsUrl());
            returnUrlObject.getParams().put("MD", externalOrderId);
            returnUrlObject.getParams().put("PaReq", paymentOrderResult.getPaReq());
            returnUrlObject.getParams().put("TermUrl", paymentOrderResult.getRedirect());
            returnUrlObject.getParams().put("language", language);
            returnUrlObject.setAction("post");

            integrationPaymentResponse.setIntegrationOrderStatus(RbsOrderStatus.REDIRECTED_TO_ACS);
            integrationPaymentResponse.setPaymentSecureType(PaymentSecureType.TDS);
        }
        else {
            IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(new IntegrationOrderStatusRequest(integrationPaymentRequest.getEndpoint(), externalOrderId));
            integrationPaymentResponse.setIntegrationOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus());
            integrationPaymentResponse.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
            integrationPaymentResponse.setPaymentSecureType(PaymentSecureType.SSL);
            returnUrlObject.setUrl(paymentOrderResult.getRedirect());
            returnUrlObject.setAction("redirect");
        }
    }

    @Override
    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationOrderStatusResponse integrationOrderStatusResponse = new IntegrationOrderStatusResponse();
        integrationOrderStatusResponse.setExternalId(integrationOrderStatusRequest.getExternalId());

        GetOrderStatusExtendedRequest getOrderStatusExtendedRequest = new GetOrderStatusExtendedRequest();
        getOrderStatusExtendedRequest.setOrderId(integrationOrderStatusRequest.getExternalId());
        try {
            GetOrderStatusExtendedResponse getOrderStatusExtendedResponse = getMerchantService(integrationOrderStatusRequest.getEndpoint()).getOrderStatusExtended(getOrderStatusExtendedRequest);
            integrationOrderStatusResponse.setMessage(getOrderStatusExtendedResponse.getErrorMessage());
            if ( !"0".equals(getOrderStatusExtendedResponse.getErrorCode()) ) {
                logger.error("Error get order status: {}, because: {}, code: {}",
                        new Object[]{integrationOrderStatusRequest.getExternalId(),
                                getOrderStatusExtendedResponse.getErrorMessage(),
                                getOrderStatusExtendedResponse.getErrorCode()});
                integrationOrderStatusResponse.setOrderStatus(null);
                integrationOrderStatusResponse.setSuccess(false);
            }
            CardBindingInfo cardBindingInfo = getOrderStatusExtendedResponse.getBindingInfo();
            if ( null != cardBindingInfo ) {
                integrationOrderStatusResponse.setBindingInfo(new BindingInfo(cardBindingInfo.getClientId(), cardBindingInfo.getBindingId()));
            }
            integrationOrderStatusResponse.setSuccess(true);
            RbsOrderStatus rbsOrderStatus = RbsOrderStatus.valueOf(getOrderStatusExtendedResponse.getOrderStatus());
            integrationOrderStatusResponse.setIntegrationOrderStatus(rbsOrderStatus);
            integrationOrderStatusResponse.setOrderStatus(toOrderStatus(rbsOrderStatus));
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
            OrderResult orderResult = getMerchantService(integrationReverseRequest.getEndpoint()).reverseOrder(reversalOrderParams);
            integrationReverseResponse.setMessage(orderResult.getErrorMessage());
            if ( 0 == orderResult.getErrorCode() ) {
                integrationReverseResponse.setSuccess(true);
            }
            else {
                logger.error("Error integration reverse by orderId: {}, cause: {}", integrationReverseRequest.getExternalId(), orderResult.getErrorMessage());
            }
            return integrationReverseResponse;
        } catch (Exception e) {
            throw new IntegrationException("Error reverse orderId: " + integrationReverseRequest.getExternalId() + " by system: " + integrationReverseRequest.getEndpoint(),e);
        }
    }

    @Override
    public IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException {
        IntegrationCompletionResponse integrationCompletionResponse = new IntegrationCompletionResponse();
        integrationCompletionResponse.setOrderId(integrationCompletionRequest.getOrderId());
        integrationCompletionResponse.setExternalOrderId(integrationCompletionRequest.getExternalOrderId());
        try {
            DepositOrderParams depositOrderParams = new DepositOrderParams();
            depositOrderParams.setOrderId(integrationCompletionRequest.getExternalOrderId());
            depositOrderParams.setDepositAmount(integrationCompletionRequest.getAmount());
            OrderResult orderResult = getMerchantService(integrationCompletionRequest.getEndpoint()).depositOrder(depositOrderParams);
            if ( 0 != orderResult.getErrorCode() ) {
                integrationCompletionResponse.setSuccess(false);
                integrationCompletionResponse.setMessage(orderResult.getErrorMessage());
            }
            else {
                integrationCompletionResponse.setSuccess(true);
                integrationCompletionResponse.setMessage("Completion by externalOrderId: " + integrationCompletionRequest.getExternalOrderId() + " successfully");
            }
        } catch (Exception e) {
            logger.error("Error rbs completion by externalOrderId: " + integrationCompletionRequest.getExternalOrderId(),e);
            integrationCompletionResponse.setSuccess(false);
            integrationCompletionResponse.setMessage("Error completion by externalOrderId: " + integrationCompletionRequest.getExternalOrderId() + ", cause: " + e.getMessage());
        }
        return integrationCompletionResponse;
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
                case DECLINED: return OrderStatus.DECLINED;
                case REDIRECTED_TO_ACS: return OrderStatus.REDIRECTED_TO_EXTERNAL;
            }
            return OrderStatus.REGISTERED;
        }
        //throw new IllegalArgumentException("Unknown integration order status: " + integrationOrderStatus);
        return null;
    }

}
