package ru.qrhandshake.qrpos.repository;


import org.junit.Test;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;

import javax.annotation.Resource;
import java.util.List;

@ActiveProfiles(value = RbsIntegrationConfig.RBS_PROFILE)
public class EndpointRepositoryTest extends GeneralTest {

    @Resource
    private EndpointRepository endpointRepository;
    @Resource
    private EndpointCatalogRepository endpointCatalogRepository;

    @Test
    @Rollback
    @Transactional
    public void testCreate() throws Exception {


        Merchant merchant = merchantRepository.findByName("merchant");
        assertNotNull(merchant);

        EndpointCatalog endpointCatalog = endpointCatalogRepository.findByIntegrationSupport(IntegrationSupport.RBS_SBRF);

        UserPasswordEndpoint endpoint = new UserPasswordEndpoint();
        endpoint.setMerchant(merchant);
        endpoint.setEnabled(true);
        endpoint.setUsername("qr-api2");
        endpoint.setPassword("qr");
        endpoint.setEndpointCatalog(endpointCatalog);
//
        endpointRepository.save(endpoint);

        Endpoint endpoint1 = endpointRepository.findByMerchantAndIntegrationSupport(merchant, IntegrationSupport.RBS_SBRF);
        assertNotNull(endpoint1);
        System.out.println(endpoint1);


    }

    @Test
    public void testFindByMerchantAndIntegrationSupport() throws Exception {
        Merchant merchant = merchantRepository.findByName("test_merchant");
        assertNotNull(merchant);

        Endpoint endpoint = endpointRepository.findByMerchantAndIntegrationSupport(merchant, IntegrationSupport.RBS_SBRF);
        System.out.println(endpoint);
    }
}
