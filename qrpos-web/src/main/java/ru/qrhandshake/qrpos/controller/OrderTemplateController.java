package ru.qrhandshake.qrpos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.BindingPaymentByOrderTemplateRequest;
import ru.qrhandshake.qrpos.api.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.api.OrderTemplateRequest;
import ru.qrhandshake.qrpos.api.OrderTemplateResponse;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.OrderTemplateService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.security.Principal;

/**
 * Created by lameroot on 08.08.16.
 */
@Controller
@RequestMapping(value = "/order_template")
public class OrderTemplateController {

    @Resource
    private AuthService authService;
    @Resource
    private OrderTemplateService orderTemplateService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public OrderTemplateResponse create(@Valid OrderTemplateRequest orderTemplateRequest) {
        return orderTemplateService.create(orderTemplateRequest);
    }

    public void paymentByOrderTemplate(Principal principal, @Valid BindingPaymentByOrderTemplateRequest bindingPaymentByOrderTemplateRequest) throws AuthException {
        Client client = authService.clientAuth(principal, bindingPaymentByOrderTemplateRequest, true);
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = orderTemplateService.createOrderByTemplate(client, bindingPaymentByOrderTemplateRequest);
        //todo: check


    }
}
