package ru.qrhandshake.qrpos.controller.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;
import ru.qrhandshake.qrpos.ServletConfigTest;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResponse;
import ru.qrhandshake.qrpos.api.endpoint.UserPasswordEndpointCredentialsRequest;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderRegisterResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderStatusResponse;
import ru.qrhandshake.qrpos.controller.MerchantOrderController;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;
import ru.qrhandshake.qrpos.integration.IntegrationFacade;
import ru.qrhandshake.qrpos.integration.IntegrationService;
import ru.qrhandshake.qrpos.repository.*;
import ru.qrhandshake.qrpos.service.ClientService;
import ru.qrhandshake.qrpos.service.MerchantService;
import ru.rbs.mpi.test.acs.AcsUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by lameroot on 06.06.16.
 */
@Transactional
public abstract class ItTest extends ServletConfigTest {

    protected final static String SSL_CARD = "5555555555555599";
    protected final static String TDS_CARD = "4111111111111111";
    protected Long amount = 1000L;
    protected String sessionId = UUID.randomUUID().toString();
    protected String deviceId = UUID.randomUUID().toString();

    public static interface CardData {
        String getPan();
        String getMonth();
        String getYear();
        String getCardHolder();
        String getCvc();
        boolean needAcs();
        boolean isValid();
    }
    public static class TDSCardData implements CardData {
        @Override
        public String getPan() {
            return TDS_CARD;
        }

        @Override
        public String getMonth() {
            return "12";
        }

        @Override
        public String getYear() {
            return "2019";
        }

        @Override
        public String getCardHolder() {
            return "test test";
        }

        @Override
        public String getCvc() {
            return "123";
        }

        @Override
        public boolean needAcs() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
    public static class SSLCardData implements CardData {
        @Override
        public String getPan() {
            return SSL_CARD;
        }

        @Override
        public String getMonth() {
            return "12";
        }

        @Override
        public String getYear() {
            return "2019";
        }

        @Override
        public String getCardHolder() {
            return "test test";
        }

        @Override
        public String getCvc() {
            return "123";
        }

        @Override
        public boolean needAcs() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    @Resource
    protected ClientRepository clientRepository;
    @Resource
    protected MerchantRepository merchantRepository;
    @Resource
    protected UserRepository userRepository;
    @Resource
    protected ClientService clientService;
    @Resource
    protected TerminalRepository terminalRepository;
    @Resource
    protected ObjectMapper objectMapper;
    @Resource
    protected MerchantOrderRepository merchantOrderRepository;
    @Resource
    protected MerchantService merchantService;
    @Resource
    protected BindingRepository bindingRepository;
    @Autowired(required = false)
    protected Map<IntegrationSupport,IntegrationFacade> integrationFacades;
    @Resource
    protected IntegrationService integrationService;
    @Resource
    protected OrderTemplateHistoryRepository orderTemplateHistoryRepository;
    @Resource
    protected EndpointRepository endpointRepository;
    @Resource
    private EndpointCatalogRepository endpointCatalogRepository;

    protected void registerBinding(ClientRegisterResponse clientRegisterResponse, CardData cardData) throws Exception {
        Principal principalClient = clientTestingAuthenticationToken(clientRegisterResponse.getAuth());
        MvcResult mvcResultRegister = mockMvc.perform(get("/order/register_for_binding")
                        .principal(principalClient)
                        .param("amount", amount.toString())
                        .param("sessionId", sessionId)
                        .param("deviceId", deviceId)
        ).andDo(print()).andReturn();

        MerchantOrderRegisterResponse merchantOrderRegisterResponse = objectMapper.readValue(mvcResultRegister.getResponse().getContentAsString(), MerchantOrderRegisterResponse.class);
        assertNotNull(merchantOrderRegisterResponse);
        String orderId = merchantOrderRegisterResponse.getOrderId();
        assertNotNull(orderId);
        MerchantOrder merchantOrderByOrderId = merchantOrderRepository.findByOrderId(orderId);
        assertNotNull(merchantOrderByOrderId);
        Merchant rootMerchant = merchantService.findRootMerchant();
        assertNotNull(rootMerchant);
        assertEquals(rootMerchant.getId(), merchantOrderByOrderId.getMerchant().getId());

        MvcResult mvcResult = mockMvc.perform(post("/order" + MerchantOrderController.PAYMENT_PATH)
                        .principal(principalClient)
                        .param("orderId", merchantOrderRegisterResponse.getOrderId())
                        .param("pan", cardData.getPan())
                        .param("month", cardData.getMonth())
                        .param("year", cardData.getYear())
                        .param("cardHolderName", cardData.getCardHolder())
                        .param("cvc", cardData.getCvc())
                        .param("paymentWay", "card")
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult);

        PaymentResponse paymentResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), PaymentResponse.class);

