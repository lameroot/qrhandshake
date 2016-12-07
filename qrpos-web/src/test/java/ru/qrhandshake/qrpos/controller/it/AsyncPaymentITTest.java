package ru.qrhandshake.qrpos.controller.it;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.api.PaymentResponse;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.TerminalRegisterResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.util.Util;
import ru.rbs.mpi.test.acs.AcsUtils;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestPropertySource(properties = {"integration.rbs.paymentBinding.async.enabled=true"})
public class AsyncPaymentITTest extends ItTest {

    private PaymentType expectedPaymentType;

    @Before
    public void initConfig() {
        expectedPaymentType = PaymentType.valueOf(environment.getProperty("integration.rbs.paymentType", PaymentType.PURCHASE.name()));
    }

    @Test
    public void testSslPaymentBinding() throws Exception {
        assertTrue(environment.getProperty("integration.rbs.paymentBinding.async.enabled", Boolean.class));

        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("pan", SSL_CARD)
                        .param("month", "12")
                        .param("year", "2019")
                        .param("cardHolderName", "test test")
                        .param("cvc", "123")
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult);
        PaymentResponse paymentResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaymentResponse.class);
        ReturnUrlObject returnUrlObject = paymentResponse.getReturnUrlObject();
        assertNotNull(returnUrlObject);
        assertEquals("redirect", returnUrlObject.getAction());
        assertTrue(returnUrlObject.getUrl().contains("/finish/"));
        assertTrue(returnUrlObject.getUrl().contains(merchantOrderRegisterResponse.getOrderId()));

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
        assertTrue(binding.isEnabled());
        assertEquals(PaymentSecureType.SSL, binding.getPaymentSecureType());

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());

        MerchantOrderRegisterResponse merchantOrderRegisterResponse1 = registerOrder(terminalRegisterResponse.getAuth(),
                amount, sessionId, deviceId);

        MvcResult mvcResultBinding = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse1.getOrderId())
                        .param("bindingId", binding.getBindingId())
                        .param("confirmValue", "123")
                        .param("paymentWay", "binding")
        ).andDo(print()).andReturn();

        assertNotNull(mvcResultBinding);
        PaymentResponse paymentResponseBinding = objectMapper.readValue(mvcResultBinding.getResponse().getContentAsString(), PaymentResponse.class);
        ReturnUrlObject returnUrlObjectBinding = paymentResponseBinding.getReturnUrlObject();
        assertNotNull(returnUrlObjectBinding);
        assertEquals("redirect", returnUrlObjectBinding.getAction());
        assertTrue(returnUrlObjectBinding.getUrl().contains("/finish/"));
        assertTrue(returnUrlObjectBinding.getUrl().contains(merchantOrderRegisterResponse1.getOrderId()));

        MerchantOrder merchantOrder1 = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse1.getOrderId());
        assertNotNull(merchantOrder1);
        assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PENDING);
        assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());

        Thread.sleep(5000L);
    }

    @Test
    public void testTdsPaymentBinding() throws Exception {
        assertTrue(environment.getProperty("integration.rbs.paymentBinding.async.enabled", Boolean.class));

        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("pan", TDS_CARD)
                        .param("month", "12")
                        .param("year", "2019")
                        .param("cardHolderName", "test test")
                        .param("cvc", "123")
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult);

        PaymentResponse paymentResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaymentResponse.class);
        ReturnUrlObject returnUrlObject = paymentResponse.getReturnUrlObject();
        assertNotNull(returnUrlObject);
        assertEquals("post",returnUrlObject.getAction());
        assertNotNull(returnUrlObject.getParams().get("MD"));
        assertNotNull(returnUrlObject.getParams().get("PaReq"));
        assertNotNull(returnUrlObject.getParams().get("TermUrl"));
        assertNotNull(returnUrlObject.getUrl());

        String paRes = AcsUtils.emulateCommunicationWithACS(returnUrlObject.getParams().get("MD"), returnUrlObject.getParams().get("TermUrl"), returnUrlObject.getParams().get("PaReq"), true);
        assertNotNull(paRes);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(returnUrlObject.getParams().get("TermUrl")
                + "?PaRes=" + paRes
                + "&MD=" + returnUrlObject.getParams().get("MD"), String.class);
        assertNotNull(responseEntity);
        System.out.println(responseEntity);
        assertEquals(302,responseEntity.getStatusCode().value());
        String finishUri = "/order/finish/" + merchantOrderRegisterResponse.getOrderId() + "?orderId=" + returnUrlObject.getParams().get("MD");
        assertTrue(responseEntity.getHeaders().getLocation().toString().contains(finishUri));

        MvcResult finishMvcResult = mockMvc.perform(get(finishUri))
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
        assertTrue(binding.isEnabled());
        assertEquals(PaymentSecureType.TDS, binding.getPaymentSecureType());

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(), merchantOrder.getClient().getUsername());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());

        MerchantOrderRegisterResponse merchantOrderRegisterResponse1 = registerOrder(terminalRegisterResponse.getAuth(),
                amount, sessionId, deviceId);
        MvcResult mvcResultBinding = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse1.getOrderId())
                        .param("bindingId", binding.getBindingId())
                                //.param("confirmValue", "123")
                        .param("paymentWay", "binding")
        ).andDo(print()).andReturn();

        assertNotNull(mvcResultBinding);
        PaymentResponse paymentResponseBinding = objectMapper.readValue(mvcResultBinding.getResponse().getContentAsString(), PaymentResponse.class);
        ReturnUrlObject returnUrlObjectBinding = paymentResponseBinding.getReturnUrlObject();
        assertNotNull(returnUrlObjectBinding);
        if ( "post".equals(returnUrlObjectBinding.getAction()) ) {//3ds
            assertEquals("post", returnUrlObjectBinding.getAction());
            assertNotNull(returnUrlObjectBinding.getParams().get("MD"));
            assertNotNull(returnUrlObjectBinding.getParams().get("PaReq"));
            assertNotNull(returnUrlObjectBinding.getParams().get("TermUrl"));
            assertNotNull(returnUrlObjectBinding.getUrl());
            assertEquals(OrderStatus.REDIRECTED_TO_EXTERNAL, paymentResponse.getOrderStatus());

            String paResBinding = AcsUtils.emulateCommunicationWithACS(returnUrlObjectBinding.getParams().get("MD"), returnUrlObjectBinding.getParams().get("TermUrl"), returnUrlObjectBinding.getParams().get("PaReq"), true);
            assertNotNull(paResBinding);
            ResponseEntity<String> responseEntityBinding = restTemplate.getForEntity(returnUrlObjectBinding.getParams().get("TermUrl")
                    + "?PaRes=" + paResBinding
                    + "&MD=" + returnUrlObjectBinding.getParams().get("MD"), String.class);
            assertNotNull(responseEntityBinding);
            System.out.println(responseEntityBinding);
            assertEquals(302, responseEntityBinding.getStatusCode().value());
            String finishUriBinding = "/order/finish/" + merchantOrderRegisterResponse1.getOrderId() + "?orderId=" + returnUrlObjectBinding.getParams().get("MD");
            assertTrue(responseEntityBinding.getHeaders().getLocation().toString().contains(finishUriBinding));

            MvcResult finishMvcResultBinding = mockMvc.perform(get(finishUriBinding))
                    .andDo(print())
                    .andReturn();
            assertNotNull(finishMvcResultBinding);
            Map<String, Object> finishModelBinding = finishMvcResultBinding.getModelAndView().getModel();
            assertNotNull(finishModelBinding);
            assertTrue(!finishModelBinding.isEmpty());
            assertTrue(ResponseStatus.SUCCESS.equals(finishModelBinding.get("status")));
            assertTrue(finishMvcResultBinding.getResponse().getForwardedUrl().contains("finish"));
        }
        else {
            assertEquals("redirect", returnUrlObjectBinding.getAction());
            assertTrue(returnUrlObjectBinding.getUrl().contains("/finish/"));
            assertTrue(returnUrlObjectBinding.getUrl().contains(merchantOrderRegisterResponse1.getOrderId()));

            MerchantOrder merchantOrder1 = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse1.getOrderId());
            assertNotNull(merchantOrder1);
            assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PENDING);
            assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());
        }

        Thread.sleep(5000L);
    }


}
