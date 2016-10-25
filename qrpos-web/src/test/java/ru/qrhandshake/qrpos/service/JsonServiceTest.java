package ru.qrhandshake.qrpos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.CardPaymentParams;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.PaymentWay;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.UserPasswordEndpoint;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lameroot on 31.05.16.
 */
public class JsonServiceTest extends GeneralTest {

    @Resource
    private JsonService jsonService;
    @Resource
    private ObjectMapper objectMapper;

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

        PaymentParams paymentParams = jsonService.jsonToPaymentParams(json, PaymentWay.CARD);
        assertNotNull(paymentParams);
        System.out.println(paymentParams);
        if ( paymentParams instanceof CardPaymentParams ) {
            CardPaymentParams cardPaymentParams1 = (CardPaymentParams)paymentParams;
            System.out.println(cardPaymentParams1.getPaymentAccount());
            System.out.println(cardPaymentParams1.getPan());
            System.out.println(cardPaymentParams.getCardHolderName());
        }
        //this is test
    }

    @Test
    public void testSerializer() throws Exception {
        Merchant merchant = new Merchant();
        merchant.setName("test");
        Terminal terminal1 = new Terminal();
        terminal1.setMerchant(merchant);
        terminal1.setAuthName("login1");
        Terminal terminal2 = new Terminal();
        terminal2.setMerchant(merchant);
        terminal2.setAuthName("login2");

        Set<Terminal> terminals = new HashSet<>();
        terminals.add(terminal1);
        terminals.add(terminal2);


        String s = objectMapper.writeValueAsString(merchant);
        System.out.println(s);

        System.out.println(objectMapper.writeValueAsString(terminal1));

        UserPasswordEndpoint userPasswordEndpoint = new UserPasswordEndpoint();
        userPasswordEndpoint.setPassword("pass");
        userPasswordEndpoint.setMerchant(merchant);

        System.out.println(objectMapper.writeValueAsString(userPasswordEndpoint));
    }
}
