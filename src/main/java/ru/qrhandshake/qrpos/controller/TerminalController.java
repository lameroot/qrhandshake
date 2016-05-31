package ru.qrhandshake.qrpos.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.service.TerminalService;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

/**
 * Created by lameroot on 24.05.16.
 */
@Controller
@RequestMapping(value = TerminalController.TERMINAL_PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
public class TerminalController {

    public final static String TERMINAL_PATH = "/terminal";
    public final static String REGISTER_PATH = "/register";

    @Resource
    private TerminalService terminalService;

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse auth(@Valid ApiAuth apiAuth) {
        return Optional.ofNullable(terminalService.auth(apiAuth)).isPresent()
                ? new ApiResponse(ResponseStatus.SUCCESS,"Auth success")
                : new ApiResponse(ResponseStatus.FAIL,"Auth fail");
    }

    @RequestMapping(value = "/register")
    @ResponseBody
    public TerminalRegisterResponse register(Principal principal, TerminalRegisterRequest terminalRegisterRequest) {
        User user = (User)((Authentication) principal).getPrincipal();
        if ( !user.canCreateTerminal() ) {//todo: перенести этот код в сервис создания
            TerminalRegisterResponse terminalRegisterResponse = new TerminalRegisterResponse();
            terminalRegisterResponse.setStatus(ResponseStatus.FAIL);
            terminalRegisterResponse.setMessage("User: " + user.getUsername() + " can't create terminal");
            return terminalRegisterResponse;
        }
        return terminalService.create(user.getMerchant(), terminalRegisterRequest);
    }



}
