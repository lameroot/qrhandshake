package ru.qrhandshake.qrpos.controller;

import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterParams;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterRequest;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResponse;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResult;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.AuthService;
import ru.qrhandshake.qrpos.service.EndpointService;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * Created by lameroot on 24.09.16.
 */
@Controller
@RequestMapping(value = "/endpoint")
public class EndpointController {

    @Resource
    private ConversionService conversionService;
    @Resource
    private AuthService authService;
    @Resource
    private EndpointService endpointService;

    @RequestMapping(value = "/register")
    @ResponseBody
    public EndpointRegisterResponse register(Principal principal, EndpointRegisterRequest endpointRegisterRequest) throws AuthException {
        authService.terminalAuth(principal, endpointRegisterRequest);
        EndpointRegisterParams endpointRegisterParams = conversionService.convert(endpointRegisterRequest,EndpointRegisterParams.class);
        EndpointRegisterResult endpointRegisterResult = endpointService.register(endpointRegisterParams);
        return conversionService.convert(endpointRegisterResult, EndpointRegisterResponse.class);
    }
}
