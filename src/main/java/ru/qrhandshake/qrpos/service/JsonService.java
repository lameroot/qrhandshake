package ru.qrhandshake.qrpos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by lameroot on 31.05.16.
 */
@Service
public class JsonService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ObjectMapper objectMapper;

    public String paymentParamsToJsonString(PaymentParams paymentParams) {
        if ( null == paymentParams ) return null;
        if ( paymentParams instanceof CardPaymentParams) {
            CardPaymentParams cardPaymentParams = (CardPaymentParams)paymentParams;
            try {
                return objectMapper.writeValueAsString(cardPaymentParams);
            } catch (JsonProcessingException e) {
                logger.error("Error cardPaymentParams to string",e);
                return null;
            }
        }
        logger.warn("Unknown paymentParams type: " + paymentParams.getClass());
        return null;
    }

    public PaymentParams jsonToPaymentParams(String str) {
        try {
            return objectMapper.readValue(str, PaymentParams.class);
        } catch (IOException e) {
            logger.error("Error read str: " + str,e);
            return null;
        }
    }
}
