package ru.qrhandshake.qrpos.controller.it;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.repository.BindingRepository;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;
import ru.qrhandshake.qrpos.service.UserService;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by lameroot on 06.06.16.
 */
public class PaymentITTest extends ItTest {

    private final static String SSL_CARD = "5555555555555599";
    private final static String TDS_CARD = "4111111111111111";

    Long amount = 1000L;
    String sessionId = UUID.randomUUID().toString();
    String deviceId = UUID.randomUUID().toString();

    @Resource
    private MerchantOrderRepository merchantOrderRepository;
    @Resource
    private BindingRepository bindingRepository;

    @Test
    @Transactional
    public void testSslCardPaymentByClient() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("paymentParams.pan", SSL_CARD)
                        .param("paymentParams.month", "12")
                        .param("paymentParams.year", "2019")
                        .param("paymentParams.cardHolderName", "test test")
                        .param("paymentParams.cvc", "123")
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult);
        assertTrue(mvcResult.getResponse().getStatus() == 302);
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains("/finish/"));
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains(merchantOrderRegisterResponse.getOrderId()));

        MvcResult finishMvcResult = mockMvc.perform(get("/order/finish/" + merchantOrderRegisterResponse.getOrderId())
                .param("orderId", merchantOrderRegisterResponse.getOrderId()))
                .andDo(print())
                .andReturn();
        assertNotNull(finishMvcResult);
        Map<String,Object> finishModel = finishMvcResult.getModelAndView().getModel();
        assertNotNull(finishModel);
        assertTrue(!finishModel.isEmpty());
        assertTrue(ResponseStatus.SUCCESS.equals(finishModel.get("status")));
        assertTrue(finishMvcResult.getResponse().getForwardedUrl().contains("finish"));

        Binding binding = bindingRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(binding);
        assertTrue(binding.isCompleted());

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
    }

    @Test
    @Transactional
    public void testBindingPayment() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("paymentParams.pan", SSL_CARD)
                        .param("paymentParams.month", "12")
                        .param("paymentParams.year", "2019")
                        .param("paymentParams.cardHolderName", "test test")
                        .param("paymentParams.cvc", "123")
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult);
        assertTrue(mvcResult.getResponse().getStatus() == 302);
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains("/finish/"));
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains(merchantOrderRegisterResponse.getOrderId()));

        MvcResult finishMvcResult = mockMvc.perform(get("/order/finish/" + merchantOrderRegisterResponse.getOrderId())
                .param("orderId", merchantOrderRegisterResponse.getOrderId()))
                .andDo(print())
                .andReturn();
        assertNotNull(finishMvcResult);
        Map<String,Object> finishModel = finishMvcResult.getModelAndView().getModel();
        assertNotNull(finishModel);
        assertTrue(!finishModel.isEmpty());
        assertTrue(ResponseStatus.SUCCESS.equals(finishModel.get("status")));
        assertTrue(finishMvcResult.getResponse().getForwardedUrl().contains("finish"));

        Binding binding = bindingRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(binding);
        assertTrue(binding.isCompleted());

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());

        MerchantOrderRegisterResponse merchantOrderRegisterResponse1 = registerOrder(terminalRegisterResponse.getAuth(),
                amount, sessionId, deviceId);
        MvcResult mvcResultBinding = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse1.getOrderId())
                        .param("paymentParams.bindingId", binding.getBindingId())
                        .param("paymentParams.confirmValue", "123")
                        .param("paymentWay", "binding")
        ).andDo(print()).andReturn();

        assertNotNull(mvcResultBinding);
        assertTrue(mvcResultBinding.getResponse().getStatus() == 302);
        assertTrue(mvcResultBinding.getResponse().getRedirectedUrl().contains("/finish/"));
        assertTrue(mvcResultBinding.getResponse().getRedirectedUrl().contains(merchantOrderRegisterResponse1.getOrderId()));

        MerchantOrder merchantOrder1 = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse1.getOrderId());
        assertNotNull(merchantOrder1);
        assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder1.getPaymentDate());
        assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());

    }
}
