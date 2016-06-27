package ru.qrhandshake.qrpos.controller.it;


import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.MerchantOrderDto;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.exception.MerchantOrderNotFoundException;
import ru.qrhandshake.qrpos.integration.IntegrationCompletionRequest;
import ru.qrhandshake.qrpos.integration.IntegrationCompletionResponse;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationFacade;
import ru.qrhandshake.qrpos.repository.BindingRepository;
import ru.qrhandshake.qrpos.repository.MerchantOrderRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;
import ru.qrhandshake.qrpos.service.UserService;
import ru.qrhandshake.qrpos.util.Util;
import ru.rbs.mpi.test.acs.AcsUtils;

import javax.annotation.Resource;
import java.util.HashMap;
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
    @Autowired(required = false)
    private RbsIntegrationFacade rbsIntegrationFacade;
    @Resource
    private IntegrationService integrationService;
    private PaymentType expectedPaymentType;

    @Before
    public void initConfig() {
        expectedPaymentType = PaymentType.valueOf(environment.getProperty("rbs.paymentType", PaymentType.PURCHASE.name()));
    }

    @Test
    @Transactional
    public void testOrderNotFound() throws Exception {
        String unknownOrderId = UUID.randomUUID().toString();
        MvcResult mvcResult = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH + "/" + unknownOrderId))
                .andDo(print())
                .andReturn();
        assertNotNull(mvcResult);
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains("payment.html?orderId=" + unknownOrderId));
    }

    @Test
    @Transactional
    public void testCanPaymentMerchantOrder() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);

        MvcResult mvcResult = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.PAYMENT_PATH + "/" + merchantOrderRegisterResponse.getOrderId()))
                .andDo(print())
                .andReturn();
        assertNotNull(mvcResult);
        assertTrue(mvcResult.getResponse().getRedirectedUrl().contains("payment.html?orderId=" + merchantOrderRegisterResponse.getOrderId()));

        MvcResult mvcResultSessionStatus = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.SESSION_STATUS_PATH)
                .param("orderId", merchantOrderRegisterResponse.getOrderId())).andDo(print()).andReturn();
        assertNotNull(mvcResultSessionStatus);

        SessionStatusResponse sessionStatusResponse = objectMapper.readValue(mvcResultSessionStatus.getResponse().getContentAsString(), SessionStatusResponse.class);
        assertNotNull(sessionStatusResponse);
        assertEquals(ResponseStatus.SUCCESS,sessionStatusResponse.getStatus());
        assertTrue(OrderStatus.PAID != sessionStatusResponse.getOrderStatus());
    }

    @Test
    @Transactional
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

        MvcResult mvcResultSessionStatus = mockMvc.perform(get(MerchantOrderController.MERCHANT_ORDER_PATH + MerchantOrderController.SESSION_STATUS_PATH)
            .param("orderId", merchantOrderRegisterResponse.getOrderId())).andDo(print()).andReturn();
        assertNotNull(mvcResultSessionStatus);

        SessionStatusResponse sessionStatusResponse = objectMapper.readValue(mvcResultSessionStatus.getResponse().getContentAsString(), SessionStatusResponse.class);
        assertNotNull(sessionStatusResponse);
        assertEquals(ResponseStatus.SUCCESS,sessionStatusResponse.getStatus());
        assertEquals(OrderStatus.PAID, sessionStatusResponse.getOrderStatus());
    }

    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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

        IntegrationCompletionRequest integrationCompletionRequest = new IntegrationCompletionRequest(merchantOrder.getIntegrationSupport(),merchantOrder.getExternalId());
        integrationCompletionRequest.setOrderId(merchantOrder.getOrderId());
        integrationCompletionRequest.setAmount(merchantOrder.getAmount());
        IntegrationCompletionResponse integrationCompletionResponse = integrationService.completion(integrationCompletionRequest);
        assertTrue(integrationCompletionResponse.isSuccess());

        ReflectionTestUtils.setField(rbsIntegrationFacade,"sPaymentType",sRealPaymentType);
    }

    @Test
    @Transactional
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
    @Transactional
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

    @Test
    @Transactional
    public void testBindingPayment() throws Exception {
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
        assertTrue(merchantOrder1.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder1.getPaymentDate());
        assertEquals(PaymentWay.BINDING, merchantOrder1.getPaymentWay());
    }

    @Test
    @Transactional
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
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());

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
    @Transactional
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

        ApiResponse apiResponseTerminal = authTerminal(terminalRegisterResponse.getAuth().getAuthName(),terminalRegisterResponse.getAuth().getAuthPassword());
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
    @Transactional
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
    @Transactional
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


        MvcResult mvcResultGetBindings = mockMvc.perform(get("/order/getBindings")
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
    @Transactional
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
            .param("orderId",merchantOrderRegisterResponse.getOrderId())
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
    @Transactional
    public void testGetSessionStatusSuccessAndNotPaid() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());

        MvcResult getSessionStatusMvcResult = mockMvc.perform(get("/order/sessionStatus")
                        .param("orderId",merchantOrderRegisterResponse.getOrderId())
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
    @Transactional
    public void testGetSessionStatusOrderNotFound() throws Exception {
        MvcResult getSessionStatusMvcResult = mockMvc.perform(get("/order/sessionStatus")
                        .param("orderId",UUID.randomUUID().toString())
        ).andDo(print()).andReturn();

        assertNotNull(getSessionStatusMvcResult);
        assertTrue(getSessionStatusMvcResult.getResolvedException() instanceof MerchantOrderNotFoundException);
        ApiResponse apiResponse = objectMapper.readValue(getSessionStatusMvcResult.getResponse().getContentAsString(),ApiResponse.class);
        assertNotNull(apiResponse);
        assertEquals(ResponseStatus.FAIL,apiResponse.getStatus());
    }

    //todo: тест на дублирующиеся биндинги и 3дс платеж
}
