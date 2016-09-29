package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.exception.IntegrationException;

public interface P2pIntegrationFacade {

    IntegrationP2PTransferResponse p2pTransfer(IntegrationP2PTransferRequest integrationP2PTransferRequest) throws IntegrationException;
    boolean isApplicable();
    IntegrationSupport getIntegrationSupport();
}
