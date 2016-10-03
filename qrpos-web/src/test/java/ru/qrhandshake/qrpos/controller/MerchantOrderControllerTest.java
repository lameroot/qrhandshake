package ru.qrhandshake.qrpos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderStatusResponse;
import ru.qrhandshake.qrpos.domain.Binding;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.repository.BindingRepository;
import ru.qrhandshake.qrpos.service.ClientService;
import ru.qrhandshake.qrpos.service.MerchantService;
import ru.qrhandshake.qrpos.service.UserService;

import javax.annotation.Resource;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by lameroot on 23.05.16.
 */
public class MerchantOrderControllerTest extends ServletConfigTest {

    @Resource
    private MerchantService merchantService;
    @Resource
    private UserService userService;
    @Resource
    private ClientService clientService;
    @Resource
    private BindingRepository bindingRepository;
    @Resource
    private RestTemplate restTemplate;

    private final static String MERCHANT_LOGIN = "merchant";
    private final static String MERCHANT_PASSWORD = "password";
    private final static String SSL_CARD = "5555555555555599";
    private final static String TDS_CARD = "4111111111111111";


    @Test
    public void testRegister() throws Exception {
        mockMvc.perform(get("/order/register")
                .param("authName","merchant.auth")
                .param("authPassword","merchant.password")
                .param("amount", "1000")
                .param("sessionId", UUID.randomUUID().toString())
                .param("deviceId","11111-2222-333")
        )
                .andDo(print());
    }

    @Test
    public void testRegisterByTemplate() throws Exception {
        mockMvc.perform(get("/order/registerByTemplate")
                .param("authName","user")
                .param("authPassword","password")
                .param("templateId", "37")
        )
                .andDo(print());
    }

