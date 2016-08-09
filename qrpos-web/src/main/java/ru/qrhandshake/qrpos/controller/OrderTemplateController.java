package ru.qrhandshake.qrpos.controller;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.OrderTemplateService;
import ru.qrhandshake.qrpos.service.TerminalService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Set;

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
    @Resource
    private ConversionService conversionService;
    @Resource
    private TerminalService terminalService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public OrderTemplateResponse create(Principal principal, @Valid OrderTemplateRequest orderTemplateRequest) throws AuthException {
        if ( null == principal ) throw new AuthException("Unknown principal");

        OrderTemplateParams orderTemplateParams = conversionService.convert(orderTemplateRequest, OrderTemplateParams.class);

        //todo: move to validation
        Terminal terminal = orderTemplateParams.getTerminal();
        if ( null == terminal ) throw new AuthException("Unknown terminal id: " + orderTemplateRequest.getTerminalId());
        if ( !(((Authentication) principal).getPrincipal() instanceof User) ) throw new AuthException("Invalid principal");
        User user = (User)((Authentication) principal).getPrincipal();
        Set<Terminal> terminals = terminalService.findByMerchant(user.getMerchant());
        if ( !terminals.contains(terminal) ) throw new AuthException("Unknown terminal id: " + orderTemplateRequest.getTerminalId() + " for own merchant");

        OrderTemplateResult orderTemplateResult = orderTemplateService.create(orderTemplateParams);
        return conversionService.convert(orderTemplateResult, OrderTemplateResponse.class);
    }

    @RequestMapping(value = "/payment", params = {"paymentWay=bindingByOrderTemplate"})
    @ResponseBody
    public BindingPaymentByOrderTemplateResponse paymentOrderByTemplate(Principal principal,
                                                                        @Valid BindingPaymentByOrderTemplateRequest bindingPaymentByOrderTemplateRequest,
                                                                        HttpServletRequest request) throws AuthException {
        Client client = authService.clientAuth(principal, bindingPaymentByOrderTemplateRequest, true);
        BindingPaymentByOrderTemplateParams bindingPaymentByOrderTemplateParams = conversionService.convert(bindingPaymentByOrderTemplateRequest,BindingPaymentByOrderTemplateParams.class);

        //todo: move to validator
        if ( null == bindingPaymentByOrderTemplateParams.getOrderTemplate() ) throw new AuthException("Invalid orderTemplateId");
        bindingPaymentByOrderTemplateParams.setReturnUrl(getReturnUrl(request,String.valueOf(bindingPaymentByOrderTemplateParams.getOrderTemplate().getId())));

        BindingPaymentByOrderTemplateResult bindingPaymentByOrderTemplateResult = orderTemplateService.paymentOrderByTemplate(client, bindingPaymentByOrderTemplateParams);
        return conversionService.convert(bindingPaymentByOrderTemplateResult, BindingPaymentByOrderTemplateResponse.class);
    }

    private String getReturnUrl(HttpServletRequest request, String orderTemplateId){
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()
                + "/order_template/finish" + "/" + orderTemplateId;
    }
}
