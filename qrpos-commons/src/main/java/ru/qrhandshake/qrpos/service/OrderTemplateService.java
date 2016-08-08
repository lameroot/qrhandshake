package ru.qrhandshake.qrpos.service;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Terminal;
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

    public OrderTemplateResponse create(OrderTemplateRequest orderTemplateRequest) {
        Terminal terminal = Optional.ofNullable(terminalRepository.findOne(orderTemplateRequest.getTerminalId())).orElseGet(null);
        if ( null == terminal ) {
            OrderTemplateResponse orderTemplateResponse = new OrderTemplateResponse();
            orderTemplateResponse.setStatus(ResponseStatus.FAIL);
            orderTemplateResponse.setMessage("Unknown terminal");
        }
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setAmount(orderTemplateRequest.getAmount());
        orderTemplate.setDescription(orderTemplateRequest.getDescription());
        orderTemplate.setName(orderTemplateRequest.getName());
        orderTemplate.setTerminal(terminal);

        orderTemplateRepository.save(orderTemplate);

        OrderTemplateResponse orderTemplateResponse = new OrderTemplateResponse();
        orderTemplateResponse.setId(orderTemplate.getId());
        orderTemplateResponse.setStatus(ResponseStatus.SUCCESS);

        return orderTemplateResponse;
    }

    public OrderTemplate findById(Long id) {
        return orderTemplateRepository.findOne(id);
    }

    public MerchantOrderRegisterResponse createOrderByTemplate(Client client, BindingPaymentByOrderTemplateRequest bindingPaymentByOrderTemplateRequest) throws Exception {
        OrderTemplate orderTemplate = orderTemplateRepository.findOne(Long.valueOf(bindingPaymentByOrderTemplateRequest.getOrderTemplateId()));
        //todo: check exists
        Terminal terminal = orderTemplate.getTerminal();
        MerchantOrderRegisterRequest merchantOrderRegisterRequest = new MerchantOrderRegisterRequest();
        merchantOrderRegisterRequest.setAmount(orderTemplate.getAmount());
        merchantOrderRegisterRequest.setDescription(orderTemplate.getDescription());
        merchantOrderRegisterRequest.setDeviceId(bindingPaymentByOrderTemplateRequest.getDeviceId());
        merchantOrderRegisterRequest.setSessionId(bindingPaymentByOrderTemplateRequest.getSessionId());

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = orderService.register(terminal, merchantOrderRegisterRequest, null);
        BindingPaymentParams bindingPaymentParams = new BindingPaymentParams();
        //todo: fill param
        paymentByBinding(client, orderTemplate, bindingPaymentParams);

        return null;
    }

    public void paymentByBinding(Client client, OrderTemplate orderTemplate, BindingPaymentParams bindingPaymentParams) throws Exception {


        orderService.payment(client, bindingPaymentParams);
    }

}
