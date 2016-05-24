package ru.qrhandshake.qrpos.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.dto.AuthRequest;
import ru.qrhandshake.qrpos.dto.TerminalRegisterRequest;
import ru.qrhandshake.qrpos.dto.TerminalRegisterResponse;

import javax.validation.Valid;

/**
 * Created by lameroot on 24.05.16.
 */
@Controller
@RequestMapping(value = TerminalController.TERMINAL_PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
public class TerminalController {

    public final static String TERMINAL_PATH = "/terminal";
    public final static String REGISTER_PATH = "/register";



    @RequestMapping(value = REGISTER_PATH)
    @ResponseBody
    public TerminalRegisterResponse register(@Valid AuthRequest authRequest,
                                             @RequestParam(name = "merchant_id") String merchantId) {
        return null;
    }

}
