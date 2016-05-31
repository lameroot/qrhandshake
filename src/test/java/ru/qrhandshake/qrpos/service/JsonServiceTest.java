package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;

import javax.annotation.Resource;

/**
 * Created by lameroot on 31.05.16.
 */
public class JsonServiceTest extends GeneralTest {

    @Resource
    private JsonService jsonService;

    @Test
    public void testToCardPayments() {
        CardPaymentParams cardPaymentParams = new CardPaymentParams();
        cardPaymentParams.setPan("4111111111111111");
        cardPaymentParams.setCardHolderName("this i");
        cardPaymentParams.setCvc("123");
        cardPaymentParams.setMonth("12");
        cardPaymentParams.setYear("2019");

        String json = jsonService.paymentParamsToJsonString(cardPaymentParams);
        assertNotNull(json);
        System.out.println(json);

        PaymentParams paymentParams = jsonService.jsonToPaymentParams(json);
        assertNotNull(paymentParams);
        System.out.println(paymentParams);
        if ( paymentParams instanceof CardPaymentParams ) {
            CardPaymentParams cardPaymentParams1 = (CardPaymentParams)paymentParams;
            System.out.println(cardPaymentParams1.getMaskedPan());
            System.out.println(cardPaymentParams1.getPan());
            System.out.println(cardPaymentParams.getCardHolderName());
        }
    }
}
