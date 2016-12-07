package ru.qrhandshake.qrpos.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.integration.IntegrationPaymentRequest;
import ru.qrhandshake.qrpos.repository.EndpointRepository;
import ru.qrhandshake.qrpos.util.HibernateUtils;
import ru.rbs.commons.cluster.retry.Retriable;
import ru.rbs.commons.cluster.retry.RetriableExecutor;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lameroot on 25.10.16.
 */
@ActiveProfiles(value = {"prod","rbs"})
public class RetryConfigTest extends GeneralTest {

    @Resource
    private RetriableExecutor retriableExecutor;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private EndpointRepository endpointRepository;

    @Test
    @Transactional
    public void testSerializer() throws JsonProcessingException {
        Merchant merchant = new Merchant();
        merchant.setName("test");
        Terminal terminal1 = new Terminal();
        terminal1.setMerchant(merchant);
        terminal1.setAuthName("login1");
        Terminal terminal2 = new Terminal();
        terminal2.setMerchant(merchant);
        terminal2.setAuthName("login2");

        Set<Terminal> terminals = new HashSet<>();
        terminals.add(terminal1);
        terminals.add(terminal2);


        String s = objectMapper.writeValueAsString(merchant);
        System.out.println(s);

        System.out.println(objectMapper.writeValueAsString(terminal1));

//        UserPasswordEndpoint userPasswordEndpoint = new UserPasswordEndpoint();
//        userPasswordEndpoint.setPassword("pass");
//        userPasswordEndpoint.setMerchant(merchant);

//        Merchant merchant1 = merchantRepository.findOne(-1L);
//        System.out.println(merchant1);

        Endpoint endpoint = endpointRepository.findOne(-1L);
        assertNotNull(endpoint);
        System.out.println(endpoint.getClass());
        endpoint.setEndpointCatalog(HibernateUtils.initializeAndUnproxy(endpoint.getEndpointCatalog()));
        endpoint.setMerchant(HibernateUtils.initializeAndUnproxy(endpoint.getMerchant()));
        Merchant merchant1 = merchantRepository.findOne(endpoint.getMerchant().getId());
        Merchant merchantReal = HibernateUtils.initializeAndUnproxy(merchant1);
        System.out.println(merchant1);
        System.out.println(merchantReal);

        IntegrationPaymentRequest integrationPaymentRequest = new IntegrationPaymentRequest();
        integrationPaymentRequest.setEndpoint(endpoint);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        System.out.println(mapper.writeValueAsString(endpoint));
        System.out.println(mapper.writeValueAsString(integrationPaymentRequest));

        retriableExecutor.execute(integrationPaymentRequest, new Retriable<IntegrationPaymentRequest>() {
            @Override
            public void execute(IntegrationPaymentRequest data) {
                System.out.println("hello");
            }

            @Override
            public Date next(int attemptNumber) {
                return null;
            }

            @Override
            public int maxAttempts() {
                return 0;
            }
        });

    }


}