    @Test
    @Transactional
    public void testGetStatus() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .param("authName","merchant.auth")
                        .param("authPassword", "merchant.password")
                        .param("amount", "1000")
                        .param("sessionId", UUID.randomUUID().toString())
                        .param("deviceId","11111-2222-333")
                        )
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);
        System.out.println("orderId = " + orderId);

        mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .param("authName","merchant.auth")
                .param("authPassword", "merchant.password")
                .param("orderId", orderId))
                .andDo(print());
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testCardPayment() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .param("authName","merchant.auth")
                        .param("authPassword", "merchant.password")
                        .param("amount", "1000")
                        .param("sessionId", sessionId)
                        .param("deviceId", "11111-2222-333")
        )
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);

        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        Authentication authentication = new TestingAuthenticationToken(client, null);

        mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", orderId)
                        .param("paymentParams.pan",SSL_CARD)
                        .param("paymentParams.month", "12")
                        .param("paymentParams.year","2019")
                        .param("paymentParams.cardHolderName","test test")
                        .param("paymentParams.cvc","123")
                        .param("paymentWay","card")
        ).andDo(print());

        String getOrderStatusResponse = mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .param("authName", "merchant.auth")
                .param("authPassword", "merchant.password")
                .param("orderId", orderId)
                .param("externalRequest","true"))
                .andDo(print()).andReturn().getResponse().getContentAsString();

    }

    //localhost:9988/qr/order/register?authName=merchant.auth&authPassword=merchant.password&amount=1000&sessionId=12345678
    @Test
    @Transactional
    @Rollback(false)
    public void testTDSCardPayment() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .param("authName","merchant.auth")
                        .param("authPassword", "merchant.password")
                        .param("amount", "1000")
                        .param("sessionId", sessionId)
                        .param("deviceId", "11111-2222-333")
        )
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);

        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        Authentication authentication = new TestingAuthenticationToken(client, null);

        MvcResult mvcResultPayment =  mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", orderId)
                        .param("paymentParams.pan",TDS_CARD)
                        .param("paymentParams.month","12")
                        .param("paymentParams.year","2019")
                        .param("paymentParams.cardHolderName","test test")
                        .param("paymentParams.cvc","123")
                        .param("paymentWay","card")
        ).andDo(print()).andReturn();

        Map<String,Object> modelMap = mvcResultPayment.getModelAndView().getModel();
        assertNotNull(modelMap);
        assertTrue(modelMap.containsKey("acsUrl"));
        String acsUrl = (String)modelMap.get("acsUrl");
        String mdOrder = (String)modelMap.get("mdOrder");
        String paReq = (String)modelMap.get("paReq");
        String termUrl = (String)modelMap.get("termUrl");

        AcsRequest acsRequest = new AcsRequest(mdOrder, paReq, termUrl);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("MD",mdOrder);
        map.add("PaReq",paReq);
        map.add("TermUrl",termUrl);
        String acsResponse = restTemplate.postForObject(acsUrl, map, String.class);
        assertNotNull(acsResponse);
        System.out.println(acsResponse);
        /*

        String getOrderStatusResponse = mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .param("authName", "merchant.auth")
                .param("authPassword", "merchant.password")
                .param("orderId", orderId)
                .param("externalRequest","true"))
                .andDo(print()).andReturn().getResponse().getContentAsString();

        */

    }

    @Test
    @Rollback(false)
    @Transactional
    public void testPaymentBinding() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .param("authName","merchant.auth")
                        .param("authPassword", "merchant.password")
                        .param("amount", "1000")
                        .param("sessionId", sessionId)
                        .param("deviceId", "11111-2222-333")
        )
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);

        Client client = clientService.findByUsername("client");
        assertNotNull(client);

        Binding binding = bindingRepository.findByEnabled(true).stream().findFirst().orElse(null);
        if ( null == binding ) {
            return;
        }

        Authentication authentication = new TestingAuthenticationToken(client, null);

        mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(authentication)
                        .param("orderId", orderId)
                        .param("paymentParams.bindingId",binding.getBindingId())
                        .param("paymentParams.confirmValue", "123")
                        .param("paymentWay","binding")
        ).andDo(print());

        String getOrderStatusResponse = mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .param("authName", "merchant.auth")
                .param("authPassword", "merchant.password")
                .param("orderId", orderId)
                .param("externalRequest","true"))
                .andDo(print()).andReturn().getResponse().getContentAsString();

    }

    @Transactional
    @Rollback(false)
    @Test
    public void testReverse() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .param("authName","merchant.auth")
                        .param("authPassword", "merchant.password")
                        .param("amount", "1000")
                        .param("sessionId", sessionId)
                        .param("deviceId", "11111-2222-333")
        )
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);

        mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .param("orderId", orderId)
                        .param("paymentParams.pan","5555555555555599")
                        .param("paymentParams.month","12")
                        .param("paymentParams.year","2019")
                        .param("paymentParams.cardHolderName","test test")
                        .param("paymentParams.cvc","123")
                .param("paymentWay","CARD")
        ).andDo(print());

        String getOrderStatusResponse = mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .param("authName", "merchant.auth")
                .param("authPassword", "merchant.password")
                .param("orderId", orderId))
                .andDo(print()).andReturn().getResponse().getContentAsString();
        MerchantOrderStatusResponse merchantOrderStatusResponse = objectMapper.readValue(getOrderStatusResponse,MerchantOrderStatusResponse.class);
        assertNotNull(merchantOrderStatusResponse);
        if ( merchantOrderStatusResponse.getOrderStatus().equals(OrderStatus.PAID) ) {
            mockMvc.perform(post("/order/reverse")
                            .param("authName", "merchant.auth")
                            .param("authPassword", "merchant.password")
                            .param("sessionId",sessionId)
                            .param("orderId",orderId)
            ).andDo(print());
        }
        else {
            System.out.println("order not paid");
        }

    }

    @Test
    @Rollback(false)
    @Transactional
    public void testGetBindings() throws Exception {
        Client client = clientService.findByUsername("client");
        assertNotNull(client);
        Authentication authentication = new TestingAuthenticationToken(client, null);

        mockMvc.perform(get("/order/getBindings")
                .principal(authentication)
        //        .param("", "")
        )
                .andDo(print());
    }

    private class AcsRequest {
        String MD;
        String PaReq;
        String TermUrl;

        public AcsRequest(String MD, String paReq, String termUrl) {
            this.MD = MD;
            PaReq = paReq;
            TermUrl = termUrl;
        }
    }
}
