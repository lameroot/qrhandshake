package ru.qrhandshake.qrpos.controller;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.api.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.OrderTemplateResponse;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.OrderTemplate;
import ru.qrhandshake.qrpos.domain.Terminal;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Created by lameroot on 08.08.16.
 */
@Transactional
@Rollback
public class OrderTemplateControllerTest extends ServletConfigTest {

    @Test
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

        MvcResult orderTemplateMvcResult = mockMvc.perform(post("/order_template/create")
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
}
