package ru.qrhandshake.qrpos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.dto.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.service.OrderService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

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
    public final static String FINISH_PATH = "/finish";
    public final static String REVERSE_PATH = "/reverse";

    @Resource
    private OrderService orderService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = REGISTER_PATH)
    @ResponseBody
    public MerchantOrderRegisterResponse register(@Valid MerchantOrderRegisterRequest merchantOrderRegisterRequest) throws AuthException {
        return orderService.register(merchantOrderRegisterRequest);
    }

    @RequestMapping(value = ORDER_STATUS_PATH)
    @ResponseBody
    public MerchantOrderStatusResponse getOrderStatus(@Valid MerchantOrderStatusRequest merchantOrderStatusRequest) throws AuthException {
        return orderService.getOrderStatus(merchantOrderStatusRequest);
    }

    @RequestMapping(value = PAYMENT_PATH + "/{orderId}", method = RequestMethod.GET)
    public String paymentPage(@PathVariable(value = "orderId") String orderId, Model model) throws MerchantOrderNotFoundException {
        MerchantOrder merchantOrder = orderService.findByOrderId(orderId);
        if ( null == merchantOrder ) throw new MerchantOrderNotFoundException("Order: " + orderId + " not found");

        MerchantOrderDto merchantOrderDto = new MerchantOrderDto(merchantOrder);
        model.addAttribute("merchantOrder", merchantOrderDto);
        return "payment";//return payment page as jsp
    }

    @RequestMapping(value = PAYMENT_PATH, method = RequestMethod.POST, params = {"paymentWay=card"})
    public String cardPayment(Principal principal, @Valid CardPaymentRequest paymentRequest,
                              HttpServletRequest request,
                              Model model) {
        paymentRequest.setIp(request.getRemoteUser());
        return handlePaymentRequest(principal,paymentRequest,request,model);
    }


    @RequestMapping(value = FINISH_PATH + "/{orderId}")
    public String finish(@PathVariable(value = "orderId") String orderId, Model model) {
        FinishRequest finishRequest = new FinishRequest(orderId);
        FinishResponse finishResponse = orderService.finish(finishRequest);
        model.addAttribute("orderStatus",finishResponse.getOrderStatus());
        model.addAttribute("orderId",finishResponse.getOrderId());
        model.addAttribute("status",finishResponse.getStatus());
        return "finish";
    }

    @RequestMapping(value = REVERSE_PATH, method = RequestMethod.POST)
    @ResponseBody
    public MerchantOrderReverseResponse reverse(@Valid MerchantOrderReverseRequest merchantOrderReverseRequest) throws AuthException {
        return orderService.reverse(merchantOrderReverseRequest);
    }

    private String handlePaymentRequest(Principal principal, PaymentRequest paymentRequest, HttpServletRequest request, Model model) {
        Client client = null;
        if ( null != principal ) {
            client = (Client) ((Authentication) principal).getPrincipal();
            paymentRequest.setClient(client);
        }
        paymentRequest.setReturnUrl(request.getScheme() + "://" + request.getServerName() + request.getRequestURI() + FINISH_PATH + "/" + paymentRequest.getOrderId());
        PaymentResponse paymentResponse = orderService.payment(paymentRequest, model);
        if ( ResponseStatus.SUCCESS.equals(paymentResponse.getStatus()) ) {
            logger.debug("Return success payment page: {}", paymentResponse.getRedirectUrlOrPagePath());
            return paymentResponse.getRedirectUrlOrPagePath();
        }
        else {
            logger.error("Error payment of order: {}, cause: {}",paymentRequest.getOrderId(),paymentResponse.getMessage());
            return "redirect:" + PAYMENT_PATH + "/" + paymentRequest.getOrderId();
        }
    }

}
