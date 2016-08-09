package ru.qrhandshake.qrpos.service;

import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.repository.OrderTemplateRepository;
import ru.qrhandshake.qrpos.repository.TerminalRepository;

import javax.annotation.Resource;
import java.util.Optional;

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

    public OrderTemplateResult create(OrderTemplateParams orderTemplateParams) {
        Terminal terminal = orderTemplateParams.getTerminal();
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setAmount(orderTemplateParams.getAmount());
        orderTemplate.setDescription(orderTemplateParams.getDescription());
        orderTemplate.setName(orderTemplateParams.getName());
        orderTemplate.setTerminal(terminal);

        orderTemplateRepository.save(orderTemplate);

        OrderTemplateResult orderTemplateResult = new OrderTemplateResult();
        orderTemplateResult.setId(orderTemplate.getId());

        return orderTemplateResult;
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

        BindingPaymentByOrderTemplateResult bindingPaymentByOrderTemplateResult = new BindingPaymentByOrderTemplateResult();
        bindingPaymentByOrderTemplateResult.setOrderId(paymentResponse.getOrderId());

        return bindingPaymentByOrderTemplateResult;
    }



}
