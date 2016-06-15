package ru.qrhandshake.qrpos.service;

import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.ClientRegisterRequest;
import ru.qrhandshake.qrpos.api.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.ResponseStatus;

import javax.annotation.Resource;

/**
 * Created by lameroot on 01.06.16.
 */
public class ClientServiceTest extends GeneralTest {

    @Resource
    private ClientService clientService;

    @Test
    public void testRegister() {
        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setAuthName("client");
        clientRegisterRequest.setAuthPassword("password");

        ClientRegisterResponse clientRegisterResponse = clientService.register(clientRegisterRequest);
        assertNotNull(clientRegisterResponse);
        assertTrue(clientRegisterResponse.getStatus().equals(ResponseStatus.SUCCESS));
        System.out.println(clientRegisterResponse.getMessage());
    }

}
