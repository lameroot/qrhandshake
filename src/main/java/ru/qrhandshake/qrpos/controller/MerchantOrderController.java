package ru.qrhandshake.qrpos.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.dto.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.IllegalOrderStatusException;
import ru.qrhandshake.qrpos.exception.IntegrationException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.service.MerchantOrderService;
import ru.qrhandshake.qrpos.service.OrderService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by lameroot on 18.05.16.
 */
@Controller
@RequestMapping(value = MerchantOrderController.MERCHANT_ORDER_PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
public class MerchantOrderController {

    public final static String MERCHANT_ORDER_PATH = "/order";
    public final static String REGISTER_PATH = "/register";
    public final static String ORDER_STATUS_PATH = "/status";
    public final static String PAYMENT_PATH = "/payment";
    public final static String QR_PATH = "/qr"; //static files host in nginx
    @Resource
    private MerchantOrderService merchantOrderService;
    @Resource
    private IntegrationService integrationService;
    @Resource
    private OrderService orderService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = REGISTER_PATH)
    @ResponseBody
    public MerchantOrderRegisterResponse register(@Valid MerchantOrderRegisterRequest merchantOrderRegisterRequest,
                                     HttpServletRequest request) throws AuthException {
        return merchantOrderService.register(merchantOrderRegisterRequest);
    }

    @RequestMapping(value = ORDER_STATUS_PATH)
    @ResponseBody
    public MerchantOrderStatusResponse getOrderStatus(@Valid MerchantOrderStatusRequest merchantOrderStatusRequest) throws MerchantOrderNotFoundException, AuthException, IllegalOrderStatusException {
        return merchantOrderService.getOrderStatus(merchantOrderStatusRequest);

    }

    @RequestMapping(value = PAYMENT_PATH + "/{id}", method = RequestMethod.GET)
    public String paymentPage(@PathVariable("id") String id, Model model) throws MerchantOrderNotFoundException {
        MerchantOrder merchantOrder = merchantOrderService.findMerchantOrderByGeneratedId(id);
        MerchantOrderDto merchantOrderDto = new MerchantOrderDto(merchantOrder);
        model.addAttribute("merchantOrder", merchantOrderDto);
        return "payment";//return payment page as jsp
    }

    @RequestMapping(value = PAYMENT_PATH, method = RequestMethod.POST)
    public String payment(@Valid PaymentRequest paymentRequest,
                          HttpServletRequest request,
                          Model model) throws MerchantOrderNotFoundException, IntegrationException, IllegalOrderStatusException {
        PaymentResponse paymentResponse = orderService.payment(paymentRequest);

        IntegrationPaymentRequest integrationPaymentRequest = merchantOrderService.toIntegrationPaymentRequest(request.getContextPath(), paymentRequest);
        IntegrationPaymentResponse integrationPaymentResponse = integrationService.payment(integrationPaymentRequest);
        merchantOrderService.toMerchantOrder(integrationPaymentResponse);

        if (StringUtils.isNotBlank(integrationPaymentResponse.getAcsUrl()) ) {
            //todo: return acs_page.jsp
            //todo: set term and params to acs page
            return "acs";
        }
        return integrationPaymentResponse.getTermUrl();
    }



}
