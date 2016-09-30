package ru.qrhandshake.qrpos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentParams;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.api.YandexMoneyPaymentParams;
import ru.qrhandshake.qrpos.domain.PaymentWay;

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

    public PaymentParams jsonToPaymentParams(String str, PaymentWay paymentWay) {
        Class<? extends PaymentParams> clazz = defineClassOfPaymentParamsByPaymentWay(paymentWay);
        try {
            return objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            logger.error("Error read str: " + str,e);
            return null;
        }
    }

    public <P extends PaymentParams> P jsonToPaymentParams(String str, Class<P> clazz) {
        try {
            return objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            logger.error("Error read str: " + str,e);
            return null;
        }
    }

    private Class<? extends PaymentParams> defineClassOfPaymentParamsByPaymentWay(PaymentWay paymentWay) {
        if ( null == paymentWay ) throw new IllegalArgumentException("PaymentWay is null");
        switch (paymentWay) {
            case CARD: return CardPaymentParams.class;
            case BINDING: return BindingPaymentParams.class;
            case YANDEX_WALLET: return YandexMoneyPaymentParams.class;

        }
        throw new IllegalArgumentException("Unknown payment way: " + paymentWay);
    }
}
