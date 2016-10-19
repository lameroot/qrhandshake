package ru.qrhandshake.qrpos.controller.it;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.binding.GetBindingsResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderReverseResponse;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.integration.*;
import ru.qrhandshake.qrpos.util.Util;
import ru.rbs.mpi.test.acs.AcsUtils;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by lameroot on 06.06.16.
 */
public class PaymentITTest extends ItTest {

    private PaymentType expectedPaymentType;

    @Before
    public void initConfig() {
        expectedPaymentType = PaymentType.valueOf(environment.getProperty("integration.rbs.paymentType", PaymentType.PURCHASE.name()));
    }

    @Test
    public void testOrderNotFound() throws Exception {
        String unknownOrderId = UUID.randomUUID().toString();
        MvcResult mvcResult = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH + "/" + unknownOrderId))
                .andDo(print())
                .andReturn();
        assertNotNull(mvcResult);
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains("payment.html?orderId=" + unknownOrderId));
    }

    @Test
    public void testCanPaymentMerchantOrder() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());


        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);

        MvcResult mvcResult = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH + "/" + merchantOrderRegisterResponse.getOrderId()))
                .andDo(print())
                .andReturn();
        assertNotNull(mvcResult);
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains("payment.html?orderId=" + merchantOrderRegisterResponse.getOrderId()));

        MvcResult mvcResultSessionStatus = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.SESSION_STATUS_PATH)
                .principal(authentication)
                .param("orderId", merchantOrderRegisterResponse.getOrderId())).andDo(print()).andReturn();
        assertNotNull(mvcResultSessionStatus);

        SessionStatusResponse sessionStatusResponse = objectMapper.readValue(mvcResultSessionStatus.getResponse().getContentAsString(), SessionStatusResponse.class);
        assertNotNull(sessionStatusResponse);
        assertEquals(ResponseStatus.SUCCESS,sessionStatusResponse.getStatus());
        assertTrue(OrderStatus.PAID != sessionStatusResponse.getOrderStatus());
    }

    @Test
    public void testCanNotPaymentMerchantOrder() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
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
        assertNull(binding);

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertNull(merchantOrder.getClient());


        MvcResult mvcResultPayment = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH + "/" + merchantOrderRegisterResponse.getOrderId()))
                .andDo(print())
                .andReturn();
        assertNotNull(mvcResultPayment);
        assertTrue(mvcResultPayment.getResponse().getRedirectedUrl().contains("payment.html?orderId=" + merchantOrderRegisterResponse.getOrderId()));

        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResultSessionStatus = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.SESSION_STATUS_PATH)
                .principal(authentication)
            .param("orderId", merchantOrderRegisterResponse.getOrderId())).andDo(print()).andReturn();
        assertNotNull(mvcResultSessionStatus);

        SessionStatusResponse sessionStatusResponse = objectMapper.readValue(mvcResultSessionStatus.getResponse().getContentAsString(), SessionStatusResponse.class);
        assertNotNull(sessionStatusResponse);
        assertEquals(ResponseStatus.SUCCESS,sessionStatusResponse.getStatus());
        assertEquals(OrderStatus.PAID, sessionStatusResponse.getOrderStatus());
    }

    @Test
    public void testTdsCardPaymentByAnonymous() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
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
        assertNull(binding);

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertNull(merchantOrder.getClient());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());
    }

    @Test
    public void testTdsCardPaymentByApiAuth() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId, true);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(),clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
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
    }

    @Test
    public void testSslCardPaymentByAnonymous() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
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
        assertNull(binding);

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertNull(merchantOrder.getClient());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());
    }

    @Test
    public void testSslCardDepositAndCompletionByAnonymous() throws Exception {
        if ( null == rbsIntegrationFacade ) {
            return;
        }
        expectedPaymentType = PaymentType.DEPOSIT;
        String sRealPaymentType = (String)ReflectionTestUtils.getField(rbsIntegrationFacade, "sPaymentType");
        ReflectionTestUtils.setField(rbsIntegrationFacade,"sPaymentType",expectedPaymentType.name());
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
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
        assertNull(binding);

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertNull(merchantOrder.getClient());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());

        Endpoint endpoint = endpointRepository.findByMerchantAndIntegrationSupport(merchantOrder.getMerchant(), merchantOrder.getIntegrationSupport());
        IntegrationCompletionRequest integrationCompletionRequest = new IntegrationCompletionRequest(endpoint,merchantOrder.getExternalId());
        integrationCompletionRequest.setOrderId(merchantOrder.getOrderId());
        integrationCompletionRequest.setAmount(merchantOrder.getAmount());
        IntegrationCompletionResponse integrationCompletionResponse = integrationService.completion(integrationCompletionRequest);
        assertTrue(integrationCompletionResponse.isSuccess());

        ReflectionTestUtils.setField(rbsIntegrationFacade,"sPaymentType",sRealPaymentType);
    }

    @Test
    public void testSslCardPaymentByClientUsePrincipal() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(),clientRegisterResponse.getAuth().getAuthPassword());
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());
    }

    @Test
    public void testFailAllAttemptsSslPayment() throws Exception {
        int attempts = 3;
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId, true);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(),clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        while (attempts-- > 1) {
            MvcResult mvcResult0 = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                            .param("authName", clientRegisterResponse.getAuth().getAuthName())
                            .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
                            .param("orderId", merchantOrderRegisterResponse.getOrderId())
                            .param("pan", SSL_CARD)
                            .param("month", "12")
                            .param("year", "2019")
                            .param("cardHolderName", "test test")
                            .param("cvc", "666")//invalid cvc
                            .param("paymentWay", "card")
            ).andDo(print()).andReturn();
            assertNotNull(mvcResult0);
            PaymentResponse paymentResponse0 = objectMapper.readValue(mvcResult0.getResponse().getContentAsString(), PaymentResponse.class);
            assertEquals(OrderStatus.REGISTERED,paymentResponse0.getOrderStatus());
            assertEquals(ResponseStatus.FAIL,paymentResponse0.getStatus());//todo: поменять на success если ответ есть, даже если неудачный
        }

        MvcResult mvcResult0 = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("pan", SSL_CARD)
                        .param("month", "12")
                        .param("year", "2019")
                        .param("cardHolderName", "test test")
                        .param("cvc", "666")//invalid cvc
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult0);
        PaymentResponse paymentResponse0 = objectMapper.readValue(mvcResult0.getResponse().getContentAsString(), PaymentResponse.class);
        assertEquals(OrderStatus.DECLINED,paymentResponse0.getOrderStatus());
        assertEquals(ResponseStatus.SUCCESS,paymentResponse0.getStatus());
    }


    //todo: написать тесты если закончились попытки а также по 3дс карте
    @Test
    public void testFailAndSuccessSslPayment() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId, true);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(),clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        MvcResult mvcResult0 = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("pan", SSL_CARD)
                        .param("month", "12")
                        .param("year", "2019")
                        .param("cardHolderName", "test test")
                        .param("cvc", "666")//invalid cvc
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult0);
        PaymentResponse paymentResponse0 = objectMapper.readValue(mvcResult0.getResponse().getContentAsString(), PaymentResponse.class);
        assertEquals(OrderStatus.REGISTERED,paymentResponse0.getOrderStatus());
        assertEquals(ResponseStatus.FAIL,paymentResponse0.getStatus());
        assertNull(paymentResponse0.getReturnUrlObject());

        MerchantOrder merchantOrder0 = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertEquals(OrderStatus.REGISTERED, merchantOrder0.getOrderStatus());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
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
        assertEquals(merchantOrderRegisterResponse.getOrderId(),finishModel.get("orderId"));
        assertEquals(OrderStatus.PAID,finishModel.get("orderStatus"));
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());
    }

    @Test
    public void testSslCardPaymentByClientUseApiAuth() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId, true);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(),clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());
        assertEquals(expectedPaymentType, merchantOrder.getPaymentType());
    }

    /*
    Оплата по 3дс связке. Сначало делаем оплату 3дс картой, потом оплачиваем по связке. Взависимости от настроеке мерчанта
    оплата по связке может идти или по 3дс или ссл (в коде условие). Также стоит возможность оплаты безе cvc
     */
    @Test
    public void testBindingTdsPayment() throws Exception {
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
            assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PAID);
            assertNotNull(merchantOrder1.getPaymentDate());
            assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());
        }
    }

    @Test
    public void testBindingSslPayment() throws Exception {
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
        assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PAID || merchantOrder1.getOrderStatus() == OrderStatus.PENDING);
        if ( merchantOrder1.getOrderStatus() == OrderStatus.PAID ) {
            assertNotNull(merchantOrder1.getPaymentDate());
        }
        assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());
        Thread.sleep(100000);
    }

    @Test
    public void testSslPaymentAndReverseUsePrincipal() throws Exception {
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(), merchantOrder.getClient().getUsername());

        ApiResponse apiResponseTerminal = authTerminal(terminalRegisterResponse.getAuth().getAuthName(),terminalRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponseTerminal.getStatus());

        MvcResult mvcResultReverse = mockMvc.perform(post("/order/reverse")
                        .principal(terminalTestingAuthenticationToken(terminalRegisterResponse.getAuth()))
                        .param("sessionId", sessionId)
                        .param("orderId", merchantOrder.getOrderId())
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultReverse);
        String reverseResponse = mvcResultReverse.getResponse().getContentAsString();
        MerchantOrderReverseResponse merchantOrderReverseResponse = objectMapper.readValue(reverseResponse,MerchantOrderReverseResponse.class);
        assertNotNull(merchantOrderReverseResponse);
        assertTrue(ResponseStatus.SUCCESS == merchantOrderReverseResponse.getStatus());

        MerchantOrder merchantOrderAfterReverse = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrderAfterReverse);
        assertEquals(OrderStatus.REVERSED, merchantOrderAfterReverse.getOrderStatus());
    }

    @Test
    public void testSslPaymentAndReverseUseApiAuth() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId,true);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());

        ApiResponse apiResponseTerminal = authTerminal(terminalRegisterResponse.getAuth().getAuthName(), terminalRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponseTerminal.getStatus());

        MvcResult mvcResultReverse = mockMvc.perform(post("/order/reverse")
                        .param("authName", terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", terminalRegisterResponse.getAuth().getAuthPassword())
                        .param("sessionId", sessionId)
                        .param("orderId", merchantOrder.getOrderId())
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultReverse);
        String reverseResponse = mvcResultReverse.getResponse().getContentAsString();
        MerchantOrderReverseResponse merchantOrderReverseResponse = objectMapper.readValue(reverseResponse,MerchantOrderReverseResponse.class);
        assertNotNull(merchantOrderReverseResponse);
        assertTrue(ResponseStatus.SUCCESS == merchantOrderReverseResponse.getStatus());

        MerchantOrder merchantOrderAfterReverse = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrderAfterReverse);
        assertEquals(OrderStatus.REVERSED, merchantOrderAfterReverse.getOrderStatus());
    }

    @Test
    public void testSslCardPaymentAndInvalidReverseRequestAsSessionId() throws Exception {
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

        ApiResponse apiResponseTerminal = authTerminal(terminalRegisterResponse.getAuth().getAuthName(),terminalRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponseTerminal.getStatus());

        MvcResult mvcResultReverse = mockMvc.perform(post("/order/reverse")
                        .principal(terminalTestingAuthenticationToken(terminalRegisterResponse.getAuth()))
                        .param("sessionId", "invalid session id")
                        .param("orderId", merchantOrder.getOrderId())
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultReverse);
        String reverseResponse = mvcResultReverse.getResponse().getContentAsString();
        ApiResponse merchantOrderReverseResponse = objectMapper.readValue(reverseResponse,ApiResponse.class);
        assertNotNull(merchantOrderReverseResponse);
        assertTrue(ResponseStatus.FAIL == merchantOrderReverseResponse.getStatus());

        MerchantOrder merchantOrderAfterReverse = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrderAfterReverse);
        assertEquals(OrderStatus.PAID, merchantOrderAfterReverse.getOrderStatus());
    }

    @Test
    public void testGetBindings() throws Exception {
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


        MvcResult mvcResultGetBindings = mockMvc.perform(get("/binding/get_bindings")
                        .principal(authentication)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultGetBindings);
        String responseGetBindings = mvcResultGetBindings.getResponse().getContentAsString();
        assertNotNull(responseGetBindings);

        GetBindingsResponse getBindingsResponse = objectMapper.readValue(responseGetBindings, GetBindingsResponse.class);
        assertNotNull(getBindingsResponse);
        assertTrue(ResponseStatus.SUCCESS == getBindingsResponse.getStatus());
        assertNotNull(getBindingsResponse.getBindings());
        assertEquals(1, getBindingsResponse.getBindings().size());
        assertEquals(binding.getBindingId(), getBindingsResponse.getBindings().iterator().next().getBindingId());
    }

    @Test
    public void testGetSessionStatusSuccessAndPaid() throws Exception {
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

        MvcResult getSessionStatusMvcResult = mockMvc.perform(get("/order/sessionStatus")
                        .principal(authentication)
            .param("orderId", merchantOrderRegisterResponse.getOrderId())
            ).andDo(print()).andReturn();

        assertNotNull(getSessionStatusMvcResult);
        SessionStatusResponse sessionStatusResponse = objectMapper.readValue(getSessionStatusMvcResult.getResponse().getContentAsString(),SessionStatusResponse.class);
        assertNotNull(sessionStatusResponse);
        assertEquals(merchantOrderRegisterResponse.getOrderId(),sessionStatusResponse.getOrderId());
        assertEquals(OrderStatus.PAID,sessionStatusResponse.getOrderStatus());
        assertEquals(amount,sessionStatusResponse.getAmount());
        assertEquals(false,sessionStatusResponse.isCanPayment());

    }

    @Test
    public void testGetSessionStatusSuccessAndNotPaid() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());
        MvcResult getSessionStatusMvcResult = mockMvc.perform(get("/order/sessionStatus")
                        .principal(authentication)
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
        ).andDo(print()).andReturn();

        assertNotNull(getSessionStatusMvcResult);
        SessionStatusResponse sessionStatusResponse = objectMapper.readValue(getSessionStatusMvcResult.getResponse().getContentAsString(),SessionStatusResponse.class);
        assertNotNull(sessionStatusResponse);
        assertEquals(merchantOrderRegisterResponse.getOrderId(),sessionStatusResponse.getOrderId());
        assertEquals(amount,sessionStatusResponse.getAmount());
        assertEquals(OrderStatus.REGISTERED,sessionStatusResponse.getOrderStatus());
        assertEquals(true,sessionStatusResponse.isCanPayment());
    }

    @Test
    public void testGetSessionStatusOrderNotFound() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult getSessionStatusMvcResult = mockMvc.perform(get("/order/sessionStatus")
                        .principal(authentication)
                        .param("orderId", UUID.randomUUID().toString())
        ).andDo(print()).andReturn();

        assertNotNull(getSessionStatusMvcResult);
        assertTrue(getSessionStatusMvcResult.getResolvedException() instanceof MerchantOrderNotFoundException);
        ApiResponse apiResponse = objectMapper.readValue(getSessionStatusMvcResult.getResponse().getContentAsString(),ApiResponse.class);
        assertNotNull(apiResponse);
        assertEquals(ResponseStatus.FAIL,apiResponse.getStatus());
    }

    @Test
    public void testDuplicateRegisterBinding() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8), "client", AuthType.PASSWORD);
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        registerBinding(clientRegisterResponse, new TDSCardData());
        registerBinding(clientRegisterResponse, new TDSCardData());
        registerBinding(clientRegisterResponse, new SSLCardData());

        MvcResult mvcResultGetBindings = mockMvc.perform(get("/binding/get_bindings")
                        .principal(authentication)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultGetBindings);
        String responseGetBindings = mvcResultGetBindings.getResponse().getContentAsString();
        assertNotNull(responseGetBindings);

        GetBindingsResponse getBindingsResponse = objectMapper.readValue(responseGetBindings, GetBindingsResponse.class);
        assertNotNull(getBindingsResponse);
        assertTrue(ResponseStatus.SUCCESS == getBindingsResponse.getStatus());
        assertNotNull(getBindingsResponse.getBindings());

        assertEquals(2,getBindingsResponse.getBindings().size());

    }

    @Test
    public void testRegisterOrderForBindingDeclineThenRegisterSuccessAndBindingHaveToCreateViaSSLCard() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        IntegrationService integrationServiceMock = Mockito.mock(IntegrationService.class);
        IntegrationPaymentResponse integrationPaymentResponseFail = new IntegrationPaymentResponse();
        integrationPaymentResponseFail.setSuccess(false);
        integrationPaymentResponseFail.setMessage("test");
        Mockito.when(integrationServiceMock.payment(org.mockito.Matchers.anyObject())).thenReturn(integrationPaymentResponseFail);
        ReflectionTestUtils.setField(orderService,"integrationService",integrationServiceMock);

        SSLCardData sslCardDataDecline = new SSLCardData(){
            @Override
            public boolean isValid() {
                return false;
            }
        };
        registerBinding(clientRegisterResponse, sslCardDataDecline);
        Client client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);
        List<Binding> bindings = bindingRepository.findByClient(client);
        assertEquals(0, bindings.size());

        MvcResult mvcResultGetBindings = mockMvc.perform(get("/binding/get_bindings")
                        .principal(authentication)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultGetBindings);
        String responseGetBindings = mvcResultGetBindings.getResponse().getContentAsString();
        assertNotNull(responseGetBindings);
        GetBindingsResponse getBindingsResponse = objectMapper.readValue(responseGetBindings, GetBindingsResponse.class);
        assertNotNull(getBindingsResponse);
        assertTrue(ResponseStatus.SUCCESS == getBindingsResponse.getStatus());
        assertNotNull(getBindingsResponse.getBindings());
        assertEquals(0,getBindingsResponse.getBindings().size());

        ReflectionTestUtils.setField(orderService,"integrationService",integrationService);
        SSLCardData sslCardDataSuccess = new SSLCardData(){
            @Override
            public boolean isValid() {
                return true;
            }
        };
        registerBinding(clientRegisterResponse, sslCardDataSuccess);
        client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);
        bindings = bindingRepository.findByClient(client);
        assertEquals(1, bindings.size());
        assertTrue(bindings.get(0).isEnabled());

        mvcResultGetBindings = mockMvc.perform(get("/binding/get_bindings")
                        .principal(authentication)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultGetBindings);
        responseGetBindings = mvcResultGetBindings.getResponse().getContentAsString();
        assertNotNull(responseGetBindings);
        getBindingsResponse = objectMapper.readValue(responseGetBindings, GetBindingsResponse.class);
        assertNotNull(getBindingsResponse);
        assertTrue(ResponseStatus.SUCCESS == getBindingsResponse.getStatus());
        assertNotNull(getBindingsResponse.getBindings());
        assertEquals(1,getBindingsResponse.getBindings().size());
    }

    @Test
    public void testRegisterOrderForBindingDeclineThenRegisterSuccessAndBindingHaveToCreateViaTDSCard() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8), "client", AuthType.PASSWORD);
        Authentication authentication = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        TDSCardData tdsCardDataDecline = new TDSCardData(){
            @Override
            public boolean isValid() {
                return false;
            }
        };
        registerBinding(clientRegisterResponse, tdsCardDataDecline);
        Client client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);
        List<Binding> bindings = bindingRepository.findByClient(client);
        assertEquals(1, bindings.size());
        assertFalse(bindings.get(0).isEnabled());

        MvcResult mvcResultGetBindings = mockMvc.perform(get("/binding/get_bindings")
                        .principal(authentication)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultGetBindings);
        String responseGetBindings = mvcResultGetBindings.getResponse().getContentAsString();
        assertNotNull(responseGetBindings);
        GetBindingsResponse getBindingsResponse = objectMapper.readValue(responseGetBindings, GetBindingsResponse.class);
        assertNotNull(getBindingsResponse);
        assertTrue(ResponseStatus.SUCCESS == getBindingsResponse.getStatus());
        assertNotNull(getBindingsResponse.getBindings());
        assertEquals(0,getBindingsResponse.getBindings().size());

        TDSCardData tdsCardDataSuccess = new TDSCardData(){
            @Override
            public boolean isValid() {
                return true;
            }
        };
        registerBinding(clientRegisterResponse, tdsCardDataSuccess);
        client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);
        bindings = bindingRepository.findByClient(client);
        assertEquals(1, bindings.size());
        assertTrue(bindings.get(0).isEnabled());

        mvcResultGetBindings = mockMvc.perform(get("/binding/get_bindings")
                        .principal(authentication)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResultGetBindings);
        responseGetBindings = mvcResultGetBindings.getResponse().getContentAsString();
        assertNotNull(responseGetBindings);
        getBindingsResponse = objectMapper.readValue(responseGetBindings, GetBindingsResponse.class);
        assertNotNull(getBindingsResponse);
        assertTrue(ResponseStatus.SUCCESS == getBindingsResponse.getStatus());
        assertNotNull(getBindingsResponse.getBindings());
        assertEquals(1,getBindingsResponse.getBindings().size());
    }

    @Test
    public void testRegisterOrderForBinding() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        Principal principalClient = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());
        MvcResult mvcResultRegister = mockMvc.perform(get("/order/register_for_binding")
                        .principal(principalClient)
                        .param("amount", amount.toString())
                        .param("sessionId", sessionId)
                        .param("deviceId", deviceId)
        ).andDo(print()).andReturn();

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(mvcResultRegister.getResponse().getContentAsString(), MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);
        MerchantOrder merchantOrderByOrderId = merchantOrderRepository.findByOrderId(orderId);
        assertNotNull(merchantOrderByOrderId);
        Merchant rootMerchant = merchantService.findRootMerchant();
        assertNotNull(rootMerchant);
        assertEquals(rootMerchant.getId(), merchantOrderByOrderId.getMerchant().getId());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(principalClient)
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

        //проводим платеж уже другим мерчантом но со связкой, которая была создана рутовым
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        MerchantOrderRegisterResponse merchantOrderRegisterResponse1 = registerOrder(terminalRegisterResponse.getAuth(),
                amount, sessionId, deviceId);
        MvcResult mvcResultBinding = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(principalClient)
                        .param("orderId", merchantOrderRegisterResponse1.getOrderId())
                        .param("bindingId", binding.getBindingId())
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
            assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PAID);
            assertNotNull(merchantOrder1.getPaymentDate());
            assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());
        }


    }
}
