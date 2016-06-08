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
import ru.qrhandshake.qrpos.exception.AuthException;
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
    public void testSslCardPaymentByAnonymous() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
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
        assertNull(binding);

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
        assertNotNull(merchantOrder.getPaymentDate());
        assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
        assertNull(merchantOrder.getClient());
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());
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
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());

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
                        .param("authName",clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword",clientRegisterResponse.getAuth().getAuthPassword())
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
        assertNotNull(merchantOrder.getClient());
        assertEquals(clientRegisterResponse.getAuth().getAuthName(),merchantOrder.getClient().getUsername());

        ApiResponse apiResponseTerminal = authTerminal(terminalRegisterResponse.getAuth().getAuthName(),terminalRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponseTerminal.getStatus());

        MvcResult mvcResultReverse = mockMvc.perform(post("/order/reverse")
                        .param("authName",terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword",terminalRegisterResponse.getAuth().getAuthPassword())
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
}