        String finishUri = null;
        if ( cardData.needAcs() ) {
            assertTrue(paymentResponse.getStatus() == ResponseStatus.SUCCESS);
            ReturnUrlObject returnUrlObject = paymentResponse.getReturnUrlObject();
            assertNotNull(returnUrlObject);

            assertEquals("post", returnUrlObject.getAction());
            assertNotNull(returnUrlObject.getParams().get("MD"));
            assertNotNull(returnUrlObject.getParams().get("PaReq"));
            assertNotNull(returnUrlObject.getParams().get("TermUrl"));
            assertNotNull(returnUrlObject.getUrl());

            String paRes = AcsUtils.emulateCommunicationWithACS(returnUrlObject.getParams().get("MD"), returnUrlObject.getParams().get("TermUrl"), returnUrlObject.getParams().get("PaReq"), cardData.isValid());
            assertNotNull(paRes);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(returnUrlObject.getParams().get("TermUrl")
                    + "?PaRes=" + paRes
                    + "&MD=" + returnUrlObject.getParams().get("MD"), String.class);
            assertNotNull(responseEntity);
            System.out.println(responseEntity);
            assertEquals(302, responseEntity.getStatusCode().value());
            finishUri = "/order/finish/" + merchantOrderRegisterResponse.getOrderId() + "?orderId=" + returnUrlObject.getParams().get("MD");
            assertTrue(responseEntity.getHeaders().getLocation().toString().contains(finishUri));
        }
        else {
            assertTrue(cardData.isValid() ? paymentResponse.getStatus() == ResponseStatus.SUCCESS : paymentResponse.getStatus() == ResponseStatus.FAIL);
            if ( cardData.isValid() ) {
                ReturnUrlObject returnUrlObject = paymentResponse.getReturnUrlObject();
                assertNotNull(returnUrlObject);

                assertEquals("redirect", returnUrlObject.getAction());
                assertTrue(returnUrlObject.getUrl().contains("/finish/"));
                assertTrue(returnUrlObject.getUrl().contains(merchantOrderRegisterResponse.getOrderId()));
            }
            finishUri = "/order/finish/" + merchantOrderRegisterResponse.getOrderId();
        }

        MvcResult finishMvcResult = mockMvc.perform(get(finishUri))
                .andDo(print())
                .andReturn();
        assertNotNull(finishMvcResult);
        Map<String,Object> finishModel = finishMvcResult.getModelAndView().getModel();
        assertNotNull(finishModel);
        assertTrue(!finishModel.isEmpty());
        if ( cardData.isValid() ) {
            assertTrue(ResponseStatus.SUCCESS.equals(finishModel.get("status")));
            assertTrue(finishMvcResult.getResponse().getForwardedUrl().contains("finish"));
        }

