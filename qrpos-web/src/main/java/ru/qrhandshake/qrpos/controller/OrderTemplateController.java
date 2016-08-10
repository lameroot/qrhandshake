package ru.qrhandshake.qrpos.controller;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.OrderTemplateHistoryService;
import ru.qrhandshake.qrpos.service.OrderTemplateService;
import ru.qrhandshake.qrpos.service.TerminalService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Date;
import java.util.List;
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
    private OrderTemplateHistoryService orderTemplateHistoryService;
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
                                                                        HttpServletRequest request,
                                                                        @RequestHeader("User-Agent") String userAgent) throws AuthException {
        Client client = authService.clientAuth(principal, bindingPaymentByOrderTemplateRequest, true);
        BindingPaymentByOrderTemplateParams bindingPaymentByOrderTemplateParams = conversionService.convert(bindingPaymentByOrderTemplateRequest,BindingPaymentByOrderTemplateParams.class);
        bindingPaymentByOrderTemplateParams.setUserAgent(userAgent);

        //todo: move to validator
        if ( null == bindingPaymentByOrderTemplateParams.getOrderTemplate() ) throw new AuthException("Invalid orderTemplateId");
        bindingPaymentByOrderTemplateParams.setReturnUrl(getReturnUrl(request,String.valueOf(bindingPaymentByOrderTemplateParams.getOrderTemplate().getId())));

        BindingPaymentByOrderTemplateResult bindingPaymentByOrderTemplateResult = orderTemplateService.paymentOrderByTemplate(client, bindingPaymentByOrderTemplateParams);
        return conversionService.convert(bindingPaymentByOrderTemplateResult, BindingPaymentByOrderTemplateResponse.class);
    }

    @RequestMapping(value = "/get_orders")
    @ResponseBody
    public OrderTemplateHistoryResponse getOrders(@Valid OrderTemplateHistoryRequest orderTemplateHistoryRequest) {
        //todo  добавить авторизацию по терминалу
        OrderTemplateHistoryResponse orderTemplateHistoryResponse = new OrderTemplateHistoryResponse();
        //todo переделать на стримы
        List<OrderTemplateHistory> orderTemplateHistories = orderTemplateHistoryService.getLastSuccessFromDate(orderTemplateHistoryRequest.getFrom(), orderTemplateHistoryRequest.getOrderTemplateId());
        for (OrderTemplateHistory orderTemplateHistory : orderTemplateHistories) {
            orderTemplateHistoryResponse.getOrderNumbers().add(orderTemplateHistory.getHumanOrderNumber());
        }
        return orderTemplateHistoryResponse;
    }

    private String getReturnUrl(HttpServletRequest request, String orderTemplateId){
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()
                + "/order_template/finish" + "/" + orderTemplateId;
    }
}
