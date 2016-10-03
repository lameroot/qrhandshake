package ru.qrhandshake.qrpos.controller.it;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.api.PaymentResponse;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.TerminalRegisterResponse;
import ru.qrhandshake.qrpos.api.client.ClientOrderHistoryResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class ClientControllerTest extends ItTest {

    @Test
    public void testGetOrders() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("merchant_" + Util.generatePseudoUnique(8));
        TerminalRegisterResponse terminalRegisterResponse = registerTerminal(findUserByUsername(merchantRegisterResponse.getUserAuth()));
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + Util.generatePseudoUnique(8),"client", AuthType.PASSWORD);

        Client client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);
        int count = 13;
        for (int i = 0; i < count; i++) {
            MerchantOrderRegisterResponse merchantOrderRegisterResponse = registerOrder(terminalRegisterResponse.getAuth(),
                    amount,sessionId,deviceId, true);

            MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
            assertNotNull(merchantOrder);
            merchantOrder.setClient(client);
            merchantOrder.setOrderStatus(OrderStatus.PAID);
            merchantOrder.setPaymentDate(new Date());
            merchantOrderRepository.save(merchantOrder);

        }

        int sizePage = 10;
        MvcResult mvcResult = mockMvc.perform(get("/client/get_orders")
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
                        .param("page", "0")
                        .param("size", String.valueOf(sizePage))
        ).andDo(print()).andReturn();

        assertNotNull(mvcResult);
        assertNotNull(mvcResult.getResponse().getContentAsString());

        ClientOrderHistoryResponse clientOrderHistoryResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),ClientOrderHistoryResponse.class);
        assertNotNull(clientOrderHistoryResponse);
        assertEquals(sizePage,clientOrderHistoryResponse.getOrders().size());

        MvcResult mvcResult2 = mockMvc.perform(get("/client/get_orders")
                        .param("authName", clientRegisterResponse.getAuth().getAuthName())
                        .param("authPassword", clientRegisterResponse.getAuth().getAuthPassword())
                        .param("page", "1")
                        .param("size", String.valueOf(sizePage))
        ).andDo(print()).andReturn();

        assertNotNull(mvcResult2);
        assertNotNull(mvcResult2.getResponse().getContentAsString());

        ClientOrderHistoryResponse clientOrderHistoryResponse2 = objectMapper.readValue(mvcResult2.getResponse().getContentAsString(),ClientOrderHistoryResponse.class);
        assertNotNull(clientOrderHistoryResponse2);
        assertEquals(count - sizePage,clientOrderHistoryResponse2.getOrders().size());
    }
}
