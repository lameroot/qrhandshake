package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by lameroot on 25.05.16.
 */
@Service
public class OrderService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TerminalService terminalService;
    @Resource
    private MerchantOrderRepository merchantOrderRepository;
    @Resource
    private IntegrationService integrationService;

    public MerchantOrderRegisterResponse register(MerchantOrderRegisterRequest merchantOrderRegisterRequest) throws AuthException {
        Terminal terminal = null;
        if ( null == (terminal = terminalService.auth(merchantOrderRegisterRequest)) ) {
            throw new AuthException(merchantOrderRegisterRequest);
        }
        Merchant merchant = terminal.getMerchant();

        MerchantOrder merchantOrder = new MerchantOrder();
        merchantOrder.setMerchant(merchant);
        merchantOrder.setTerminal(terminal);
        merchantOrder.setDeviceId(merchantOrderRegisterRequest.getDeviceId());
        merchantOrder.setAmount(merchantOrderRegisterRequest.getAmount());
        merchantOrder.setDescription(merchantOrderRegisterRequest.getDescription());
        merchantOrder.setOrderStatus(OrderStatus.REGISTERED);
        merchantOrder.setSessionId(merchantOrderRegisterRequest.getSessionId());
        merchantOrderRepository.save(merchantOrder);

        String orderId = generateUniqueIdOrder(merchantOrder);
        String paymentUrl = buildPaymentUrl(merchantOrder, orderId);
        merchantOrder.setOrderId(orderId);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = new MerchantOrderRegisterResponse();
        merchantOrderRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        merchantOrderRegisterResponse.setMessage("Merchant order success created");
        merchantOrderRegisterResponse.setOrderId(orderId);
        merchantOrderRegisterResponse.setPaymentUrl(paymentUrl);

        return merchantOrderRegisterResponse;
    }

    public MerchantOrderStatusResponse getOrderStatus(MerchantOrderStatusRequest merchantOrderStatusRequest) throws AuthException{
        Terminal terminal = null;
        if ( null == (terminal = terminalService.auth(merchantOrderStatusRequest)) ) {
            throw new AuthException(merchantOrderStatusRequest);
        }
        MerchantOrderStatusResponse merchantOrderStatusResponse = new MerchantOrderStatusResponse();
        MerchantOrder merchantOrder = findByOrderId(merchantOrderStatusRequest.getOrderId());
        if ( null == merchantOrder ) {
            merchantOrderStatusResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderStatusResponse.setMessage("Order with id: " + merchantOrderStatusRequest.getOrderId() + " not found");
            return merchantOrderStatusResponse;
        }
        if ( !terminal.getMerchant().equals(merchantOrder.getMerchant()) ) {
            throw new AuthException("Order with id:" + merchantOrderStatusRequest.getOrderId() + " not belongs to own merchant");
        }
        if ( doExternalOrderStatusRequest(merchantOrder) ) {//делаем в этом случае всегда запрос к внешней системе
            IntegrationOrderStatusRequest integrationOrderStatusRequest = new IntegrationOrderStatusRequest(merchantOrder.getIntegrationSupport(),merchantOrder.getExternalId());
            try {
                IntegrationOrderStatusResponse integrationOrderStatusResponse = integrationService.getOrderStatus(integrationOrderStatusRequest);
                merchantOrderStatusResponse.setMessage(integrationOrderStatusResponse.getMessage());
                if ( !merchantOrder.getOrderStatus().equals(integrationOrderStatusResponse.getOrderStatus()) ) {
                    merchantOrder.setOrderStatus(integrationOrderStatusResponse.getOrderStatus());
                    merchantOrder.setExternalOrderStatus(integrationOrderStatusResponse.getIntegrationOrderStatus().getStatus());
                    if ( merchantOrder.getOrderStatus().equals(OrderStatus.PAID) ) {
                        merchantOrder.setPaymentDate(new Date());
                    }
                    merchantOrderRepository.save(merchantOrder);
                }
            } catch (IntegrationException e) {
                merchantOrderStatusResponse.setStatus(ResponseStatus.FAIL);
                merchantOrderStatusResponse.setMessage("Unable to get external order status for order:" + merchantOrder.getId());
                merchantOrderStatusResponse.setAmount(merchantOrder.getAmount());
                merchantOrderStatusResponse.setOrderId(merchantOrder.getOrderId());
                merchantOrderStatusResponse.setOrderStatus(merchantOrder.getOrderStatus());
                return merchantOrderStatusResponse;
            }
        }
        merchantOrderStatusResponse.setAmount(merchantOrder.getAmount());
        merchantOrderStatusResponse.setStatus(ResponseStatus.SUCCESS);
        merchantOrderStatusResponse.setMessage("Order status for: " + merchantOrder.getId() + " received");
        merchantOrderStatusResponse.setOrderStatus(merchantOrder.getOrderStatus());
        merchantOrderStatusResponse.setOrderId(merchantOrder.getOrderId());

        return merchantOrderStatusResponse;
    }

    public PaymentResponse payment(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = new PaymentResponse();
        MerchantOrder merchantOrder = findByOrderId(paymentRequest.getOrderId());
        if ( null == merchantOrder ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Order: " + paymentRequest.getOrderId() + " not found");
            paymentResponse.setOrderId(paymentRequest.getOrderId());
            return paymentResponse;
        }
        if ( null != merchantOrder.getOrderStatus() && OrderStatus.REGISTERED.equals(merchantOrder.getOrderStatus()) ) {
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setMessage("Order: " + merchantOrder.getOrderId() + " has already paid");
            return paymentResponse;
        }
        IntegrationPaymentRequest integrationPaymentRequest = new IntegrationPaymentRequest();
        integrationPaymentRequest.setAmount(merchantOrder.getAmount());
        integrationPaymentRequest.setCardHolderName(paymentRequest.getCardHolderName());
        integrationPaymentRequest.setClient(null);//todo: set data as ip
        integrationPaymentRequest.setCvc(paymentRequest.getCvc());
        integrationPaymentRequest.setDescription(merchantOrder.getDescription());
        integrationPaymentRequest.setOrderId(paymentRequest.getOrderId());
        integrationPaymentRequest.setMonth(paymentRequest.getMonth());
        integrationPaymentRequest.setYear(paymentRequest.getYear());
        integrationPaymentRequest.setPan(paymentRequest.getPan());
        integrationPaymentRequest.setReturnUrl(paymentRequest.getReturnUrl());
        integrationPaymentRequest.setParams(new HashMap<>());//todo: set params
        integrationPaymentRequest.setOrderStatus(merchantOrder.getOrderStatus());

        try {
            IntegrationPaymentResponse integrationPaymentResponse = integrationService.payment(integrationPaymentRequest);
            paymentResponse.setStatus(ResponseStatus.SUCCESS);
            paymentResponse.setOrderId(merchantOrder.getOrderId());
            paymentResponse.setMessage(integrationPaymentResponse.getMessage());
            if ( !merchantOrder.getOrderStatus().equals(integrationPaymentResponse.getOrderStatus()) ) {
                merchantOrder.setOrderStatus(integrationPaymentResponse.getOrderStatus());
                merchantOrder.setExternalOrderStatus(integrationPaymentResponse.getIntegrationOrderStatus().getStatus());
                if ( merchantOrder.getOrderStatus().equals(OrderStatus.PAID) ) {
                    merchantOrder.setPaymentDate(new Date());
                }
                merchantOrderRepository.save(merchantOrder);
                paymentResponse.setMessage("Paid successfully");
            }
            else {
                paymentResponse.setMessage("Paid successfully but external status wasn't changed");
            }
            paymentResponse.setOrderStatus(merchantOrder.getOrderStatus());
            paymentResponse.setAcsUrl(integrationPaymentResponse.getAcsUrl());
            paymentResponse.setPaReq(integrationPaymentResponse.getPaReq());
            paymentResponse.setTermUrl(integrationPaymentResponse.getTermUrl());
        } catch (IntegrationException e) {
            logger.error("Error payment by orderId:" + paymentRequest.getOrderId(),e);
            paymentResponse.setStatus(ResponseStatus.FAIL);
            paymentResponse.setMessage("Error external payment by id: " + paymentRequest.getOrderId());
            paymentResponse.setOrderId(paymentRequest.getOrderId());
        }
        return paymentResponse;
    }

    public MerchantOrderReverseResponse reverse(MerchantOrderReverseRequest merchantOrderReverseRequest) throws AuthException {
        Terminal terminal = null;
        if ( null == (terminal = terminalService.auth(merchantOrderReverseRequest)) ) {
            throw new AuthException(merchantOrderReverseRequest);
        }
        MerchantOrderReverseResponse merchantOrderReverseResponse = new MerchantOrderReverseResponse();
        merchantOrderReverseResponse.setOrderId(merchantOrderReverseRequest.getOrderId());

        MerchantOrder merchantOrder = findByOrderId(merchantOrderReverseRequest.getOrderId());
        if ( null == merchantOrder ) {
            merchantOrderReverseResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderReverseResponse.setMessage("Order with id: " + merchantOrderReverseResponse.getOrderId() + " not found");
            return merchantOrderReverseResponse;
        }
        if ( !isSessionValid(merchantOrder, merchantOrderReverseRequest.getSessionId()) ) {
            throw new AuthException("Invalid sessionId");
        }
        if ( !terminal.getMerchant().equals(merchantOrder.getMerchant()) ) {
            throw new AuthException("Order with id:" + merchantOrderReverseRequest.getOrderId() + " not belongs to own merchant");
        }

        try {
            IntegrationReverseRequest integrationReverseRequest = new IntegrationReverseRequest(merchantOrder.getIntegrationSupport());
            integrationReverseRequest.setOrderId(merchantOrderReverseRequest.getOrderId());

            IntegrationReverseResponse integrationReverseResponse = integrationService.reverse(integrationReverseRequest);
            merchantOrderReverseResponse.setStatus(ResponseStatus.SUCCESS);
            merchantOrderReverseResponse.setMessage(integrationReverseResponse.getMessage());
        } catch (IntegrationException e) {
            logger.error("Error reverse order by orderId: " + merchantOrderReverseRequest.getOrderId(),e);
            merchantOrderReverseResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderReverseResponse.setMessage("Error external reverse by orderId: " + merchantOrderReverseRequest.getOrderId());

        }
        return merchantOrderReverseResponse;
    }

    public void back(Model model, HttpServletRequest request) {
        integrationService.back(model, request);
    }

    public MerchantOrder findByOrderId(String orderId) {
        return merchantOrderRepository.findByOrderId(orderId);
    }

    private boolean doExternalOrderStatusRequest(MerchantOrder merchantOrder) {
        OrderStatus orderStatus = merchantOrder.getOrderStatus();
        return null != orderStatus && orderStatus.equals(OrderStatus.REGISTERED) && StringUtils.isNotBlank(merchantOrder.getExternalId());
    }

    private String buildPaymentUrl(MerchantOrder merchantOrder, String orderId) {
        return MerchantOrderController.PAYMENT_PATH + "/" + orderId;
    }

    private String generateUniqueIdOrder(MerchantOrder merchantOrder) {
        return String.valueOf(merchantOrder.getId());
    }

    private boolean isSessionValid(MerchantOrder merchantOrder, String sessionId) {
        return merchantOrder.getSessionId().equals(sessionId);
    }
}
