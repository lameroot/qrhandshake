package ru.qrhandshake.qrpos.integration.rbs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import ru.bpc.phoenix.proxy.api.MerchantServiceProvider;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.bpc.phoenix.proxy.api.P2PServiceProvider;
import ru.bpc.phoenix.web.api.merchant.soap.MerchantService;
import ru.bpc.phoenix.web.api.merchant.soap.p2p.P2PWSController;
import ru.paymentgate.engine.webservices.merchant.*;
import ru.paymentgate.engine.webservices.p2p.*;
import ru.qrhandshake.qrpos.api.BindingPaymentParams;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ClientDto;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lameroot on 19.05.16.
 */
public class RbsIntegrationFacade implements IntegrationFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String language = "ru";
    private final static String currency = "643";
    private final static String ACS_REDIRECT_PAGE = "rbs/acs_redirect";

    @Resource
    private Environment environment;
    private MerchantServiceProvider merchantServiceProvider;
    private P2PServiceProvider p2PServiceProvider;
    private final NamePasswordToken namePasswordToken;
    private final NamePasswordToken p2pNamePasswordToken;
    private MerchantService merchantService;
    private P2PWSController p2PWSController;
    private IntegrationSupport integrationSupport;

    @Value("${rbs.paymentType:PURCHASE}")
    private String sPaymentType;

    public RbsIntegrationFacade(NamePasswordToken namePasswordToken, String wsdlLocation, IntegrationSupport integrationSupport) {
        this(namePasswordToken, wsdlLocation, null, null, integrationSupport);
    }

    public RbsIntegrationFacade(NamePasswordToken namePasswordToken, String wsdlLocation, NamePasswordToken p2pNamePasswordToken, String p2pWsdlLocation, IntegrationSupport integrationSupport) {
        this.namePasswordToken = namePasswordToken;
        this.p2pNamePasswordToken = p2pNamePasswordToken;

        this.merchantServiceProvider = new MerchantServiceProvider();
        this.merchantServiceProvider.setDebugSoap(true);
        this.merchantServiceProvider.setWsdlLocation(wsdlLocation);

        if ( null != p2pWsdlLocation ) {
            this.p2PServiceProvider = new P2PServiceProvider();
            this.p2PServiceProvider.setDebugSoap(true);
            this.p2PServiceProvider.setWsdlLocation(p2pWsdlLocation);
        }

        this.integrationSupport = integrationSupport;
    }

    MerchantService getMerchantService() {
        if ( null == merchantService ) {
            merchantService = merchantServiceProvider.getMerchantService(namePasswordToken);
        }
        return merchantService;
    }

    P2PWSController getP2PWSController() {
        if ( null == p2PWSController && null != p2PServiceProvider && null != p2pNamePasswordToken ) {
            p2PWSController = p2PServiceProvider.getP2PService(p2pNamePasswordToken);
        }
        return p2PWSController;
    }

    @Override
    public IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
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
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentBindingRequest.getOrderId());

        String externalOrderId = null;
        try {
            RegisterOrderResponse registerOrderResponse = getMerchantService().registerOrder(rbsParams);
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
            PaymentOrderResult paymentOrderResult = getMerchantService().paymentOrderBinding(paymentOrderBindingParams);
            handlePaymentResult(integrationPaymentResponse, integrationPaymentBindingRequest, externalOrderId, paymentOrderResult);
        } catch (Exception e) {
            throw new IntegrationException("Error integration BINDING payment order by orderId: " + integrationPaymentBindingRequest.getOrderId(),e);
        }
        return integrationPaymentResponse;
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
        if ( null != integrationPaymentRequest.getClient() ) {
            Client client = integrationPaymentRequest.getClient();
            rbsParams.setClientId(client.getClientId());
            if ( null != integrationPaymentRequest.getIp() ) rbsParams.getParams().add(Util.createServiceParam(Client.IP_PARAM,integrationPaymentRequest.getIp()));
            if ( null != client.getEmail() ) rbsParams.getParams().add(Util.createServiceParam(Client.EMAIL_PARAM,client.getEmail()));
            if ( null != client.getPhone() ) rbsParams.getParams().add(Util.createServiceParam(Client.PHONE_PARAM, client.getPhone()));
        }
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentRequest.getOrderId());

        String externalOrderId = null;
        PaymentType paymentType = PaymentType.valueOf(sPaymentType);
        try {
            RegisterOrderResponse registerOrderResponse = null;
            if ( PaymentType.PURCHASE == paymentType ) {
                registerOrderResponse = getMerchantService().registerOrder(rbsParams);
            }
            else if ( PaymentType.DEPOSIT == paymentType ) {
                registerOrderResponse = getMerchantService().registerOrderPreAuth(rbsParams);
            }
            else {
                integrationPaymentResponse.setSuccess(false);
                integrationPaymentResponse.setMessage("Unknown paymentTye: " + paymentType);
                return integrationPaymentResponse;
            }
            integrationPaymentResponse.setPaymentType(paymentType);

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
        paymentOrderParams.setIp(integrationPaymentRequest.getIp());
        if ( null != integrationPaymentRequest.getClient() ) {
            paymentOrderParams.setEmail(integrationPaymentRequest.getClient().getEmail());
        }
        try {
            PaymentOrderResult paymentOrderResult = getMerchantService().paymentOrder(paymentOrderParams);
            handlePaymentResult(integrationPaymentResponse,integrationPaymentRequest,externalOrderId,paymentOrderResult);
        } catch (Exception e) {
            throw new IntegrationException("Error integration payment order by orderId: " + integrationPaymentRequest.getOrderId(),e);
        }

        return integrationPaymentResponse;
    }

    @Override
    public IntegrationP2PTransferResponse p2pTransfer(IntegrationP2PTransferRequest integrationP2PTransferRequest) throws IntegrationException {
        IntegrationP2PTransferResponse integrationP2PTransferResponse = new IntegrationP2PTransferResponse();
        //1. p2pregister
        P2PRegistrationRequest p2PRegistrationRequest = new P2PRegistrationRequest();
        p2PRegistrationRequest.setAmount(integrationP2PTransferRequest.getAmount());
        p2PRegistrationRequest.setCurrency(currency);
        p2PRegistrationRequest.setOrderDescription(integrationP2PTransferRequest.getDescription());
        p2PRegistrationRequest.setEmail(integrationP2PTransferRequest.getEmail());
        p2PRegistrationRequest.setLanguage(language);
        p2PRegistrationRequest.setFailUrl("http://google.com");
        p2PRegistrationRequest.setReturnUrl("http://ya.ru");
        p2PRegistrationRequest.setOrderNumber(UUID.randomUUID().toString());
        P2PRegistrationResponse p2PRegistrationResponse = getP2PWSController().registerP2P(p2PRegistrationRequest);
        if ( 0 != p2PRegistrationResponse.getErrorCode() ) {
            integrationP2PTransferResponse.setSuccess(false);
            integrationP2PTransferResponse.setMessage(p2PRegistrationResponse.getErrorMessage());
            return integrationP2PTransferResponse;
        }
        integrationP2PTransferResponse.setExternalOrderId(p2PRegistrationResponse.getOrderId());
        integrationP2PTransferResponse.setOrderNumber(p2PRegistrationResponse.getOrderNumber());

        //2. p2pverify
        P2PTransferVerificationRequest p2PTransferVerificationRequest = new P2PTransferVerificationRequest();
        p2PTransferVerificationRequest.setOrderId(p2PRegistrationResponse.getOrderId());
        CardPaymentParams cardPaymentParams = integrationP2PTransferRequest.getPaymentParams();
        if ( !cardPaymentParams.isNotBlank() ) {
            integrationP2PTransferResponse.setSuccess(false);
            integrationP2PTransferResponse.setMessage("Card data invalid");
            return integrationP2PTransferResponse;
        }
        CardData fromCard = new CardData();
        fromCard.setCardholderName(cardPaymentParams.getCardHolderName());
        fromCard.setCvc(cardPaymentParams.getCvc());
        fromCard.setExpirationMonth(Integer.valueOf(cardPaymentParams.getMonth()));
        fromCard.setExpirationYear(Integer.valueOf(cardPaymentParams.getYear()));
        fromCard.setPan(cardPaymentParams.getPan());

        CardData toCard = new CardData();
        toCard.setPan(integrationP2PTransferRequest.getToCardPan());

        p2PTransferVerificationRequest.setFromCard(fromCard);
        p2PTransferVerificationRequest.setToCard(toCard);
        P2PTransferVerificationResponse p2PTransferVerificationResponse = getP2PWSController().verifyP2P(p2PTransferVerificationRequest);
        if ( 0 != p2PTransferVerificationResponse.getErrorCode() ) {
            integrationP2PTransferResponse.setSuccess(false);
            integrationP2PTransferResponse.setMessage(p2PTransferVerificationResponse.getErrorMessage());
            return integrationP2PTransferResponse;
        }
        long totalFee = 0L;
        for (FeeDescription feeDescription : p2PTransferVerificationResponse.getFeeDescriptionList()) {
            totalFee += feeDescription.getFeeAmount();
        }
        logger.debug("Total fee p2p transfer: {} ", totalFee);

        //3. p2pperform
        P2PTransferRequest p2PTransferRequest = new P2PTransferRequest();
        p2PTransferRequest.setOrderId(p2PRegistrationResponse.getOrderId());
        p2PTransferRequest.setFromCard(fromCard);
        p2PTransferRequest.setToCard(toCard);
        p2PTransferRequest.setAmountInput(integrationP2PTransferRequest.getAmount());//todo: тут надо вычитать комиссию
        p2PTransferRequest.setType(P2PTransferType.STANDARD);
        p2PTransferRequest.setEmail(integrationP2PTransferRequest.getEmail());
        P2PTransferResponse p2PTransferResponse = getP2PWSController().performP2P(p2PTransferRequest);
        if ( 0 != p2PTransferResponse.getErrorCode() ) {
            integrationP2PTransferResponse.setSuccess(false);
            integrationP2PTransferResponse.setMessage(p2PTransferResponse.getErrorMessage());
            return integrationP2PTransferResponse;
        }
        //todo: сделать проверку на наличие ацс
        integrationP2PTransferResponse.setSuccess(true);
        integrationP2PTransferResponse.setMessage("P2P transfer success");
        return integrationP2PTransferResponse;
    }

    private void handlePaymentResult(IntegrationPaymentResponse integrationPaymentResponse, IntegrationPaymentRequest integrationPaymentRequest, String externalOrderId, PaymentOrderResult paymentOrderResult) throws IntegrationException {
        integrationPaymentResponse.setMessage(paymentOrderResult.getErrorMessage());
        if ( 0 != paymentOrderResult.getErrorCode() ) {
            logger.error("Error payment order: " + integrationPaymentRequest.getOrderId() + " because: " + paymentOrderResult.getErrorMessage());
            integrationPaymentResponse.setSuccess(false);
            return;
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
            integrationPaymentResponse.setPaymentSecureType(PaymentSecureType.TDS);
        }
        else {
            IntegrationOrderStatusResponse integrationOrderStatusResponse = getOrderStatus(new IntegrationOrderStatusRequest(integrationPaymentRequest.getIntegrationSupport(), externalOrderId));
            integrationPaymentResponse.setIntegrationOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus());
            integrationOrderStatusResponse.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
            integrationPaymentResponse.setPaymentSecureType(PaymentSecureType.SSL);
            integrationPaymentResponse.setRedirectUrlOrPagePath("redirect:" + paymentOrderResult.getRedirect());
        }
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
            CardBindingInfo cardBindingInfo = getOrderStatusExtendedResponse.getBindingInfo();
            if ( null != cardBindingInfo ) {
                integrationOrderStatusResponse.setBindingInfo(new BindingInfo(cardBindingInfo.getClientId(), cardBindingInfo.getBindingId()));
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
    public IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException {
        IntegrationCompletionResponse integrationCompletionResponse = new IntegrationCompletionResponse();
        integrationCompletionResponse.setOrderId(integrationCompletionRequest.getOrderId());
        integrationCompletionResponse.setExternalOrderId(integrationCompletionRequest.getExternalOrderId());
        try {
            DepositOrderParams depositOrderParams = new DepositOrderParams();
            depositOrderParams.setOrderId(integrationCompletionRequest.getExternalOrderId());
            depositOrderParams.setDepositAmount(integrationCompletionRequest.getAmount());
            OrderResult orderResult = getMerchantService().depositOrder(depositOrderParams);
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
                case REDIRECTED_TO_ACS: return OrderStatus.REGISTERED;
            }
            return OrderStatus.REGISTERED;
        }
        throw new IllegalArgumentException("Unknown integration order status: " + integrationOrderStatus);
    }

}
