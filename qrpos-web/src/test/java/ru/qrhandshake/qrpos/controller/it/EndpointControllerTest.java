package ru.qrhandshake.qrpos.controller.it;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.endpoint.EndpointRegisterResponse;
import ru.qrhandshake.qrpos.domain.Endpoint;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 24.09.16.
 */
@Transactional
public class EndpointControllerTest extends ItTest {

    @Test
    public void testRegisterEndpoint() throws Exception {
        MerchantRegisterResponse merchantRegisterResponse = registerMerchant("rollback_merchant");
        EndpointRegisterResponse endpointRegisterResponse = registerEndpoint("rollback_merchant", IntegrationSupport.RBS_SBRF, "username", "password");

        Endpoint endpoint = endpointRepository.findOne(endpointRegisterResponse.getEndpointId());
        assertNotNull(endpoint);
    }

    @Test
    public void testRegisterMerchant() throws Exception {
        registerMerchant("test_merchant2");
    }
}
