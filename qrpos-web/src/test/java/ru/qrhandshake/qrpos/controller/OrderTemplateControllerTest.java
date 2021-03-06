package ru.qrhandshake.qrpos.controller;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentByOrderTemplateResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryResponse;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryResult;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateResponse;
import ru.qrhandshake.qrpos.controller.it.ItTest;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.util.Util;
import ru.rbs.mpi.test.acs.AcsUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Created by lameroot on 08.08.16.
 */
@Transactional
@Rollback
public class OrderTemplateControllerTest extends ItTest {

    private PaymentType expectedPaymentType;

    @Before
    public void initConfig() {
        expectedPaymentType = PaymentType.valueOf(environment.getProperty("integration.rbs.paymentType", PaymentType.PURCHASE.name()));
    }

    @Test
    @Ignore
    public void testCreate() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/merchant/register")
                        .param("name", UUID.randomUUID().toString())
        )
                .andExpect(jsonPath("$.status",is("SUCCESS"))).andDo(print()).andReturn();
        assertNotNull(mvcResult);
        MerchantRegisterResponse merchantRegisterResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), MerchantRegisterResponse.class);
        assertNotNull(merchantRegisterResponse);
        assertNotNull(merchantRegisterResponse.getMerchantId());
        Merchant merchant = merchantRepository.findOne(Long.valueOf(merchantRegisterResponse.getMerchantId()));
        assertNotNull(merchant);

        Terminal terminal = terminalRepository.findByAuthName(merchantRegisterResponse.getTerminalAuth().getAuthName());
        assertNotNull(terminal);

        User user = userRepository.findByUsername(merchantRegisterResponse.getUserAuth().getAuthName());
        assertNotNull(user);

        Authentication authentication = new TestingAuthenticationToken(user, null);

        MvcResult orderTemplateMvcResult = mockMvc.perform(post("/order_template/create")
                        .principal(authentication)
                        .param("name", "test")
                        .param("description", "test description")
                        .param("amount", "20000")
                        .param("terminalId", String.valueOf(terminal.getId()))
        )
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andDo(print()).andReturn();

        assertNotNull(orderTemplateMvcResult);
        OrderTemplateResponse orderTemplateResponse = objectMapper.readValue(orderTemplateMvcResult.getResponse().getContentAsString(),OrderTemplateResponse.class);
        assertNotNull(orderTemplateResponse);
        assertNotNull(orderTemplateResponse.getId());

        OrderTemplate orderTemplate = orderTemplateRepository.findOne(orderTemplateResponse.getId());
        assertNotNull(orderTemplate);
        assertEquals(terminal.getId(), orderTemplate.getTerminal().getId());
    }



    @Test
    @Transactional
    @org.junit.Ignore
    public void testPaymentOrderByTemplate() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        Long amount = 100L;
        String sessionId = UUID.randomUUID().toString();
        String deviceId = UUID.randomUUID().toString();

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                amount,sessionId,deviceId);
        ApiResponse apiResponse = authClient(clientRegisterResponse.getAuth().getAuthName(), clientRegisterResponse.getAuth().getAuthPassword());
        assertTrue(ResponseStatus.SUCCESS == apiResponse.getStatus());
        Authentication authenticationClient = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authenticationClient)
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


        User user = userRepository.findByUsername(merchantRegisterResponse.getUserAuth().getAuthName());
        assertNotNull(user);
        Terminal terminal = terminalRepository.findByAuthName(terminalRegisterResponse.getAuth().getAuthName());
        assertNotNull(terminal);

        Authentication authenticationTerminal = new TestingAuthenticationToken(terminal, null);

        Authentication authenticationUser = new TestingAuthenticationToken(user, null);

        MvcResult orderTemplateMvcResult = mockMvc.perform(post("/order_template/create")
                        .principal(authenticationUser)
                        .param("name", "test")
                        .param("description", "test description")
                        .param("amount", "20000")
                        .param("terminalId", String.valueOf(terminal.getId()))
        )
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andDo(print()).andReturn();

        assertNotNull(orderTemplateMvcResult);
        OrderTemplateResponse orderTemplateResponse = objectMapper.readValue(orderTemplateMvcResult.getResponse().getContentAsString(),OrderTemplateResponse.class);
        assertNotNull(orderTemplateResponse);
        assertNotNull(orderTemplateResponse.getId());

        OrderTemplate orderTemplate = orderTemplateRepository.findOne(orderTemplateResponse.getId());
        assertNotNull(orderTemplate);
        assertEquals(terminal.getId(), orderTemplate.getTerminal().getId());

        Date start = new Date();
        IntStream.rangeClosed(1, 10).forEach(i -> {
            try {
                MvcResult bindingPaymentOrderTemplateMvcResult = mockMvc.perform(post("/order_template/payment")
                                .principal(authenticationClient)
                                .param("paymentWay", "bindingByOrderTemplate")
                                .param("bindingId", binding.getBindingId())
                                .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
                                .param("sessionId", sessionId)
                                .param("deviceId", deviceId)
                                .param("deviceModel", "Samsung V3")
                                .param("deviceMobileNumber", "+79267777777")

                ).andDo(print()).andReturn();
                assertNotNull(bindingPaymentOrderTemplateMvcResult);
                BindingPaymentByOrderTemplateResponse bindingPaymentByOrderTemplateResponse = objectMapper.readValue(bindingPaymentOrderTemplateMvcResult.getResponse().getContentAsString(), BindingPaymentByOrderTemplateResponse.class);
                assertNotNull(bindingPaymentByOrderTemplateResponse);
                assertNotNull(bindingPaymentByOrderTemplateResponse.getOrderId());

                MerchantOrder merchantOrderByOrderTemplate = merchantOrderRepository.findByOrderId(bindingPaymentByOrderTemplateResponse.getOrderId());
                assertNotNull(merchantOrderByOrderTemplate);
                assertTrue(merchantOrderByOrderTemplate.getOrderStatus() == OrderStatus.PAID);
                assertNotNull(merchantOrderByOrderTemplate.getPaymentDate());
                assertEquals(PaymentWay.BINDING, merchantOrderByOrderTemplate.getPaymentWay());
                assertNotNull(merchantOrderByOrderTemplate.getClient());
                assertEquals(clientRegisterResponse.getAuth().getAuthName(), merchantOrderByOrderTemplate.getClient().getUsername());
                assertEquals(expectedPaymentType, merchantOrderByOrderTemplate.getPaymentType());

                //todo: дописать тест, получать список заказов оплаченных
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        MvcResult getOrdersMvcResult = mockMvc.perform(get("/order_template/get_orders")
                        .principal(authenticationTerminal)
                        .param("authName", terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", terminalRegisterResponse.getAuth().getAuthPassword())
                        .param("from", new SimpleDateFormat("yyyyMMddHHmmss").format(start))
                        .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
        ).andDo(print()).andReturn();
        assertNotNull(getOrdersMvcResult);

        OrderTemplateHistoryResponse orderTemplateHistoryResponse = objectMapper.readValue(getOrdersMvcResult.getResponse().getContentAsString(),OrderTemplateHistoryResponse.class);
        assertNotNull(orderTemplateHistoryResponse);
        assertNotNull(orderTemplateHistoryResponse.getOrders());
        assertEquals(10, orderTemplateHistoryResponse.getOrders().size());
        for (Map<String, Object> map : orderTemplateHistoryResponse.getOrders()) {
            assertNotNull(map.containsKey("humanOrderNumber"));
        }

        Thread.sleep(1000);
        Date secondStart = new Date();
        IntStream.rangeClosed(1, 4).forEach(idx -> {
            try {
                MvcResult bindingPaymentOrderTemplateMvcResult = mockMvc.perform(post("/order_template/payment")
                                .principal(authenticationClient)
                                .param("paymentWay", "bindingByOrderTemplate")
                                .param("bindingId", binding.getBindingId())
                                .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
                                .param("sessionId", sessionId)
                                .param("deviceId", deviceId)
                                .param("deviceModel", "Nokia")
                                .param("deviceMobileNumber", "+79268888888")

                ).andDo(print()).andReturn();
                assertNotNull(bindingPaymentOrderTemplateMvcResult);
                BindingPaymentByOrderTemplateResponse bindingPaymentByOrderTemplateResponse = objectMapper.readValue(bindingPaymentOrderTemplateMvcResult.getResponse().getContentAsString(), BindingPaymentByOrderTemplateResponse.class);
                assertNotNull(bindingPaymentByOrderTemplateResponse);
                assertNotNull(bindingPaymentByOrderTemplateResponse.getOrderId());

                MerchantOrder merchantOrderByOrderTemplate = merchantOrderRepository.findByOrderId(bindingPaymentByOrderTemplateResponse.getOrderId());
                assertNotNull(merchantOrderByOrderTemplate);
                assertTrue(merchantOrderByOrderTemplate.getOrderStatus() == OrderStatus.PAID);
                assertNotNull(merchantOrderByOrderTemplate.getPaymentDate());
                assertEquals(PaymentWay.BINDING, merchantOrderByOrderTemplate.getPaymentWay());
                assertNotNull(merchantOrderByOrderTemplate.getClient());
                assertEquals(clientRegisterResponse.getAuth().getAuthName(), merchantOrderByOrderTemplate.getClient().getUsername());
                assertEquals(expectedPaymentType, merchantOrderByOrderTemplate.getPaymentType());

                //todo: дописать тест, получать список заказов оплаченных
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        MvcResult getOrdersMvcResult2 = mockMvc.perform(get("/order_template/get_orders")
                        .principal(authenticationTerminal)
                        .param("authName",terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword",terminalRegisterResponse.getAuth().getAuthPassword())
                        .param("from", new SimpleDateFormat("yyyyMMddHHmmss").format(secondStart))
                        .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
        ).andDo(print()).andReturn();
        assertNotNull(getOrdersMvcResult);

        OrderTemplateHistoryResponse orderTemplateHistoryResponse2 = objectMapper.readValue(getOrdersMvcResult2.getResponse().getContentAsString(),OrderTemplateHistoryResponse.class);
        assertNotNull(orderTemplateHistoryResponse2);
        assertNotNull(orderTemplateHistoryResponse2.getOrders());
        assertEquals(4, orderTemplateHistoryResponse2.getOrders().size());
        for (Map<String, Object> map : orderTemplateHistoryResponse2.getOrders()) {
            assertNotNull(map.containsKey("humanOrderNumber"));
        }

    }

    @Test
    public void testGetOrdersFrom() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));

        Terminal terminal = terminalRepository.findByAuthName(terminalRegisterResponse.getAuth().getAuthName());
        assertNotNull(terminal);

        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setTerminal(terminal);
        orderTemplate.setName("test");
        orderTemplate.setAmount(1000L);
        orderTemplate.setDescription("test");
        orderTemplateRepository.save(orderTemplate);


        Calendar calendar = Calendar.getInstance();
        Date start = calendar.getTime();
        int count = 10;

        List<OrderTemplateHistory> success = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MerchantOrder merchantOrder = new MerchantOrder();
            merchantOrder.setOrderId(UUID.randomUUID().toString());
            merchantOrder.setAmount(1000L);
            merchantOrderRepository.save(merchantOrder);

            calendar.add(Calendar.MINUTE, 1);

            OrderTemplateHistory orderTemplateHistory = new OrderTemplateHistory();
            orderTemplateHistory.setClientId(null);
            orderTemplateHistory.setDate(calendar.getTime());
            orderTemplateHistory.setDeviceId(UUID.randomUUID().toString());
            orderTemplateHistory.setDeviceModel(i % 2 == 0 ? "Samsung" : "Nokia");
            orderTemplateHistory.setHumanOrderNumber(StringUtils.leftPad(String.valueOf(i), 6, "0"));
            orderTemplateHistory.setMerchantOrderId(merchantOrder.getId());
            orderTemplateHistory.setOrderTemplateId(orderTemplate.getId());
            if ( i % 2 == 0 ) {
                orderTemplateHistory.setStatus(true);
                success.add(orderTemplateHistory);
            }
            else {
                orderTemplateHistory.setStatus(false);
            }

            orderTemplateHistoryRepository.save(orderTemplateHistory);
        }

        assertEquals(count, orderTemplateHistoryRepository.count());
        List<Long> successIds = success.stream().map(s -> s.getId()).collect(Collectors.toList());
        assertEquals(count / 2, successIds.size());

        //get last desc
        MvcResult mvcResultDesc = mockMvc.perform(get("/order_template/get_orders")
                        .param("authName",terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword",terminalRegisterResponse.getAuth().getAuthPassword())
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
                        .param("id", String.valueOf(successIds.get(2)))
                        .param("sort", "id,desc")
        ).andDo(print()).andReturn();

        OrderTemplateHistoryResult orderTemplateHistoryResultDesc = objectMapper.readValue(mvcResultDesc.getResponse().getContentAsString(), OrderTemplateHistoryResult.class);
        assertNotNull(orderTemplateHistoryResultDesc);

        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResultDesc.getOrders()) {
            System.out.println(orderTemplateHistoryData.toMap());
        }

        //get last asc
        MvcResult mvcResultAsc = mockMvc.perform(get("/order_template/get_orders")
                        .param("authName",terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword",terminalRegisterResponse.getAuth().getAuthPassword())
                        .param("page", "0")
                        .param("size", "10")
                        .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
                        .param("id", String.valueOf(successIds.get(2)))
                        .param("sort", "id,asc")
        ).andDo(print()).andReturn();

        OrderTemplateHistoryResult orderTemplateHistoryResultAsc = objectMapper.readValue(mvcResultAsc.getResponse().getContentAsString(), OrderTemplateHistoryResult.class);
        assertNotNull(orderTemplateHistoryResultAsc);

        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResultAsc.getOrders()) {
            System.out.println(orderTemplateHistoryData.toMap());
        }

        //
        //get last
        MvcResult mvcResultAscNull = mockMvc.perform(get("/order_template/get_orders")
                        .param("authName",terminalRegisterResponse.getAuth().getAuthName())
                        .param("authPassword",terminalRegisterResponse.getAuth().getAuthPassword())
                        .param("page", "0")
                        .param("size", "3")
                        .param("orderTemplateId", String.valueOf(orderTemplate.getId()))
                        //.param("id", String.valueOf(successIds.get(2)))
                        //.param("sort", "id,asc")
        ).andDo(print()).andReturn();

        OrderTemplateHistoryResult orderTemplateHistoryResultAscNull = objectMapper.readValue(mvcResultAscNull.getResponse().getContentAsString(), OrderTemplateHistoryResult.class);
        assertNotNull(orderTemplateHistoryResultAscNull);

        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResultAscNull.getOrders()) {
            System.out.println(orderTemplateHistoryData.toMap());
        }
    }

}
