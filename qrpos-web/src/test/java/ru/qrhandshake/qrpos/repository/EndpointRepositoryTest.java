package ru.qrhandshake.qrpos.repository;


import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.domain.*;
import ru.qrhandshake.qrpos.integration.rbs.RbsIntegrationConfig;

import javax.annotation.Resource;
import java.util.List;

@ActiveProfiles(value = RbsIntegrationConfig.RBS_PROFILE)
@Sql(scripts = {"classpath:sql/postgres/migration/migration-endpoint_catalog.sql"})
public class EndpointRepositoryTest extends GeneralTest {

    @Resource
    private EndpointRepository endpointRepository;
    @Resource
    private EndpointCatalogRepository endpointCatalogRepository;

    @Test
    public void testCreate() throws Exception {
        Merchant merchant = merchantRepository.findByName("test_merchant");
        assertNotNull(merchant);

        EndpointCatalog endpointCatalog = endpointCatalogRepository.findByIntegrationSupport(IntegrationSupport.RBS_SBRF);

        UserPasswordEndpoint endpoint = new UserPasswordEndpoint();
        endpoint.setMerchant(merchant);
        endpoint.setEnabled(true);
        endpoint.setUsername("qr-api2");
        endpoint.setPassword("qr");
        endpoint.setEndpointCatalog(endpointCatalog);

        endpointRepository.save(endpoint);


    }

    @Test
    public void testFindByMerchantAndIntegrationSupport() throws Exception {
        Merchant merchant = merchantRepository.findByName("test_merchant");
        assertNotNull(merchant);

        Endpoint endpoint = endpointRepository.findByMerchantAndIntegrationSupport(merchant, IntegrationSupport.RBS_SBRF);
        System.out.println(endpoint);
    }
}
