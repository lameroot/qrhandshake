package ru.qrhandshake.qrpos.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.binding.*;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.PaymentWay;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.BindingService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.UUID;

/**
 * Created by lameroot on 17.08.16.
 */
@Controller
@RequestMapping(value = BindingController.BINDING_PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
public class BindingController {

    private final static Long MIN_AMOUNT_FOR_CREATE_BINDING = 100L;
    public final static String BINDING_PATH = "/binding";
    public final static String FINISH_PATH = "/finish";
    public final static String GET_BINDINGS_PATH = "/get_bindings";
    public final static String DELETE_PATH = "/delete";

    @Resource
    private BindingService bindingService;
    @Resource
    private AuthService authService;


    @Deprecated
    @RequestMapping(params = {"paymentWay=card"})
    @ResponseBody
    public CardBindingCreateResponse createBinding(Principal principal, @Valid CardBindingCreateRequest cardBindingCreateRequest,
                                                   HttpServletRequest request) throws AuthException {
        Client client = authService.clientAuth(principal, cardBindingCreateRequest, true);
        String orderId = UUID.randomUUID().toString();

        CardPaymentParams paymentParams = new CardPaymentParams();
        paymentParams.setPan(cardBindingCreateRequest.getPan());
        paymentParams.setMonth(cardBindingCreateRequest.getMonth());
        paymentParams.setYear(cardBindingCreateRequest.getYear());
        paymentParams.setCvc(cardBindingCreateRequest.getCvc());
        paymentParams.setCardHolderName(cardBindingCreateRequest.getCardHolderName());
        paymentParams.setIp(request.getRemoteUser());
        paymentParams.setReturnUrl(getReturnUrl(request, orderId));

        PaymentResult paymentResult = bindingService.createBinding(client, MIN_AMOUNT_FOR_CREATE_BINDING, paymentParams, PaymentWay.CARD, orderId);

        //todo: move to converter
        CardBindingCreateResponse cardBindingCreateResponse = new CardBindingCreateResponse();
        cardBindingCreateResponse.setMessage(paymentResult.getMessage());
        cardBindingCreateResponse.setStatus(ResponseStatus.valueOfCode(paymentResult.getCode()));
        cardBindingCreateResponse.setOrderStatus(paymentResult.getOrderStatus());
        cardBindingCreateResponse.setOrderId(paymentResult.getOrderId());
        cardBindingCreateResponse.setReturnUrlObject(paymentResult.getReturnUrlObject());

        return cardBindingCreateResponse;
    }

    @RequestMapping(value = DELETE_PATH)
    @ResponseBody
    public BindingDeleteResponse delete(Principal principal, @Valid BindingDeleteRequest bindingDeleteRequest) throws AuthException {
        Client client = authService.clientAuth(principal, bindingDeleteRequest, true);
        return bindingService.delete(client, bindingDeleteRequest.getBindingId(), bindingDeleteRequest.isReturnNewBindingList());
    }

    @RequestMapping(value = GET_BINDINGS_PATH)
    @ResponseBody
    public GetBindingsResponse getBindings(Principal principal, @Valid GetBindingsRequest getBindingsRequest) throws AuthException {
        return bindingService.getBindings(authService.clientAuth(principal, getBindingsRequest, true), getBindingsRequest);
    }

    @RequestMapping(value = FINISH_PATH + "/{orderId}")
    public String finish(@PathVariable(value = "orderId") String orderId, Model model) {

        FinishParams finishParams = new FinishParams(orderId);

        FinishResult finishResult = bindingService.finish(finishParams);

        return "";//todo: это вроде как делается на стороне приложения
    }

    private String getReturnUrl(HttpServletRequest request, String orderId){
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()
                + BindingController.BINDING_PATH + BindingController.FINISH_PATH + "/" + orderId;
    }

}
