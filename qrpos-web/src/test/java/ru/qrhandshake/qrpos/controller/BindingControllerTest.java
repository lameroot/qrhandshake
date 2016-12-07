package ru.qrhandshake.qrpos.controller;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.binding.BindingDeleteResponse;
import ru.qrhandshake.qrpos.api.binding.CardBindingCreateResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.controller.it.ItTest;
import ru.qrhandshake.qrpos.domain.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * User: Krainov
 * Date: 06.10.2016
 * Time: 12:33
 */
public class BindingControllerTest extends ItTest {

    @Deprecated
    public void testCreateBinding() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + UUID.randomUUID().toString(),"password",AuthType.PASSWORD);
        Client client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);
        Principal clientPrincipal = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        MvcResult mvcResult = mockMvc.perform(post("/binding")
                        .principal(clientPrincipal)
                        .param("paymentWay", "card")
                        .param("pan", "4111111111111111")
                        .param("month", "12")
                        .param("year", "2019")
                        .param("cardHolderName", "test test")
                        .param("cvc", "123")
        ).andDo(print()).andReturn();

        CardBindingCreateResponse cardBindingCreateResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CardBindingCreateResponse.class);
        assertNotNull(cardBindingCreateResponse);
        assertEquals(ResponseStatus.SUCCESS, cardBindingCreateResponse.getStatus());

    }

    @Test
    public void testDelete() throws Exception {
        ClientRegisterResponse clientRegisterResponse = registerClient("client_" + UUID.randomUUID().toString(),"password",AuthType.PASSWORD);
        Client client = clientRepository.findByUsername(clientRegisterResponse.getAuth().getAuthName());
        assertNotNull(client);

        int count = 10;
        List<Binding> newBindings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Binding binding = new Binding();
            binding.setBindingId(UUID.randomUUID().toString());
            binding.setEnabled(true);
            binding.setClient(client);
            binding.setCreatedDate(new Date());
            binding.setExternalBindingId(UUID.randomUUID().toString());
            binding.setIntegrationSupport(IntegrationSupport.RBS_SBRF);
            binding.setOrderId(UUID.randomUUID().toString());
            binding.setPaymentParams("{\"type\":\"cardPaymentParams\",\"paymentAccount\":\"411111**1111\",\"month\":\"12\",\"year\":\"2019\",\"cardHolderName\":\"this i\"}");
            binding.setPaymentSecureType(PaymentSecureType.TDS);
            binding.setPaymentWay(PaymentWay.CARD);

            bindingRepository.save(binding);
            newBindings.add(binding);
        }
        List<Binding> bindings = bindingRepository.findByClientAndEnabled(client, true);
        assertEquals(count,bindings.size());

        Principal clientPrincipal = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());

        boolean returnNewBindingList = true;
        MvcResult mvcResult = mockMvc.perform(post("/binding/delete")
                        .principal(clientPrincipal)
                        .param("bindingId", newBindings.get(0).getBindingId())
                        .param("returnNewBindingList",returnNewBindingList ? "true" : "false")
        ).andDo(print()).andReturn();
        BindingDeleteResponse bindingDeleteResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),BindingDeleteResponse.class);
        assertNotNull(bindingDeleteResponse);
        assertEquals(ResponseStatus.SUCCESS, bindingDeleteResponse.getStatus());
        if ( returnNewBindingList ) {
            assertEquals(count - 1, bindingDeleteResponse.getBindings().size());
            assertFalse(bindingDeleteResponse.getBindings().stream().filter(b -> b.getBindingId().equals(newBindings.get(0).getBindingId())).findFirst().isPresent());
        }

        assertEquals(count-1,bindingRepository.findByClient(client).size());
    }
}
