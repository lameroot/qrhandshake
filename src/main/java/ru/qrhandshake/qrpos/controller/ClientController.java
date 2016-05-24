package ru.qrhandshake.qrpos.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.ClientRegisterRequest;
import ru.qrhandshake.qrpos.api.ClientRegisterResponse;
import ru.qrhandshake.qrpos.service.ClientService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * Created by lameroot on 25.05.16.
 */
@Controller
@RequestMapping(value = "/client", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ClientController {

    @Resource
    private ClientService clientService;

    @RequestMapping(value = "/register")
    @ResponseBody
    public ClientRegisterResponse register(@Valid ClientRegisterRequest clientRegisterRequest) {
        return clientService.register(clientRegisterRequest);
    }
}
