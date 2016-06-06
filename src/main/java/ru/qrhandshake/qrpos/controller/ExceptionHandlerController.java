package ru.qrhandshake.qrpos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.dto.MerchantResponse;
import ru.qrhandshake.qrpos.dto.ResponseCode;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.IllegalOrderStatusException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;

/**
 * Created by lameroot on 24.05.16.
 */
@ControllerAdvice
public class ExceptionHandlerController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @org.springframework.web.bind.annotation.ExceptionHandler(value = BindException.class)
    public @ResponseBody String bindException(BindException e) {
        logger.error(e.getMessage());
        for (FieldError fieldError : e.getFieldErrors()) {
            logger.debug(fieldError.getField() + ":" + fieldError.getDefaultMessage());
        }
        return e.getMessage();
    }

    @ExceptionHandler(value = AuthException.class)
    @ResponseBody
    public ApiResponse authException(AuthException e) {
        logger.error("Auth error",e);
        return new ApiResponse.ErrorApiResponse(ResponseStatus.FAIL,e.getMessage());//todo:locale
    }

    @ExceptionHandler(value = MerchantOrderNotFoundException.class)
    @ResponseBody
    public ApiResponse merchantOrderNotFoundException(MerchantOrderNotFoundException e) {
        logger.error("Order not found",e);
        return new ApiResponse.ErrorApiResponse(ResponseStatus.FAIL, e.getMessage());//todo: locale
    }

    @ExceptionHandler(value = IllegalOrderStatusException.class)
    public String illegalOrderStatus(IllegalOrderStatusException e) {
        logger.error("Illegal status:",e);
        switch (e.getIllegalOrderStatus() ) {

        }
        return "";//todo: redirect to page
    }

    @ExceptionHandler(value = Throwable.class)
    public @ResponseBody String error(Throwable e) {
        logger.error("Error",e);
        return "Error";
    }

}
