package ru.qrhandshake.qrpos.integration.rbs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.repository.ClientRepository;

import javax.annotation.Resource;

public class RbsIntegrationFacade implements IntegrationFacade {

    private final static Logger logger = LoggerFactory.getLogger(RbsIntegrationFacade.class);

    @Resource
    private Environment environment;
    @Resource
    private ClientRepository clientRepository;
    @Value("${integration.rbs.paymentBinding.async.enabled:false}")
    private boolean paymentBindingAsyncEnabled;
    @Value("${integration.rbs.paymentBinding.async.maxAmount:10000}")
    private Long paymentBindingAsyncMaxAmount;

    private final IntegrationSupport integrationSupport;
    private final RbsSyncIntegrationFacade rbsSyncIntegrationFacade;
    private RbsAsyncIntegrationFacade rbsAsyncIntegrationFacade;

    public RbsIntegrationFacade(@NotNull IntegrationSupport integrationSupport, @NotNull RbsSyncIntegrationFacade rbsSyncIntegrationFacade, @Nullable RbsAsyncIntegrationFacade rbsAsyncIntegrationFacade) {
        this.integrationSupport = integrationSupport;
        this.rbsSyncIntegrationFacade = rbsSyncIntegrationFacade;
        this.rbsAsyncIntegrationFacade = rbsAsyncIntegrationFacade;
    }

    @Override
    public IntegrationPaymentResponse payment(IntegrationPaymentRequest integrationPaymentRequest) throws IntegrationException {
        return rbsSyncIntegrationFacade.payment(integrationPaymentRequest);
    }

    @Override
    public IntegrationCompletionResponse completion(IntegrationCompletionRequest integrationCompletionRequest) throws IntegrationException {
        return rbsSyncIntegrationFacade.completion(integrationCompletionRequest);
    }

    @Override
    public IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
        if ( null != rbsAsyncIntegrationFacade && paymentBindingAsyncEnabled
                && integrationPaymentBindingRequest.getClient().isAccountNonLocked()
                && integrationPaymentBindingRequest.getAmount() <= paymentBindingAsyncMaxAmount ) {
            logger.debug("So [paymentBindingAsyncEnabled={}], client not locked and amount less than {}, payment async by binding: {}",paymentBindingAsyncEnabled, paymentBindingAsyncMaxAmount, integrationPaymentBindingRequest);
            IntegrationPaymentResponse integrationSyncPaymentResponse = rbsAsyncIntegrationFacade.paymentBinding(integrationPaymentBindingRequest);
            if ( !integrationSyncPaymentResponse.isSuccess() ) {
                Client client = clientRepository.findOne(integrationPaymentBindingRequest.getClient().getId());
                client.setLocked(true);
                clientRepository.save(client);
                logger.debug("Sync payment by binding: {} was failed, so {} will be locked.", integrationPaymentBindingRequest, client);
            }
            else if ( !integrationPaymentBindingRequest.getClient().isAccountNonLocked() ) {
                Client client = clientRepository.findOne(integrationPaymentBindingRequest.getClient().getId());
                client.setLocked(false);
                clientRepository.save(client);
                logger.debug("Sync payment by binding: {} was success and {} was locked, so unlock this client", integrationPaymentBindingRequest, client);
            }
            return integrationSyncPaymentResponse;
        }
        logger.debug("Payment sync by binding: {}, because paymentBindingAsyncEnabled = {} or {} is locked or amount: {} more than {}", integrationPaymentBindingRequest, paymentBindingAsyncEnabled, integrationPaymentBindingRequest.getClient(), integrationPaymentBindingRequest.getAmount(), paymentBindingAsyncMaxAmount);
        return rbsSyncIntegrationFacade.paymentBinding(integrationPaymentBindingRequest);
    }

    @Override
    public IntegrationOrderStatusResponse getOrderStatus(IntegrationOrderStatusRequest integrationOrderStatusRequest) throws IntegrationException {
        return rbsSyncIntegrationFacade.getOrderStatus(integrationOrderStatusRequest);
    }

    @Override
    public IntegrationReverseResponse reverse(IntegrationReverseRequest integrationReverseRequest) throws IntegrationException {
        return rbsSyncIntegrationFacade.reverse(integrationReverseRequest);
    }

    @Override
    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    @Override
    public OrderStatus toOrderStatus(IntegrationOrderStatus integrationOrderStatus) {
        return integrationToOrderStatus(integrationOrderStatus);
    }

    @Override
    public boolean isApplicable() {
        return environment.acceptsProfiles(RbsIntegrationConfig.RBS_PROFILE);
    }

    public static OrderStatus integrationToOrderStatus(IntegrationOrderStatus integrationOrderStatus) {
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
        return null;
    }
}
