package ru.qrhandshake.qrpos.integration.yandex;

import com.yandex.money.api.exceptions.InsufficientScopeException;
import com.yandex.money.api.exceptions.InvalidRequestException;
import com.yandex.money.api.exceptions.InvalidTokenException;
import com.yandex.money.api.methods.ProcessPayment;
import com.yandex.money.api.methods.RequestPayment;
import com.yandex.money.api.methods.params.P2pTransferParams;
import com.yandex.money.api.methods.params.PaymentParams;
import com.yandex.money.api.net.DefaultApiClient;
import com.yandex.money.api.net.OAuth2Session;
import org.springframework.beans.factory.annotation.Value;
import ru.qrhandshake.qrpos.api.YandexMoneyPaymentParams;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.*;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by lameroot on 22.06.16.
 */
public class YandexMoneyIntegrationFacade implements P2pIntegrationFacade {

    @Value("${yandex.money.clientId}")
    private String clientId;
    @Value("${yandex.money.accessToken}")
    private String accessToken;
    @Value("${yandex.money.debug:true}")
    private boolean debug;
    @Value("${yandex.money.test:true}")
    private boolean test;
    @Value("${yandex.money.test.testResult:success}")
    private String testResult;

    @Override
    public IntegrationSupport getIntegrationSupport() {
        return null;
    }

    @Override
    public IntegrationP2PTransferResponse p2pTransfer(IntegrationP2PTransferRequest integrationP2PTransferRequest) throws IntegrationException {
        OAuth2Session session = new OAuth2Session(new DefaultApiClient(clientId, debug));
        session.setDebugLogging(debug);
        session.setAccessToken(accessToken);

        IntegrationP2PTransferResponse integrationP2PTransferResponse = new IntegrationP2PTransferResponse();
        if ( !(integrationP2PTransferRequest.getPaymentParams() instanceof YandexMoneyPaymentParams) ) {
            integrationP2PTransferResponse.setSuccess(false);
            integrationP2PTransferResponse.setMessage("Yandex data invalid");
            return integrationP2PTransferResponse;
        }
        YandexMoneyPaymentParams yandexMoneyPaymentParamsPaymentParams = (YandexMoneyPaymentParams)integrationP2PTransferRequest.getPaymentParams();

        PaymentParams paymentParams = new P2pTransferParams.Builder(integrationP2PTransferRequest.getTo())
                .setAmount(new BigDecimal(integrationP2PTransferRequest.getAmount()))
                .setMessage(integrationP2PTransferRequest.getDescription())
                .setComment(integrationP2PTransferRequest.getDescription())
                .create();
        RequestPayment.Request requestPayment = RequestPayment.Request.newInstance(paymentParams);
        if ( test ) requestPayment.setTestResult(RequestPayment.TestResult.valueOf(testResult.toUpperCase()));
        try {
            RequestPayment requestPaymentResponse = session.execute(requestPayment);
            //todo: check response
            ProcessPayment.Request processPayment = new ProcessPayment.Request(requestPaymentResponse.requestId);
            if ( test ) processPayment.setTestResult(ProcessPayment.TestResult.valueOf(testResult.toUpperCase()));
            ProcessPayment processPaymentResponse = session.execute(processPayment);
            //todo: check response

            integrationP2PTransferResponse.setSuccess(true);
            //todo: set externalId
        } catch (Exception e) {
            e.printStackTrace();
            integrationP2PTransferResponse.setMessage(e.getMessage());
            integrationP2PTransferResponse.setSuccess(false);
        } finally {
            session.setAccessToken(null);
        }

        return integrationP2PTransferResponse;
    }

    @Override
    public boolean isApplicable() {
        return false;
    }
}
