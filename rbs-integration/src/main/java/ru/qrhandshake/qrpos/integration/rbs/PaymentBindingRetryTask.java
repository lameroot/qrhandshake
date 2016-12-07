package ru.qrhandshake.qrpos.integration.rbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.PaymentWay;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentBindingRequest;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;
import ru.rbs.commons.cluster.retry.Retriable;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

public class PaymentBindingRetryTask implements Retriable<IntegrationPaymentBindingRequest>  {

    private final static Logger logger = LoggerFactory.getLogger(PaymentBindingRetryTask.class);

    @Value("${integration.rbs.paymentBinding.async.retry.maxAttempts:3}")
    private Integer maxAttempts;

    @Resource
    private MerchantOrderRepository merchantOrderRepository;
    @Resource
    private ClientRepository clientRepository;
    @Resource
    private RbsSyncIntegrationFacade rbsSyncIntegrationFacade;

    @Override
    public void execute(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) {
        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(integrationPaymentBindingRequest.getOrderId());
        if ( null == merchantOrder ) {
            logger.error("During async paymentBinding {} order with orderId: {} not found",integrationPaymentBindingRequest, integrationPaymentBindingRequest.getOrderId());
            //todo: ошибку куда то надо логировать, так как это серьёзно
            return;
        }
        Client client = clientRepository.findOne(integrationPaymentBindingRequest.getClient().getId());
        if ( null == client ) {
            logger.error("Unknown client for request: {}", integrationPaymentBindingRequest);
            return;
        }
        try {
            integrationPaymentBindingRequest.setOrderId(integrationPaymentBindingRequest.getOrderId() + "_" + new Date().getTime());//todo: сделать номер попытки, когда появится код
            IntegrationPaymentResponse integrationPaymentResponse = rbsSyncIntegrationFacade.paymentBinding(integrationPaymentBindingRequest);
            if ( !integrationPaymentResponse.isSuccess() ) {
                throw new RuntimeException("Invalid status, go to retry");
            }

            if ( integrationPaymentResponse.getIntegrationOrderStatus().isPaid() ) {
                merchantOrder.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());
                merchantOrder.setPaymentType(integrationPaymentResponse.getPaymentType());
                merchantOrder.setOrderStatus(RbsIntegrationFacade.integrationToOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus()));
                merchantOrder.setExternalOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus().getStatus());
                merchantOrder.setExternalId(integrationPaymentResponse.getExternalId());
                merchantOrder.setClient(client);
                merchantOrder.setPaymentWay(PaymentWay.BINDING);
                merchantOrder.setIntegrationSupport(integrationPaymentBindingRequest.getEndpoint().getEndpointCatalog().getIntegrationSupport());

                if ( !client.isAccountNonLocked() ) {
                    client.setLocked(false);
                    clientRepository.save(client);
                }
                merchantOrder.setPaymentDate(new Date());
                merchantOrderRepository.save(merchantOrder);
                return;
            }
            else {
                logger.debug("PaymentBinding was successfully, but status is not paid, will try to pay later");
                throw new RuntimeException("PaymentBinding was successfully, but status is not paid, will try to pay later");
            }
        } catch (Exception e) {
            if ( client.isAccountNonLocked() ) {
                client.setLocked(true);
                clientRepository.save(client);
            }
            throw new RuntimeException("Error paymentBinding for " + integrationPaymentBindingRequest,e);
        }
    }

    @Override
    public Date next(int attemptNumber) {
        Calendar current = Calendar.getInstance();
        //current.add(Calendar.HOUR_OF_DAY,-1);
        if ( 0 == attemptNumber ) {
            current.add(Calendar.SECOND, 24*4);
        }
        else if ( 1 == attemptNumber ) {
            current.add(Calendar.SECOND, 24*14);
        }
        else if ( 2 == attemptNumber ) {
            current.add(Calendar.SECOND, 24*28);
        }
        return current.getTime();
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

}
