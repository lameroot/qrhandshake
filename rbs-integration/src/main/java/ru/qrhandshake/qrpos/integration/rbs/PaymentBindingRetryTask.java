package ru.qrhandshake.qrpos.integration.rbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentBindingRequest;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;
import ru.rbs.commons.cluster.retry.Retriable;

import javax.annotation.Resource;
import java.util.Date;

public class PaymentBindingRetryTask implements Retriable<IntegrationPaymentBindingRequest>  {

    private final static Logger logger = LoggerFactory.getLogger(PaymentBindingRetryTask.class);

    @Value("${integration.rbs.paymentBindingRetry.maxAttempts:3}")
    private Integer maxAttempts;

    @Resource
    private RbsIntegrationFacade rbsIntegrationFacade;
    @Resource
    private MerchantOrderRepository merchantOrderRepository;

    @Override
    public void execute(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) {
        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(integrationPaymentBindingRequest.getOrderId());
        if ( null == merchantOrder ) {
            logger.error("During async paymentBinding {} order with orderId: {} not found",integrationPaymentBindingRequest, integrationPaymentBindingRequest.getOrderId());
            //todo: ошибку куда то надо логировать, так как это серьёзно
            return;
        }
        try {
            IntegrationPaymentResponse integrationPaymentResponse = rbsIntegrationFacade.paymentBinding(integrationPaymentBindingRequest);
            merchantOrder.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());
            merchantOrder.setPaymentType(integrationPaymentResponse.getPaymentType());
            merchantOrder.setOrderStatus(integrationPaymentResponse.getOrderStatus());
            merchantOrder.setExternalOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus().getStatus());
            merchantOrder.setExternalId(integrationPaymentResponse.getExternalId());

            if ( !integrationPaymentResponse.isSuccess() ) {
                throw new RuntimeException("Invalid status, go to retry");
            }
            //rbsIntegrationFacade.getOrderStatus()
            merchantOrderRepository.save(merchantOrder);
        } catch (IntegrationException e) {
            throw new RuntimeException("");
        }

    }

    @Override
    public Date next(int attemptNumber) {
        return null;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }
}
