package ru.qrhandshake.qrpos.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.service.ClientService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Optional;

/**
 * Created by lameroot on 25.05.16.
 */
@Controller
@RequestMapping(value = "/client", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ClientController {

    @Resource
    private ClientService clientService;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ClientRegisterResponse register(@Valid ClientRegisterRequest clientRegisterRequest) {
        return clientService.register(clientRegisterRequest);
    }

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse auth(@Valid ApiAuth apiAuth) {
        return Optional.ofNullable(clientService.auth(apiAuth)).isPresent()
                ? new ApiResponse(ResponseStatus.SUCCESS,"Auth success")
                : new ApiResponse(ResponseStatus.FAIL,"Auth fail");
    }
}
