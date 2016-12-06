package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentByOrderTemplateParams;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentByOrderTemplateResult;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentParams;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterRequest;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateParams;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateResult;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.repository.OrderTemplateRepository;
import ru.qrhandshake.qrpos.repository.TerminalRepository;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by lameroot on 08.08.16.
 */
@Service
public class OrderTemplateService {

    @Resource
    private OrderTemplateRepository orderTemplateRepository;
    @Resource
    private MerchantRepository merchantRepository;
    @Resource
    private OrderService orderService;
    @Resource
    private TerminalRepository terminalRepository;
    @Resource
    private ConversionService conversionService;
    @Resource
    private OrderTemplateHistoryService orderTemplateHistoryService;


    public OrderTemplate create(OrderTemplateParams orderTemplateParams) throws AuthException {
        Terminal terminal = terminalRepository.findOne(orderTemplateParams.getTerminalId());
        if ( null == terminal ) throw new AuthException("Invalid terminalId: " + orderTemplateParams.getTerminalId());
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setAmount(orderTemplateParams.getAmount());
        orderTemplate.setDescription(orderTemplateParams.getDescription());
        orderTemplate.setName(orderTemplateParams.getName());
        orderTemplate.setTerminal(terminal);

        return orderTemplateRepository.save(orderTemplate);
    }

    public OrderTemplate findById(Long id) {
        return orderTemplateRepository.findOne(id);
    }

    public BindingPaymentByOrderTemplateResult paymentOrderByTemplate(Client client, BindingPaymentByOrderTemplateParams bindingPaymentByOrderTemplateParams) throws AuthException {
        OrderTemplate orderTemplate = bindingPaymentByOrderTemplateParams.getOrderTemplate();

        Terminal terminal = orderTemplate.getTerminal();
        MerchantOrderRegisterRequest merchantOrderRegisterRequest = new MerchantOrderRegisterRequest();
        merchantOrderRegisterRequest.setAmount(orderTemplate.getAmount());
        merchantOrderRegisterRequest.setDescription(orderTemplate.getDescription());
        merchantOrderRegisterRequest.setDeviceId(bindingPaymentByOrderTemplateParams.getDeviceId());
        merchantOrderRegisterRequest.setSessionId(bindingPaymentByOrderTemplateParams.getSessionId());

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = orderService.register(terminal, merchantOrderRegisterRequest, null);
        BindingPaymentParams bindingPaymentParams = new BindingPaymentParams();
        bindingPaymentParams.setConfirmValue(null);
        bindingPaymentParams.setBindingId(bindingPaymentByOrderTemplateParams.getBindingId());
        bindingPaymentParams.setOrderId(merchantOrderRegisterResponse.getOrderId());
        bindingPaymentParams.setReturnUrl(bindingPaymentByOrderTemplateParams.getReturnUrl());

        PaymentResponse paymentResponse = orderService.payment(client, bindingPaymentParams);

        OrderTemplateHistory orderTemplateHistory = new OrderTemplateHistory();
        orderTemplateHistory.setOrderTemplateId(orderTemplate.getId());
        orderTemplateHistory.setMerchantOrderId(merchantOrderRegisterResponse.getId());
        orderTemplateHistory.setDeviceId(bindingPaymentByOrderTemplateParams.getDeviceId());
        orderTemplateHistory.setDeviceModel(bindingPaymentByOrderTemplateParams.getDeviceModel());
        orderTemplateHistory.setClientId(client.getId());
        orderTemplateHistory.setDate(new Date());
        orderTemplateHistory.setDeviceMobileNumber(null != client && StringUtils.isNotBlank(client.getPhone()) ? client.getPhone() : bindingPaymentByOrderTemplateParams.getDeviceMobileNumber());
        //todo: генерация номера заказа должна задавать в шаблоне, то есть н-р номер маршрута + номер заказа, отдельная стратегия
        orderTemplateHistory.setHumanOrderNumber(orderTemplateHistoryService.generateHumanOrderNumber(merchantOrderRegisterResponse.getId()));
        orderTemplateHistory.setStatus(paymentResponse.getStatus() == ResponseStatus.SUCCESS ? true : false);
        orderTemplateHistoryService.save(orderTemplateHistory);

        BindingPaymentByOrderTemplateResult bindingPaymentByOrderTemplateResult = new BindingPaymentByOrderTemplateResult();
        bindingPaymentByOrderTemplateResult.setOrderId(paymentResponse.getOrderId());
        bindingPaymentByOrderTemplateResult.setHumanOrderNumber(orderTemplateHistory.getHumanOrderNumber());
        bindingPaymentByOrderTemplateResult.setStatus(orderTemplateHistory.getStatus());

        return bindingPaymentByOrderTemplateResult;
    }


}
