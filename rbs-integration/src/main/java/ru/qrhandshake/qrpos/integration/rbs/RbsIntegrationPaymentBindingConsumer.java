package ru.qrhandshake.qrpos.integration.rbs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentBindingRequest;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentResponse;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;

import javax.annotation.Resource;

public class RbsIntegrationPaymentBindingConsumer implements ChannelAwareMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(RbsIntegrationPaymentBindingConsumer.class);

    @Resource
    private RbsIntegrationFacade rbsIntegrationFacade;
    @Resource
    private MerchantOrderRepository merchantOrderRepository;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        logger.debug("Receive message from queue: {}", message.getMessageProperties().getConsumerQueue());
        IntegrationPaymentBindingRequest integrationPaymentBindingRequest = objectMapper.readValue(message.getBody(), IntegrationPaymentBindingRequest.class);
        integrationPaymentBindingRequest.setForceSync(true);
        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(integrationPaymentBindingRequest.getOrderId());
        if ( null == merchantOrder ) {
            logger.error("During async paymentBinding {} order with orderId: {} not found",integrationPaymentBindingRequest, integrationPaymentBindingRequest.getOrderId());
            //todo: ошибку куда то надо логировать, так как это серьёзно
            return;
        }

        IntegrationPaymentResponse integrationPaymentResponse = rbsIntegrationFacade.paymentBinding(integrationPaymentBindingRequest);
        merchantOrder.setPaymentSecureType(integrationPaymentResponse.getPaymentSecureType());
        merchantOrder.setPaymentType(integrationPaymentResponse.getPaymentType());
        merchantOrder.setOrderStatus(integrationPaymentResponse.getOrderStatus());
        merchantOrder.setExternalOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus().getStatus());
        merchantOrder.setExternalId(integrationPaymentResponse.getExternalId());
        if ( !integrationPaymentResponse.isSuccess() ) {

        }

        merchantOrderRepository.save(merchantOrder);
    }
}
