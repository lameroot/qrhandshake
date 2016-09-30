package ru.qrhandshake.qrpos.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.GeneralTest;
import ru.qrhandshake.qrpos.api.client.ClientConfirmRequest;
import ru.qrhandshake.qrpos.api.client.ClientConfirmResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterRequest;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.service.confirm.MailConfirmService;
import ru.qrhandshake.qrpos.service.mail.MailSender;
import ru.qrhandshake.qrpos.service.mail.MailSenderException;

import javax.annotation.Resource;

@Transactional
public class ClientServiceTest extends GeneralTest {

    @Resource
    private ClientService clientService;
    @Resource
    private ClientRepository clientRepository;
    @Resource
    private MailConfirmService mailConfirmService;
    @Resource
    private SecurityService securityService;
    private MailSender mailSender = Mockito.mock(MailSender.class);

    @Test
    public void testRegisterViaPassword() {
        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setAuthType(AuthType.PASSWORD);
        clientRegisterRequest.setAuthName("client");
        clientRegisterRequest.setAuthPassword("password");

        ClientRegisterResponse clientRegisterResponse = clientService.register(clientRegisterRequest);
        assertNotNull(clientRegisterResponse);
        assertTrue(clientRegisterResponse.getStatus().equals(ResponseStatus.SUCCESS));

        Client client = clientRepository.findByUsername(clientRegisterRequest.getAuthName());
        assertNotNull(client);
        assertTrue(client.isEnabled());
        assertNotNull(client.getPassword());
    }

    @Test
    public void testRegisterViaEmailAndSuccessConfirm() throws MailSenderException {
        ReflectionTestUtils.setField(mailConfirmService,"mailSender",mailSender);
        doNothing().when(mailSender).send(anyObject());

        String password = "password";
        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setAuthType(AuthType.EMAIL);
        clientRegisterRequest.setAuthName("client@mail.ru");
        clientRegisterRequest.setAuthPassword(password);
        clientRegisterRequest.setConfirm(true);

        ClientRegisterResponse clientRegisterResponse = clientService.register(clientRegisterRequest);
        assertNotNull(clientRegisterResponse);
        assertTrue(clientRegisterResponse.getStatus().equals(ResponseStatus.SUCCESS));

        Client client = clientRepository.findByUsername(clientRegisterRequest.getAuthName());
        assertNotNull(client);
        assertTrue(!client.isEnabled());
        assertNull(client.getPassword());
        assertNotNull(client.getConfirmCode());

        //change password
        password = "password_changed";
        ClientConfirmRequest clientConfirmRequest = new ClientConfirmRequest();
        clientConfirmRequest.setAuthType(AuthType.EMAIL);
        clientConfirmRequest.setAuthName("client@mail.ru");
        clientConfirmRequest.setAuthPassword(password);
        clientConfirmRequest.setConfirmCode(client.getConfirmCode());

        ClientConfirmResponse clientConfirmResponse = clientService.confirm(clientConfirmRequest);
        assertNotNull(clientConfirmResponse);
        assertEquals(ResponseStatus.SUCCESS,clientConfirmResponse.getStatus());

        client = clientRepository.findByUsername(clientConfirmRequest.getAuthName());
        assertNotNull(client);
        assertTrue(client.isEnabled());
        assertNotNull(client.getPassword());
        assertNull(client.getConfirmCode());

        assertTrue(securityService.match(password,client.getPassword()));
    }

    @Test
    public void testRegisterViaPhoneAndConfirm() throws MailSenderException {
        ReflectionTestUtils.setField(mailConfirmService,"mailSender",mailSender);
        doNothing().when(mailSender).send(anyObject());

        String authName = "+79267787787";
        String password = "password";
        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setAuthType(AuthType.PHONE);
        clientRegisterRequest.setAuthName(authName);
        clientRegisterRequest.setAuthPassword(password);
        clientRegisterRequest.setConfirm(true);

        ClientRegisterResponse clientRegisterResponse = clientService.register(clientRegisterRequest);
        assertNotNull(clientRegisterResponse);
        assertTrue(clientRegisterResponse.getStatus().equals(ResponseStatus.SUCCESS));

        Client client = clientRepository.findByUsername(clientRegisterRequest.getAuthName());
        assertNotNull(client);
        assertTrue(!client.isEnabled());
        assertNull(client.getPassword());
        assertNotNull(client.getConfirmCode());

        //change password
        password = "password_changed";
        ClientConfirmRequest clientConfirmRequest = new ClientConfirmRequest();
        clientConfirmRequest.setAuthType(AuthType.EMAIL);
        clientConfirmRequest.setAuthName(authName);
        clientConfirmRequest.setAuthPassword(password);
        clientConfirmRequest.setConfirmCode(client.getConfirmCode());

        ClientConfirmResponse clientConfirmResponse = clientService.confirm(clientConfirmRequest);
        assertNotNull(clientConfirmResponse);
        assertEquals(ResponseStatus.SUCCESS,clientConfirmResponse.getStatus());

        client = clientRepository.findByUsername(clientConfirmRequest.getAuthName());
        assertNotNull(client);
        assertTrue(client.isEnabled());
        assertNotNull(client.getPassword());
        assertNull(client.getConfirmCode());

        assertTrue(securityService.match(password,client.getPassword()));
    }

    @Test
    @Ignore
    public void testRegisterViaPhoneReal() throws MailSenderException {
        String authName = "+79267787787";
        String password = "password";
        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setAuthType(AuthType.PHONE);
        clientRegisterRequest.setAuthName(authName);
        clientRegisterRequest.setAuthPassword(password);
        clientRegisterRequest.setConfirm(true);

        ClientRegisterResponse clientRegisterResponse = clientService.register(clientRegisterRequest);
        assertNotNull(clientRegisterResponse);
        assertTrue(clientRegisterResponse.getStatus().equals(ResponseStatus.SUCCESS));

        Client client = clientRepository.findByUsername(clientRegisterRequest.getAuthName());
        assertNotNull(client);
        assertTrue(!client.isEnabled());
        assertNull(client.getPassword());
        assertNotNull(client.getConfirmCode());
    }


}
