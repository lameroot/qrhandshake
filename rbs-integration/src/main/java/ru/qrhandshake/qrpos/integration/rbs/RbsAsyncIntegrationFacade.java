package ru.qrhandshake.qrpos.integration.rbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.domain.PaymentSecureType;
import ru.qrhandshake.qrpos.domain.PaymentType;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentBindingRequest;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentResponse;
import ru.rbs.commons.cluster.retry.RetriableExecutor;

import javax.annotation.Resource;

class RbsAsyncIntegrationFacade {

    private final Logger logger = LoggerFactory.getLogger(RbsAsyncIntegrationFacade.class);

    @Resource
    private RetriableExecutor retriableExecutor;
    @Resource
    private PaymentBindingRetryTask paymentBindingRetryTask;

    public IntegrationPaymentResponse paymentBinding(IntegrationPaymentBindingRequest integrationPaymentBindingRequest) throws IntegrationException {
        IntegrationPaymentResponse integrationPaymentResponse = new IntegrationPaymentResponse();
        integrationPaymentResponse.setOrderId(integrationPaymentBindingRequest.getOrderId());
        integrationPaymentResponse.setPaymentType(PaymentType.PURCHASE);
        integrationPaymentResponse.setSuccess(true);
        integrationPaymentResponse.setPaymentSecureType(PaymentSecureType.SSL);
        integrationPaymentResponse.setMessage("Order is pending");
        integrationPaymentResponse.setOrderStatus(OrderStatus.PENDING);
        integrationPaymentResponse.setIntegrationOrderStatus(RbsOrderStatus.CREATED);
        ReturnUrlObject returnUrlObject = new ReturnUrlObject();
        returnUrlObject.setUrl("/order/finish/" + integrationPaymentBindingRequest.getOrderId());//http://localhost:80/order/finish/219_1476906395931?orderId=dd53b41b-c322-4b00-ba5a-a998760c6c17
        returnUrlObject.setAction("redirect");
        integrationPaymentResponse.setReturnUrlObject(returnUrlObject);

        retriableExecutor.execute(integrationPaymentBindingRequest, paymentBindingRetryTask);

        logger.debug("Async paymentBinding for {}", integrationPaymentBindingRequest);
        return integrationPaymentResponse;
    }

}
