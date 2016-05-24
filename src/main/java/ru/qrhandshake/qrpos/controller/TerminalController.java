package ru.qrhandshake.qrpos.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.TerminalRegisterRequest;
import ru.qrhandshake.qrpos.api.TerminalRegisterResponse;
import ru.qrhandshake.qrpos.service.TerminalService;

import javax.annotation.Resource;
import javax.validation.Valid;

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

    @RequestMapping(value = "/register")
    @ResponseBody
    public TerminalRegisterResponse register(@Valid TerminalRegisterRequest terminalRegisterRequest) {
        return terminalService.register(terminalRegisterRequest);
    }



}
