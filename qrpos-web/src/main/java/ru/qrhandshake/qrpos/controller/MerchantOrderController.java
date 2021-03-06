package ru.qrhandshake.qrpos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentParams;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentRequest;
import ru.qrhandshake.qrpos.api.merchantorder.*;
import ru.qrhandshake.qrpos.converter.PaymentWayConverter;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.MerchantService;
import ru.qrhandshake.qrpos.service.OrderService;
import ru.qrhandshake.qrpos.service.TerminalService;

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
    public final static String REGISTER_FOR_BINDING_PATH = "/register_for_binding";
    public final static String REGISTER_BY_TEMPLATE_PATH = "/registerByTemplate";
    public final static String ORDER_STATUS_PATH = "/status";
    public final static String SESSION_STATUS_PATH = "/sessionStatus";
    public final static String PAYMENT_PATH = "/payment";
    public final static String FINISH_PATH = "/finish";
    public final static String REVERSE_PATH = "/reverse";

    @Resource
    private OrderService orderService;
    @Resource
    private AuthService authService;
    @Resource
    private MerchantService merchantService;
    @Resource
    private TerminalService terminalService;

    @Value("${buildHttpsRequest:false}")
    private boolean buildHttpsRequest;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(PaymentWay.class, new PaymentWayConverter());
    }

    @RequestMapping(value = REGISTER_BY_TEMPLATE_PATH)
    @ResponseBody
    public MerchantOrderRegisterResponse registerByTemplate(Principal principal, MerchantOrderRegisterByTemplateRequest request)
            throws AuthException {
        authService.clientAuth(principal, request, true);

        String paymentPath = MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH;
        return orderService.registerByTemplate(request, paymentPath);
    }

    @RequestMapping(value = REGISTER_PATH)
    @ResponseBody
    public MerchantOrderRegisterResponse register(Principal principal,@Valid MerchantOrderRegisterRequest merchantOrderRegisterRequest)
            throws AuthException {
        String paymentPath = MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH;
        return orderService.register(authService.terminalAuth(principal, merchantOrderRegisterRequest), merchantOrderRegisterRequest, paymentPath);
    }

    @RequestMapping(value = REGISTER_FOR_BINDING_PATH)
    @ResponseBody
    public MerchantOrderRegisterResponse registerForBindingByClient(Principal principal, @Valid MerchantOrderRegisterRequest merchantOrderRegisterRequest)
        throws AuthException {
        String paymentPath = MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH;
        authService.clientAuth(principal,merchantOrderRegisterRequest,true);
        Merchant merchant = merchantService.findRootMerchant();
        if ( null == merchant ) throw new AuthException("Unknown root merchant");
        Terminal terminal = terminalService.findByMerchant(merchant).stream().filter(t -> t.isEnabled()).findFirst().orElseThrow(() -> new AuthException("Not found parent terminal"));
        return orderService.register(terminal, merchantOrderRegisterRequest, paymentPath);
    }

    @RequestMapping(value = ORDER_STATUS_PATH)
    @ResponseBody
    public MerchantOrderStatusResponse getOrderStatus(Principal principal,
            @Valid MerchantOrderStatusRequest merchantOrderStatusRequest) throws AuthException {
        return orderService.getOrderStatus(authService.terminalAuth(principal, merchantOrderStatusRequest), merchantOrderStatusRequest);
    }

    @RequestMapping(value = PAYMENT_PATH + "/{orderId}", method = RequestMethod.GET)
    public String paymentPage(@PathVariable(value = "orderId") String orderId) {
        return "redirect:/payment.html?orderId=" + orderId;
    }

    @RequestMapping(value = SESSION_STATUS_PATH)
    @ResponseBody
    public SessionStatusResponse getSessionStatus(Principal principal, @Valid SessionStatusRequest sessionStatusRequest) throws MerchantOrderNotFoundException, AuthException {
        authService.clientAuth(principal, sessionStatusRequest, true);
        MerchantOrder merchantOrder = orderService.findByOrderId(sessionStatusRequest.getOrderId());
        if ( null == merchantOrder ) throw new MerchantOrderNotFoundException("Order: " + sessionStatusRequest.getOrderId() + " not found");
        SessionStatusResponse sessionStatusResponse = new SessionStatusResponse(sessionStatusRequest.getOrderId());
        sessionStatusResponse.setAmount(merchantOrder.getAmount());
        sessionStatusResponse.setOrderStatus(merchantOrder.getOrderStatus());
        sessionStatusResponse.setCanPayment(merchantOrder.canPayment());
        sessionStatusResponse.setStatus(ResponseStatus.SUCCESS);
        sessionStatusResponse.setDescription(merchantOrder.getDescription());
        sessionStatusResponse.setMessage("Session status");

        return sessionStatusResponse;
    }

    @RequestMapping(value = PAYMENT_PATH, method = RequestMethod.POST, params = {"paymentWay=card"})
    @ResponseBody
    public PaymentResponse cardPayment(Principal principal, @Valid CardPaymentRequest paymentRequest,
                              HttpServletRequest request) throws AuthException {
        Client client = authService.clientAuth(principal, paymentRequest, false);

        //TODO move to converter
        CardPaymentParams paymentParams = new CardPaymentParams();
        paymentParams.setOrderId(paymentRequest.getOrderId());
        paymentParams.setPan(paymentRequest.getPan());
        paymentParams.setMonth(paymentRequest.getMonth());
        paymentParams.setYear(paymentRequest.getYear());
        paymentParams.setCvc(paymentRequest.getCvc());
        paymentParams.setCardHolderName(paymentRequest.getCardHolderName());
        paymentParams.setIp(request.getRemoteUser());
        paymentParams.setReturnUrl(getReturnUrl(request, paymentRequest.getOrderId()));

        return orderService.payment(client, paymentParams);
    }

    @RequestMapping(value = PAYMENT_PATH, method = RequestMethod.POST, params = {"paymentWay=binding"})
    @ResponseBody
    public PaymentResponse bindingPayment(Principal principal, @Valid BindingPaymentRequest paymentRequest,
                                 HttpServletRequest request) throws AuthException {
        Client client = authService.clientAuth(principal, paymentRequest, false);

        //TODO move to converter
        BindingPaymentParams paymentParams = new BindingPaymentParams();
        paymentParams.setOrderId(paymentRequest.getOrderId());
        paymentParams.setBindingId(paymentRequest.getBindingId());
        paymentParams.setConfirmValue(paymentRequest.getConfirmValue());
        paymentParams.setIp(request.getRemoteUser());
        paymentParams.setReturnUrl(getReturnUrl(request, paymentRequest.getOrderId()));

        return orderService.payment(client, paymentParams);
    }


    @RequestMapping(value = FINISH_PATH + "/{orderId}")
    public String finish(@PathVariable(value = "orderId") String orderId, Model model) {
        //todo: когда клиент приходит н-р с ацс, то виден внешний идентификатор в запросе клиенту, может стоит его убрать
        FinishRequest finishRequest = new FinishRequest(orderId);
        FinishResponse finishResponse = orderService.finish(finishRequest);
        model.addAttribute("orderStatus",finishResponse.getOrderStatus());
        model.addAttribute("orderId",finishResponse.getOrderId());
        model.addAttribute("status",finishResponse.getStatus());
        model.addAttribute("message",finishResponse.getMessage());
        return "finish";
    }

    @RequestMapping(value = REVERSE_PATH, method = RequestMethod.POST)
    @ResponseBody
    public MerchantOrderReverseResponse reverse(Principal principal, @Valid MerchantOrderReverseRequest merchantOrderReverseRequest) throws AuthException {
        return orderService.reverse(authService.terminalAuth(principal, merchantOrderReverseRequest), merchantOrderReverseRequest);
    }

    private String getReturnUrl(HttpServletRequest request, String orderId){
        logger.debug("request schema: {}", request.getScheme());
        if ( buildHttpsRequest ) {
            return "https" + "://" + request.getServerName() + request.getContextPath()
                    + MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.FINISH_PATH + "/" + orderId;
        }
        else {
            return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()
                    + MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.FINISH_PATH + "/" + orderId;
        }
    }
}
