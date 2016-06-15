package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.integration.IntegrationP2PTransferRequest;
import ru.qrhandshake.qrpos.integration.IntegrationP2PTransferResponse;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;

import javax.annotation.Resource;

/**
 * Created by lameroot on 09.06.16.
 */
@ActiveProfiles(value = {"prod", RbsIntegrationConfig.RBS_PROFILE})
public class IntegrationServiceTest extends GeneralTest {

    @Resource
    private IntegrationService integrationService;

    @Test
    public void testP2PTransfer() throws Exception {
        CardPaymentParams cardPaymentParams = new CardPaymentParams();
        cardPaymentParams.setYear("2019");
        cardPaymentParams.setMonth("12");
        cardPaymentParams.setCvc("123");
        cardPaymentParams.setCardHolderName("tes tse p2p");
        cardPaymentParams.setPan("5555555555555599");
        IntegrationP2PTransferRequest integrationP2PTransferRequest = new IntegrationP2PTransferRequest(IntegrationSupport.RBS_SBRF,
                cardPaymentParams, "4111111111111111", 10000L);
        IntegrationP2PTransferResponse integrationP2PTransferResponse = integrationService.p2pTransfer(integrationP2PTransferRequest);
        assertTrue(integrationP2PTransferResponse.isSuccess());
    }
}
