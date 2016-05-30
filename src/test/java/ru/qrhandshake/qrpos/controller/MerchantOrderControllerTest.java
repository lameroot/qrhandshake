package ru.qrhandshake.qrpos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.api.MerchantOrderStatusResponse;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.api.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.service.MerchantService;
import ru.qrhandshake.qrpos.service.UserService;

import javax.annotation.Resource;

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

    private final static String MERCHANT_LOGIN = "merchant";
    private final static String MERCHANT_PASSWORD = "password";

    @Test
    public void testRegister1() throws Exception {

    }

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
    public void testPayment() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .param("authName","merchant.auth")
                        .param("authPassword", "merchant.password")
                        .param("amount", "1000")
                        .param("sessionId", UUID.randomUUID().toString())
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
            .param("pan","5555555555555599")
                .param("month","12")
                .param("year","2019")
                .param("cardHolderName","test test")
                .param("cvc","123")
        ).andDo(print());

        mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .param("authName","merchant.auth")
                .param("authPassword", "merchant.password")
                .param("orderId", orderId))
                .andDo(print());

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
                .param("paymentWay","card")
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
}
