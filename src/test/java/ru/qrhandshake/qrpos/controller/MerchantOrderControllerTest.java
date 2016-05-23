package ru.qrhandshake.qrpos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.dto.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.service.MerchantService;

import javax.annotation.Resource;

import java.io.StringReader;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by lameroot on 23.05.16.
 */
public class MerchantOrderControllerTest extends ServletConfigTest {

    @Resource
    private MerchantService merchantService;

    private final static String MERCHANT_LOGIN = "merchant";

    @Test
    public void testRegister() throws Exception {
        if ( null == merchantService.loadUserByUsername(MERCHANT_LOGIN) ) {
            MerchantDto merchantDto = new MerchantDto();
            merchantDto.setUsername(MERCHANT_LOGIN);
            merchantDto.setPassword("password");
            merchantDto.setName("name");
            Merchant merchant = merchantService.create(merchantDto);
            assertNotNull(merchant);
        }

        mockMvc.perform(get(MerchantOrderController.REGISTER_PATH)
                .param("login","merchant")
                .param("password","password")
                .param("amount", "1000"))
            .andDo(print());
    }

    @Test
    @Transactional
    public void testGetStatus() throws Exception {
        if ( null == merchantService.loadUserByUsername(MERCHANT_LOGIN) ) {
            MerchantDto merchantDto = new MerchantDto();
            merchantDto.setUsername(MERCHANT_LOGIN);
            merchantDto.setPassword("password");
            merchantDto.setName("name");
            Merchant merchant = merchantService.create(merchantDto);
            assertNotNull(merchant);
        }

        MvcResult mvcResult = mockMvc.perform(get(MerchantOrderController.REGISTER_PATH)
                .param("login", MERCHANT_LOGIN)
                .param("password", "password")
                .param("amount", "1000"))
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);
        System.out.println("orderId = " + orderId);

        mockMvc.perform(get(MerchantOrderController.ORDER_STATUS_PATH)
                .param("login", MERCHANT_LOGIN)
                .param("password", "password")
                .param("orderId", orderId))
                .andDo(print());
    }

    @Test
    @Transactional
    public void testPayment() throws Exception {
        if ( null == merchantService.loadUserByUsername(MERCHANT_LOGIN) ) {
            MerchantDto merchantDto = new MerchantDto();
            merchantDto.setUsername(MERCHANT_LOGIN);
            merchantDto.setPassword("password");
            merchantDto.setName("name");
            Merchant merchant = merchantService.create(merchantDto);
            assertNotNull(merchant);
        }

        MvcResult mvcResult = mockMvc.perform(get(MerchantOrderController.REGISTER_PATH)
                .param("login", MERCHANT_LOGIN)
                .param("password", "password")
                .param("amount", "1000"))
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);

        mockMvc.perform(post(MerchantOrderController.PAYMENT_PATH)
            .param("orderId", orderId)
            .param("pan","5555555555555599")
                .param("month","12")
                .param("year","2019")
                .param("cardHolderName","test test")
                .param("cvc","123")
        ).andDo(print());

    }
}
