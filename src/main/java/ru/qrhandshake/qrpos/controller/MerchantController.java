package ru.qrhandshake.qrpos.controller;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.qrhandshake.qrpos.api.MerchantRegisterRequest;
import ru.qrhandshake.qrpos.api.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.service.MerchantService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * Created by lameroot on 24.05.16.
 */
@Controller
@RequestMapping(value = MerchantController.MERCHANT_PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
public class MerchantController {

    public final static String MERCHANT_PATH = "/merchant";
    public final static String REGISTER_PATH = "/register";

    @Resource
    private MerchantService merchantService;

    @RequestMapping(value = REGISTER_PATH)
    @ResponseBody
    public MerchantDto registerMerchant(@Valid MerchantDto merchantRequest) {
        merchantService.create(merchantRequest);
        return merchantRequest;
    }

    @RequestMapping(value = "/is_exists")
    @ResponseBody
    public Boolean isExists(@RequestParam(value = "merchant_id") String merchantId) {
        return merchantService.isExist(merchantId);
    }

    @RequestMapping(value = "/register")
    @ResponseBody
    public MerchantRegisterResponse register(@Valid MerchantRegisterRequest merchantRegisterRequest) {
        return merchantService.register(merchantRegisterRequest);
    }
}
