package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;

import javax.annotation.Resource;

/**
 * Created by lameroot on 25.05.16.
 */
@Service
public class OrderService {

    @Resource
    private TerminalService terminalService;
    @Resource
    private MerchantOrderRepository merchantOrderRepository;

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
        merchantOrderRepository.save(merchantOrder);

        String orderId = generateUniqueIdOrder(merchantOrder);
        String paymentUrl = buildPaymentUrl(merchantOrder, orderId);

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
        MerchantOrder merchantOrder = findByOrderId(merchantOrderStatusRequest.getOrderId());
        if ( null == merchantOrder ) {
            MerchantOrderStatusResponse merchantOrderStatusResponse = new MerchantOrderStatusResponse();
            merchantOrderStatusResponse.setStatus(ResponseStatus.FAIL);
            merchantOrderStatusResponse.setMessage("Order with id: " + merchantOrderStatusRequest.getOrderId() + " not found");
            return merchantOrderStatusResponse;
        }
        if (StringUtils.isNotBlank(merchantOrder.getExternalId()) ) {
            //todo: делать запрос к реальной системе взависимости от данного параметра
        }
        return null;
    }

    private MerchantOrder findByOrderId(String orderId) {
        try {
            Long id = Long.valueOf(orderId);
            return merchantOrderRepository.findOne(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private String buildPaymentUrl(MerchantOrder merchantOrder, String orderId) {
        return MerchantOrderController.PAYMENT_PATH + "/" + orderId;
    }

    private String generateUniqueIdOrder(MerchantOrder merchantOrder) {
        return String.valueOf(merchantOrder.getId());
    }
}
