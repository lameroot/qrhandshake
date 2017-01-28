package ru.qrhandshake.qrpos.integration.rbs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentParams;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.*;
import ru.rbs.http.api.client.ApiClient;
import ru.rbs.http.api.domain.*;
import ru.rbs.http.api.domain.BindingInfo;
import ru.rbs.http.api.methods.*;

import javax.annotation.Resource;
import java.util.Map;

class RbsSyncHttpIntegrationFacade implements RbsSyncIntegrationFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String language = "ru";
    private final static String currency = "643";

    @Resource
    private ApiClient restRbsHttpClient;

    @Value("${integration.rbs.paymentType:PURCHASE}")
    private String sPaymentType;

    private <T> T newInstance(Class<T> clazz, IntegrationRequest integrationRequest) throws IntegrationException {
        try {
            Endpoint endpoint = integrationRequest.getEndpoint();
            UserPasswordEndpoint userPasswordEndpoint = (UserPasswordEndpoint) endpoint;
            return clazz.getConstructor(String.class, String.class, String.class).newInstance(userPasswordEndpoint.getUsername(), userPasswordEndpoint.getPassword(), endpoint.getEndpointCatalog().getAddress());
        } catch (Exception e) {
            throw new IntegrationException("Can't create request for class: " + clazz,e);
        }
    }

    public IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentBindingRequest.getOrderId());
        integrationPaymentResponse.setPaymentType(PaymentType.PURCHASE);

        RegisterOrderParams rbsParams = newInstance(RegisterOrderParams.class, integrationPaymentBindingRequest);
        rbsParams.setCurrency(currency);
        rbsParams.setLanguage(language);
        rbsParams.setDescription(integrationPaymentBindingRequest.getDescription());
        rbsParams.setAmount(integrationPaymentBindingRequest.getAmount());
        rbsParams.setReturnUrl(integrationPaymentBindingRequest.getReturnUrl());
        rbsParams.setOrderNumber(integrationPaymentBindingRequest.getOrderId());
        for (Map.Entry<String, String> entry : integrationPaymentBindingRequest.getParams().entrySet()) {
            rbsParams.getJsonParams().add(entry.getKey(),entry.getValue());
        }
        if (null != integrationPaymentBindingRequest.getClient()) {
            Client client = integrationPaymentBindingRequest.getClient();
            rbsParams.setClientId(client.getClientId());
            if (null != integrationPaymentBindingRequest.getIp()) {
                rbsParams.getJsonParams().add(Client.IP_PARAM, integrationPaymentBindingRequest.getIp());
            }
            if (null != client.getEmail()) {
                rbsParams.getJsonParams().add(Client.EMAIL_PARAM, client.getEmail());
            }
            if (null != client.getPhone()) {
                rbsParams.getJsonParams().add(Client.PHONE_PARAM, client.getPhone());
            }
        }

        String externalOrderId = null;
        try {
            RegisterOrderProcess registerOrderProcess = restRbsHttpClient.execute(RegisterOrderProcess.Request.newInstance(rbsParams));
            integrationPaymentResponse.setMessage(registerOrderProcess.getErrorMessage());
            if ( 0 != registerOrderProcess.getErrorCode() ) {
                logger.error("Error register order: " + integrationPaymentBindingRequest.getOrderId() + " because : " + registerOrderProcess.getErrorMessage());
                integrationPaymentResponse.setSuccess(false);
                return integrationPaymentResponse;
            }
            integrationPaymentResponse.setSuccess(true);
            externalOrderId = registerOrderProcess.getOrderId();
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

        PaymentOrderBindingParams paymentOrderBindingParams = newInstance(PaymentOrderBindingParams.class, integrationPaymentBindingRequest);
        paymentOrderBindingParams.setBindingId(integrationPaymentBindingRequest.getExternalBindingId());
        paymentOrderBindingParams.setCvc(paymentParams.getConfirmValue());
        paymentOrderBindingParams.setEmail(integrationPaymentBindingRequest.getClient().getEmail());
        paymentOrderBindingParams.setIp(integrationPaymentBindingRequest.getIp());
        paymentOrderBindingParams.setLanguage(language);
        paymentOrderBindingParams.setMdOrder(externalOrderId);

        try {
            PaymentOrderBindingProcess paymentOrderBindingProcess = restRbsHttpClient.execute(PaymentOrderBindingProcess.Request.newInstance(paymentOrderBindingParams));
            handlePaymentResult(integrationPaymentResponse, integrationPaymentBindingRequest, externalOrderId, paymentOrderBindingProcess);
        } catch (Exception e) {
            throw new IntegrationException("Error integration BINDING payment order by orderId: " + integrationPaymentBindingRequest.getOrderId(),e);
        }
        return integrationPaymentResponse;
    }

    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException {
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentRequest.getOrderId());
        PaymentType paymentType = null != integrationPaymentRequest.getPaymentType() ? integrationPaymentRequest.getPaymentType() : PaymentType.valueOf(sPaymentType);
        integrationPaymentResponse.setPaymentType(paymentType);

        String externalOrderId = integrationPaymentRequest.getExternalId();
        if ( StringUtils.isBlank(externalOrderId) ) {
            RegisterOrderParams rbsParams = newInstance(RegisterOrderParams.class, integrationPaymentRequest);
            rbsParams.setCurrency(currency);
            rbsParams.setLanguage(language);
            rbsParams.setDescription(integrationPaymentRequest.getDescription());
            rbsParams.setAmount(integrationPaymentRequest.getAmount());
            rbsParams.setReturnUrl(integrationPaymentRequest.getReturnUrl());
            rbsParams.setOrderNumber(integrationPaymentRequest.getOrderId());
            for (Map.Entry<String, String> entry : integrationPaymentRequest.getParams().entrySet()) {
                rbsParams.getJsonParams().add(entry.getKey(),entry.getValue());
            }
            if (null != integrationPaymentRequest.getClient()) {
                Client client = integrationPaymentRequest.getClient();
                rbsParams.setClientId(client.getClientId());
                if (null != integrationPaymentRequest.getIp()) {
                    rbsParams.getJsonParams().add(Client.IP_PARAM, integrationPaymentRequest.getIp());
                }
                if (null != client.getEmail()) {
                    rbsParams.getJsonParams().add(Client.EMAIL_PARAM, client.getEmail());
                }
                if (null != client.getPhone()) {
                    rbsParams.getJsonParams().add(Client.PHONE_PARAM, client.getPhone());
                }
            }

            try {
                RegisterOrderProcess registerOrderProcess = null;
                if (PaymentType.PURCHASE == paymentType) {
                    registerOrderProcess = restRbsHttpClient.execute(RegisterOrderProcess.Request.newInstance(rbsParams));
                } else if (PaymentType.DEPOSIT == paymentType) {
                    registerOrderProcess = restRbsHttpClient.execute(RegisterPreAuthOrderProcess.Request.newInstance(rbsParams));
                } else {
                    integrationPaymentResponse.setSuccess(false);
                    integrationPaymentResponse.setMessage("Unknown paymentTye: " + paymentType);
                    return integrationPaymentResponse;
                }
                integrationPaymentResponse.setMessage(registerOrderProcess.getErrorMessage());
                if (0 != registerOrderProcess.getErrorCode()) {
                    logger.error("Error register order: " + integrationPaymentRequest.getOrderId() + " because : " + registerOrderProcess.getErrorMessage());
                    integrationPaymentResponse.setSuccess(false);
                    return integrationPaymentResponse;
                }
                integrationPaymentResponse.setSuccess(true);
                externalOrderId = registerOrderProcess.getOrderId();
                integrationPaymentResponse.setExternalId(externalOrderId);

            } catch (Exception e) {
                throw new IntegrationException("Error integration payment order by orderId:" + integrationPaymentRequest.getOrderId(), e);
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

        PaymentOrderParams paymentOrderParams = newInstance(PaymentOrderParams.class, integrationPaymentRequest);
        paymentOrderParams.setPan(paymentParams.getPan());
        paymentOrderParams.setCardHolder(paymentParams.getCardHolderName());
        paymentOrderParams.setCvc(paymentParams.getCvc());
        paymentOrderParams.setLanguage(language);
        paymentOrderParams.setExpiry(paymentParams.getYear() + paymentParams.getMonth());
        paymentOrderParams.setMdOrder(externalOrderId);
        paymentOrderParams.setIp(integrationPaymentRequest.getIp());
        if ( null != integrationPaymentRequest.getClient() ) {
            paymentOrderParams.setEmail(integrationPaymentRequest.getClient().getEmail());
        }
        try {
            PaymentOrderProcess paymentOrderProcess = restRbsHttpClient.execute(PaymentOrderProcess.Request.newInstance(paymentOrderParams));
            handlePaymentResult(integrationPaymentResponse,integrationPaymentRequest,externalOrderId,paymentOrderProcess);
        } catch (Exception e) {
            throw new IntegrationException("Error integration payment order by orderId: " + integrationPaymentRequest.getOrderId(),e);
        }

        return integrationPaymentResponse;
    }

    private void handlePaymentResult(IntegrationPaymentResponse integrationPaymentResponse, IntegrationPaymentRequest integrationPaymentRequest, String externalOrderId, PaymentOrderProcess paymentOrderResult) throws IntegrationException {
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
        ru.rbs.http.api.domain.ReturnUrlObject returnUrlObjectHttp = paymentOrderResult.getReturnUrlObject();
        ReturnUrlObject returnUrlObject = new ReturnUrlObject();
        integrationPaymentResponse.setReturnUrlObject(returnUrlObject);
        if ( StringUtils.isNotBlank(returnUrlObjectHttp.getUrl()) && StringUtils.isNotBlank(returnUrlObjectHttp.getParams().get("paReq")) ) {
            returnUrlObject.setUrl(returnUrlObjectHttp.getUrl());
            returnUrlObject.getParams().put("MD", externalOrderId);
            returnUrlObject.getParams().put("PaReq", returnUrlObjectHttp.getParams().get("paReq"));
            returnUrlObject.getParams().put("TermUrl", returnUrlObjectHttp.getParams().get("termUrl"));
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
            returnUrlObject.setUrl(returnUrlObjectHttp.getUrl());
            returnUrlObject.setAction("redirect");
        }
    }

    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        IntegrationOrderStatusResponse integrationOrderStatusResponse = new IntegrationOrderStatusResponse();
        integrationOrderStatusResponse.setExternalId(integrationOrderStatusRequest.getExternalId());

        OrderStatusExtendedParams getOrderStatusExtendedRequest = newInstance(OrderStatusExtendedParams.class, integrationOrderStatusRequest);
        getOrderStatusExtendedRequest.setOrderId(integrationOrderStatusRequest.getExternalId());
        try {
            OrderStatusExtendedProcess orderStatusExtendedProcess = restRbsHttpClient.execute(OrderStatusExtendedProcess.Request.newInstance(getOrderStatusExtendedRequest));
            integrationOrderStatusResponse.setMessage(orderStatusExtendedProcess.getErrorMessage());
            if ( 0 != orderStatusExtendedProcess.getErrorCode() ) {
                logger.error("Error get order status: {}, because: {}, code: {}",
                        new Object[]{integrationOrderStatusRequest.getExternalId(),
                                orderStatusExtendedProcess.getErrorMessage(),
                                orderStatusExtendedProcess.getErrorCode()});
                integrationOrderStatusResponse.setOrderStatus(null);
                integrationOrderStatusResponse.setSuccess(false);
            }
            BindingInfo bindingInfoHttp = orderStatusExtendedProcess.getBindingInfo();
            if ( null != bindingInfoHttp) {
                integrationOrderStatusResponse.setBindingInfo(new ru.qrhandshake.qrpos.domain.BindingInfo(bindingInfoHttp.getClientId(), bindingInfoHttp.getBindingId()));
            }
            integrationOrderStatusResponse.setSuccess(true);
            RbsOrderStatus rbsOrderStatus = RbsOrderStatus.valueOf(orderStatusExtendedProcess.getOrderStatus());
            integrationOrderStatusResponse.setIntegrationOrderStatus(rbsOrderStatus);
            return integrationOrderStatusResponse;
        } catch (Exception e) {
            throw new IntegrationException("Error integration get order status by orderId:" + integrationOrderStatusRequest.getExternalId(),e);
        }
    }

    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        ReverseParams reverseParams = newInstance(ReverseParams.class, integrationReverseRequest);
        reverseParams.setOrderId(integrationReverseRequest.getExternalId());
        reverseParams.setLanguage(language);
        try {
            IntegrationReverseResponse integrationReverseResponse = new IntegrationReverseResponse();
            integrationReverseResponse.setOrderId(integrationReverseRequest.getOrderId());
            integrationReverseResponse.setExternalId(integrationReverseRequest.getExternalId());
            ReverseProcess reverseProcess = restRbsHttpClient.execute(ReverseProcess.Request.newInstance(reverseParams));
            integrationReverseResponse.setMessage(reverseProcess.getErrorMessage());
            if ( 0 == reverseProcess.getErrorCode() ) {
                integrationReverseResponse.setSuccess(true);
            }
            else {
                logger.error("Error integration reverse by orderId: {}, cause: {}", integrationReverseRequest.getExternalId(), reverseProcess.getErrorMessage());
            }
            return integrationReverseResponse;
        } catch (Exception e) {
            throw new IntegrationException("Error reverse orderId: " + integrationReverseRequest.getExternalId() + " by system: " + integrationReverseRequest.getEndpoint(),e);
        }
    }

    public IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException {
        IntegrationCompletionResponse integrationCompletionResponse = new IntegrationCompletionResponse();
        integrationCompletionResponse.setOrderId(integrationCompletionRequest.getOrderId());
        integrationCompletionResponse.setExternalOrderId(integrationCompletionRequest.getExternalOrderId());

        try {
            DepositParams depositParams = newInstance(DepositParams.class, integrationCompletionRequest);
            depositParams.setOrderId(integrationCompletionRequest.getExternalOrderId());
            depositParams.setAmount(integrationCompletionRequest.getAmount());
            DepositProcess depositProcess = restRbsHttpClient.execute(DepositProcess.Request.newInstance(depositParams));
            if ( 0 != depositProcess.getErrorCode() ) {
                integrationCompletionResponse.setSuccess(false);
                integrationCompletionResponse.setMessage(depositProcess.getErrorMessage());
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
}
