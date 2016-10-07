package ru.qrhandshake.qrpos.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.client.*;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderStatusResponse;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.ClientService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/client", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ClientController {

    @Resource
    private ClientService clientService;
    @Resource
    private AuthService authService;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ClientRegisterResponse register(@Valid ClientRegisterRequest clientRegisterRequest) {
        return clientService.register(clientRegisterRequest);
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    @ResponseBody
    public ClientConfirmResponse confirm(@Valid ClientConfirmRequest clientConfirmRequest) {
        return clientService.confirm(clientConfirmRequest);
    }

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse auth(@Valid ApiAuth apiAuth) {
        return Optional.ofNullable(clientService.auth(apiAuth)).isPresent()
                ? new ApiResponse(ResponseStatus.SUCCESS,"Auth success")
                : new ApiResponse(ResponseStatus.FAIL,"Auth fail");
    }

    @RequestMapping(value = "/get_orders")
    @ResponseBody
    public ClientOrderHistoryResponse getOrders(Principal principal, ClientOrderHistoryRequest clientOrderHistoryRequest, Pageable pageable) throws AuthException {
        Client client = authService.clientAuth(principal, clientOrderHistoryRequest, true);
        Sort sort = new Sort(Sort.Direction.DESC,"paymentDate");
        PageRequest pageRequest = new PageRequest(pageable.getPageNumber(),pageable.getPageSize(),sort);
        List<MerchantOrder> merchantOrders = clientService.getOrders(client, pageRequest);

        ClientOrderHistoryResponse clientOrderHistoryResponse = new ClientOrderHistoryResponse();
        for (MerchantOrder merchantOrder : merchantOrders) {
            MerchantOrderStatusResponse merchantOrderStatusResponse = new MerchantOrderStatusResponse();
            merchantOrderStatusResponse.setAmount(merchantOrder.getAmount());
            merchantOrderStatusResponse.setOrderId(merchantOrder.getOrderId());
            merchantOrderStatusResponse.setOrderStatus(merchantOrder.getOrderStatus());
            merchantOrderStatusResponse.setMessage("Ok");
            merchantOrderStatusResponse.setStatus(ResponseStatus.SUCCESS);
            merchantOrderStatusResponse.setCreatedDate(merchantOrder.getCreatedDate());
            merchantOrderStatusResponse.setPaymentDate(merchantOrder.getPaymentDate());
            merchantOrderStatusResponse.setDescription(merchantOrder.getDescription());

            clientOrderHistoryResponse.getOrders().add(merchantOrderStatusResponse);
        }

        clientOrderHistoryResponse.setMessage("Ok");
        clientOrderHistoryResponse.setStatus(ResponseStatus.SUCCESS);
        return clientOrderHistoryResponse;
    }
}
