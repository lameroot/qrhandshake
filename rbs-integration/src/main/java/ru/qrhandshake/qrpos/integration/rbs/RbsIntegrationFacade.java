package ru.qrhandshake.qrpos.integration.rbs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.*;

import javax.annotation.Resource;

public class RbsIntegrationFacade implements IntegrationFacade {

    @Resource
    private Environment environment;
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
            return rbsAsyncIntegrationFacade.paymentBinding(integrationPaymentBindingRequest);
        }
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
        //throw new IllegalArgumentException("Unknown integration order status: " + integrationOrderStatus);
        return null;
    }
}
