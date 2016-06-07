package ru.qrhandshake.qrpos.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;
import ru.qrhandshake.qrpos.service.ClientService;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by lameroot on 06.06.16.
 */
public class ItTest extends ServletConfigTest {

    @Resource
    protected MerchantRepository merchantRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private ClientService clientService;
    @Resource
    private TerminalRepository terminalRepository;
    @Resource
    protected ObjectMapper objectMapper;

    protected MerchantRegisterResponse registerMerchant(String name) throws Exception {
        Merchant merchant = merchantRepository.findByName(name);
        if ( null != merchant ) throw new IllegalArgumentException("Merchant with name: " + name + " already exists");

        MvcResult mvcResult = mockMvc.perform(post("/merchant/register")
                .param("authName", name + ".authName")
                .param("authPassword", name + ".authPassword")
                .param("name", name))
                .andDo(print()).andReturn();
        assertNotNull(mvcResult);
        String response = mvcResult.getResponse().getContentAsString();
        assertNotNull(response);
        MerchantRegisterResponse merchantRegisterResponse = objectMapper.readValue(response, MerchantRegisterResponse.class);
        assertTrue(ResponseStatus.SUCCESS.equals(merchantRegisterResponse.getStatus()));
        assertNotNull(merchantRegisterResponse.getAuth());
        assertTrue(merchantRegisterResponse.getAuth().authIsNotBlank());
        assertNotNull(merchantRegisterResponse.getUserAuth());
        assertTrue(merchantRegisterResponse.getUserAuth().authIsNotBlank());
        assertNotNull(merchantRegisterResponse.getTerminalAuth());
        assertTrue(merchantRegisterResponse.getTerminalAuth().authIsNotBlank());

        return merchantRegisterResponse;
    }

    protected User findUserByUsername(ApiAuth userAuth) {
        assertNotNull(userAuth);
        User user = userRepository.findByUsername(userAuth.getAuthName());
        assertNotNull(user);
        return user;
    }

    protected Client findClientByUsername(ApiAuth clientAuth) {
        assertNotNull(clientAuth);
        Client client = clientService.auth(clientAuth);
        assertNotNull(client);
        return client;
    }

    protected TestingAuthenticationToken clientTestingAuthenticationToken(ApiAuth clientApiAuth) {
        return new TestingAuthenticationToken(findClientByUsername(clientApiAuth),null);
    }


    protected TestingAuthenticationToken terminalTestingAuthenticationToken(ApiAuth terminalApiAuth) {
        Terminal terminal = terminalRepository.findByAuthName(terminalApiAuth.getAuthName());
        assertNotNull(terminal);
        return new TestingAuthenticationToken(terminal,null);
    }

    protected ClientRegisterResponse registerClient(String username, String password, AuthType authType) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/client/register")
                .param("authName", username)
                .param("authPassword", password)
                .param("authType", null != authType ? authType.name() : ""))
                .andDo(print()).andReturn();
        assertNotNull(mvcResult);
        String response = mvcResult.getResponse().getContentAsString();
        assertNotNull(response);
        ClientRegisterResponse clientRegisterResponse = objectMapper.readValue(response,ClientRegisterResponse.class);
        assertNotNull(clientRegisterResponse);
        assertTrue(ResponseStatus.SUCCESS == clientRegisterResponse.getStatus());
        assertNotNull(clientRegisterResponse.getAuth());

        return clientRegisterResponse;
    }

    protected TerminalRegisterResponse registerTerminal(User user) throws Exception {
        assertNotNull(user);
        Authentication authentication = new TestingAuthenticationToken(user, null);
        MvcResult mvcResult = mockMvc.perform(post("/terminal/register")
                .principal(authentication)
                ).andDo(print()).andReturn();
        assertNotNull(mvcResult);
        String response = mvcResult.getResponse().getContentAsString();
        assertNotNull(response);
        TerminalRegisterResponse terminalRegisterResponse = objectMapper.readValue(response,TerminalRegisterResponse.class);
        assertNotNull(terminalRegisterResponse);
        assertTrue(ResponseStatus.SUCCESS == terminalRegisterResponse.getStatus());
        assertTrue(user.getMerchant().getMerchantId().equals(terminalRegisterResponse.getMerchantId()));
        assertNotNull(terminalRegisterResponse.getAuth());

        return terminalRegisterResponse;
    }

    protected ApiResponse authTerminal(String authName, String authPassword) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/terminal/auth")
                .param("authName", authName)
                .param("authPassword", authPassword))
                .andDo(print()).andReturn();
        assertNotNull(mvcResult);
        String response = mvcResult.getResponse().getContentAsString();
        assertNotNull(response);
        ApiResponse apiResponse = objectMapper.readValue(response,ApiResponse.class);
        assertNotNull(apiResponse);
        return apiResponse;
    }

    protected ApiResponse authClient(String authName, String authPassword) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/client/auth")
                .param("authName", authName)
                .param("authPassword", authPassword))
                .andDo(print()).andReturn();
        assertNotNull(mvcResult);
        String response = mvcResult.getResponse().getContentAsString();
        assertNotNull(response);
        ApiResponse apiResponse = objectMapper.readValue(response,ApiResponse.class);
        assertNotNull(apiResponse);
        return apiResponse;
    }

    protected MerchantOrderRegisterResponse registerOrder(ApiAuth terminalAuth, Long amount, String sessionId, String deviceId) throws Exception {
        ApiResponse apiResponse = authTerminal(terminalAuth.getAuthName(),terminalAuth.getAuthPassword());
        assertTrue(apiResponse.getStatus() == ResponseStatus.SUCCESS);
        MvcResult mvcResult = mockMvc.perform(get("/order/register")
                        .principal(terminalTestingAuthenticationToken(terminalAuth))
                        .param("amount", amount.toString())
                        .param("sessionId", sessionId)
                        .param("deviceId", deviceId)
        )
                .andDo(print())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(response, MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);
        assertTrue(ResponseStatus.SUCCESS == merchantOrderRegisterResponse.getStatus());

        return merchantOrderRegisterResponse;
    }

    protected MerchantOrderStatusResponse getOrderStatus(ApiAuth terminalAuth, String orderId, boolean externalRequest) throws Exception {
        String getOrderStatusResponse = mockMvc.perform(get("/order" + MerchantOrderController.ORDER_STATUS_PATH)
                .principal(terminalTestingAuthenticationToken(terminalAuth))
                .param("orderId", orderId)
                .param("externalRequest",Boolean.toString(externalRequest)))
                .andDo(print()).andReturn().getResponse().getContentAsString();
        assertNotNull(getOrderStatusResponse);
        MerchantOrderStatusResponse merchantOrderStatusResponse = objectMapper.readValue(getOrderStatusResponse,MerchantOrderStatusResponse.class);
        assertNotNull(merchantOrderStatusResponse);
        assertTrue(ResponseStatus.SUCCESS == merchantOrderStatusResponse.getStatus());

        return merchantOrderStatusResponse;
    }
}
