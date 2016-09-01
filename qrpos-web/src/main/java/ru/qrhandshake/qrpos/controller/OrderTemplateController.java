package ru.qrhandshake.qrpos.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.OrderTemplateHistoryService;
import ru.qrhandshake.qrpos.service.OrderTemplateService;
import ru.qrhandshake.qrpos.service.TerminalService;
import ru.qrhandshake.qrpos.util.MaskUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.text.SimpleDateFormat;
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

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyyMMddHHmmss"),true));
    }

    @Deprecated
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public OrderTemplateResponse create(Principal principal, @Valid OrderTemplateRequest orderTemplateRequest) throws AuthException {
        if ( null == principal ) throw new AuthException("Unknown principal");

        OrderTemplateParams orderTemplateParams = conversionService.convert(orderTemplateRequest, OrderTemplateParams.class);
        OrderTemplateResult orderTemplateResult = orderTemplateService.create(orderTemplateParams);
        return conversionService.convert(orderTemplateResult, OrderTemplateResponse.class);
    }

    @RequestMapping(value = "/payment", params = {"paymentWay=bindingByOrderTemplate"})
    @ResponseBody
    public BindingPaymentByOrderTemplateResponse paymentOrderByTemplate(Principal principal,
                                                                        @Valid BindingPaymentByOrderTemplateRequest bindingPaymentByOrderTemplateRequest,
                                                                        HttpServletRequest request,
                                                                        @RequestHeader(value = "User-Agent", required = false) String userAgent) throws AuthException {
        Client client = authService.clientAuth(principal, bindingPaymentByOrderTemplateRequest, true);
        BindingPaymentByOrderTemplateParams bindingPaymentByOrderTemplateParams = conversionService.convert(bindingPaymentByOrderTemplateRequest,BindingPaymentByOrderTemplateParams.class);
        if (StringUtils.isBlank(bindingPaymentByOrderTemplateParams.getDeviceModel())) bindingPaymentByOrderTemplateParams.setDeviceModel(userAgent);

        //todo: move to validator
        if ( null == bindingPaymentByOrderTemplateParams.getOrderTemplate() ) throw new AuthException("Invalid orderTemplateId");
        bindingPaymentByOrderTemplateParams.setReturnUrl(getReturnUrl(request,String.valueOf(bindingPaymentByOrderTemplateParams.getOrderTemplate().getId())));

        BindingPaymentByOrderTemplateResult bindingPaymentByOrderTemplateResult = orderTemplateService.paymentOrderByTemplate(client, bindingPaymentByOrderTemplateParams);
        return conversionService.convert(bindingPaymentByOrderTemplateResult, BindingPaymentByOrderTemplateResponse.class);
    }

    @RequestMapping(value = "/get_orders")
    @ResponseBody
    public OrderTemplateHistoryResponse getOrders(@Valid OrderTemplateHistoryRequest orderTemplateHistoryRequest) throws AuthException {
        authService.terminalAuth(null, orderTemplateHistoryRequest);//todo авторизацию тут и везде вынесте из методов в обработчики на спринге

        OrderTemplateHistoryParams orderTemplateHistoryParams = conversionService.convert(orderTemplateHistoryRequest,OrderTemplateHistoryParams.class);
        OrderTemplateHistoryResult orderTemplateHistoryResult = orderTemplateHistoryService.getLastSuccessFromDate(orderTemplateHistoryParams);
        return conversionService.convert(orderTemplateHistoryResult, OrderTemplateHistoryResponse.class);
    }

    @RequestMapping(value = "/get_orders_from")
    @ResponseBody
    public OrderTemplateHistoryResponse getOrdersFrom(@Valid OrderTemplateHistoryRequest orderTemplateHistoryRequest, Pageable pageable) throws AuthException {
        authService.terminalAuth(null, orderTemplateHistoryRequest);//todo авторизацию тут и везде вынесте из методов в обработчики на спринге

        OrderTemplateHistoryParams orderTemplateHistoryParams = conversionService.convert(orderTemplateHistoryRequest,OrderTemplateHistoryParams.class);
        OrderTemplateHistoryResult orderTemplateHistoryResult = orderTemplateHistoryService.getFromId(orderTemplateHistoryParams, pageable);
        return conversionService.convert(orderTemplateHistoryResult, OrderTemplateHistoryResponse.class);
    }

    @RequestMapping(value = "/get_orders_until")
    @ResponseBody
    public OrderTemplateHistoryResponse getOrdersUntil(@Valid OrderTemplateHistoryRequest orderTemplateHistoryRequest, Pageable pageable) throws AuthException {
        authService.terminalAuth(null, orderTemplateHistoryRequest);//todo авторизацию тут и везде вынесте из методов в обработчики на спринге

        OrderTemplateHistoryParams orderTemplateHistoryParams = conversionService.convert(orderTemplateHistoryRequest,OrderTemplateHistoryParams.class);
        OrderTemplateHistoryResult orderTemplateHistoryResult = orderTemplateHistoryService.getUntilId(orderTemplateHistoryParams, pageable);
        return conversionService.convert(orderTemplateHistoryResult, OrderTemplateHistoryResponse.class);
    }

    private String getReturnUrl(HttpServletRequest request, String orderTemplateId){
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()
                + "/order_template/finish" + "/" + orderTemplateId;
    }
}
