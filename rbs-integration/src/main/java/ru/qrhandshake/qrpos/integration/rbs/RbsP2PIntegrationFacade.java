package ru.qrhandshake.qrpos.integration.rbs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import ru.bpc.phoenix.proxy.api.NamePasswordToken;
import ru.bpc.phoenix.proxy.api.P2PServiceProvider;
import ru.bpc.phoenix.web.api.merchant.soap.p2p.P2PWSController;
import ru.paymentgate.engine.webservices.p2p.*;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.UserPasswordEndpoint;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.IntegrationP2PTransferRequest;
import ru.qrhandshake.qrpos.integration.IntegrationP2PTransferResponse;
import ru.qrhandshake.qrpos.integration.P2pIntegrationFacade;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RbsP2PIntegrationFacade implements P2pIntegrationFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String language = "ru";
    private final static String currency = "643";

    @Resource
    private Environment environment;

    private P2PServiceProvider p2PServiceProvider;
    private IntegrationSupport integrationSupport;
    private Map<Endpoint, P2PWSController> p2PWSControllerMap = new HashMap<>();

    public RbsP2PIntegrationFacade(@NotNull IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }

    @Nullable
    P2PWSController getP2PWSController(@NotNull Endpoint endpoint) {
        P2PWSController p2PWSController = null;
        if ( null == p2PServiceProvider ) {
            throw new IllegalArgumentException("P2P unsupported");
        }
        UserPasswordEndpoint userPasswordEndpoint = (UserPasswordEndpoint)endpoint;
        if ( null == (p2PWSController = p2PWSControllerMap.get(userPasswordEndpoint)) ) {
            if ( null == p2PServiceProvider ) {
                this.p2PServiceProvider = new P2PServiceProvider();
                this.p2PServiceProvider.setDebugSoap(true);
                this.p2PServiceProvider.setWsdlLocation(endpoint.getEndpointCatalog().getAddress());
            }
            p2PWSController = p2PServiceProvider.getP2PService(new NamePasswordToken(userPasswordEndpoint.getUsername(),userPasswordEndpoint.getPassword()));
            p2PWSControllerMap.put(userPasswordEndpoint,p2PWSController);
        }
        if ( null == p2PWSController ) throw new IllegalArgumentException("Can't create p2pController");
        return p2PWSController;
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
        P2PRegistrationResponse p2PRegistrationResponse = getP2PWSController(integrationP2PTransferRequest.getEndpoint()).registerP2P(p2PRegistrationRequest);
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
        PaymentParams paymentParams = integrationP2PTransferRequest.getPaymentParams();
        if ( !(paymentParams instanceof CardPaymentParams) ) {
            integrationP2PTransferResponse.setSuccess(false);
            integrationP2PTransferResponse.setMessage("Card data invalid");
            return integrationP2PTransferResponse;
        }
        CardPaymentParams cardPaymentParams = (CardPaymentParams)paymentParams;
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
        toCard.setPan(integrationP2PTransferRequest.getTo());

        p2PTransferVerificationRequest.setFromCard(fromCard);
        p2PTransferVerificationRequest.setToCard(toCard);
        P2PTransferVerificationResponse p2PTransferVerificationResponse = getP2PWSController(integrationP2PTransferRequest.getEndpoint()).verifyP2P(p2PTransferVerificationRequest);
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
        P2PTransferResponse p2PTransferResponse = getP2PWSController(integrationP2PTransferRequest.getEndpoint()).performP2P(p2PTransferRequest);
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

    @Override
    public boolean isApplicable() {
        return environment.acceptsProfiles(RbsIntegrationConfig.RBS_PROFILE);
    }

    @Override
    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }
}