        MerchantOrder merchantOrder = merchantOrderRepository.findByOrderId(merchantOrderRegisterResponse.getOrderId());
        assertNotNull(merchantOrder);
        if ( cardData.isValid() ) {
            assertTrue(merchantOrder.getOrderStatus() == OrderStatus.PAID);
            assertNotNull(merchantOrder.getPaymentDate());
            assertEquals(PaymentWay.CARD, merchantOrder.getPaymentWay());
            assertNotNull(merchantOrder.getClient());
            assertEquals(clientRegisterResponse.getAuth().getAuthName(), merchantOrder.getClient().getUsername());
        }
        else {
            assertTrue(merchantOrder.getOrderStatus() != OrderStatus.PAID);
        }

        return;
    }

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

        registerEndpoint(name, IntegrationSupport.RBS_SBRF, environment.getRequiredProperty("merchant.trans1.username"), environment.getRequiredProperty("merchant.trans1.password"));

        return merchantRegisterResponse;
    }

    EndpointRegisterResponse registerEndpoint(String merchantLogin, IntegrationSupport integrationSupport, String username, String password) throws Exception {
        Merchant root = merchantRepository.findOne(-1L);
        if ( null == root ) throw new IllegalArgumentException("Merchant with id: " + -1 + " unknown");
        Terminal terminal = terminalRepository.findByMerchant(root).stream().filter(t -> t.isEnabled()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Terminal invalid"));
        TestingAuthenticationToken terminalPrincipal = new TestingAuthenticationToken(terminal,null);
        EndpointCatalog endpointCatalog = endpointCatalogRepository.findByIntegrationSupport(integrationSupport);
        Assert.notNull(endpointCatalog,"Invalid integration: " + integrationSupport);

        Merchant merchant = merchantRepository.findByName(merchantLogin);
        assertNotNull(merchant);

        UserPasswordEndpointCredentialsRequest userPasswordEndpointCredentialsRequest = new UserPasswordEndpointCredentialsRequest(username,password);
        String userPasswordJson = objectMapper.writeValueAsString(userPasswordEndpointCredentialsRequest);

        MvcResult mvcResult = mockMvc.perform(post("/endpoint/register")
                .principal(terminalPrincipal)
                .param("endpointCatalogId", String.valueOf(endpointCatalog.getId()))
                .param("merchantId", String.valueOf(merchant.getId()))
                .param("credentials", userPasswordJson)
        ).andDo(print()).andReturn();
        assertNotNull(mvcResult);
        String response = mvcResult.getResponse().getContentAsString();
        assertNotNull(response);
        EndpointRegisterResponse endpointRegisterResponse = objectMapper.readValue(response, EndpointRegisterResponse.class);
        assertNotNull(endpointRegisterResponse);
        assertNotNull(endpointRegisterResponse.getEndpointId());
        assertEquals(ResponseStatus.SUCCESS,endpointRegisterResponse.getStatus());

        return endpointRegisterResponse;

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
                .param("authType", null != authType ? authType.name() : "")
                )
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
        return registerOrder(terminalAuth, amount, sessionId, deviceId, false);
    }

    protected MerchantOrderRegisterResponse registerOrder(ApiAuth terminalAuth, Long amount, String sessionId, String deviceId, boolean useApiAuth) throws Exception {
        ApiResponse apiResponse = authTerminal(terminalAuth.getAuthName(),terminalAuth.getAuthPassword());
        assertTrue(apiResponse.getStatus() == ResponseStatus.SUCCESS);
        MvcResult mvcResult = null;
        if ( useApiAuth ) {
            mvcResult = mockMvc.perform(get("/order/register")
                            .param("authName",terminalAuth.getAuthName())
                            .param("authPassword", terminalAuth.getAuthPassword())
                            .param("amount", amount.toString())
                            .param("sessionId", sessionId)
                            .param("deviceId", deviceId)
            )
                    .andDo(print())
                    .andReturn();
        }
        else {
            mvcResult = mockMvc.perform(get("/order/register")
                            .principal(terminalTestingAuthenticationToken(terminalAuth))
                            .param("amount", amount.toString())
                            .param("sessionId", sessionId)
                            .param("deviceId", deviceId)
            )
                    .andDo(print())
                    .andReturn();
        }
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
