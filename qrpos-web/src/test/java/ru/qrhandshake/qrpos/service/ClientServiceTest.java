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
import ru.qrhandshake.qrpos.domain.Confirm;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.repository.ConfirmRepository;
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
    @Resource
    private ConfirmRepository confirmRepository;

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
        Confirm confirm = confirmRepository.findByClientAndAuthType(client, AuthType.EMAIL);
        assertNotNull(confirm);
        assertTrue(confirm.isEnabled());
        assertNotNull(confirm.getCode());

        //change password
        password = "password_changed";
        ClientConfirmRequest clientConfirmRequest = new ClientConfirmRequest();
        clientConfirmRequest.setAuthType(AuthType.EMAIL);
        clientConfirmRequest.setAuthName("client@mail.ru");
        clientConfirmRequest.setAuthPassword(password);
        clientConfirmRequest.setConfirmCode(confirm.getCode());

        ClientConfirmResponse clientConfirmResponse = clientService.confirm(clientConfirmRequest);
        assertNotNull(clientConfirmResponse);
        assertEquals(ResponseStatus.SUCCESS,clientConfirmResponse.getStatus());

        client = clientRepository.findByUsername(clientConfirmRequest.getAuthName());
        assertNotNull(client);
        assertTrue(client.isEnabled());
        assertNotNull(client.getPassword());
        confirm = confirmRepository.findByClientAndAuthType(client,AuthType.EMAIL);
        assertNull(confirm);

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
        Confirm confirm = confirmRepository.findByClientAndAuthType(client, AuthType.PHONE);
        assertNotNull(confirm);
        assertTrue(confirm.isEnabled());
        assertNotNull(confirm.getCode());

        //change password
        password = "password_changed";
        ClientConfirmRequest clientConfirmRequest = new ClientConfirmRequest();
        clientConfirmRequest.setAuthType(AuthType.PHONE);
        clientConfirmRequest.setAuthName(authName);
        clientConfirmRequest.setAuthPassword(password);
        clientConfirmRequest.setConfirmCode(confirm.getCode());

        ClientConfirmResponse clientConfirmResponse = clientService.confirm(clientConfirmRequest);
        assertNotNull(clientConfirmResponse);
        assertEquals(ResponseStatus.SUCCESS,clientConfirmResponse.getStatus());

        client = clientRepository.findByUsername(clientConfirmRequest.getAuthName());
        assertNotNull(client);
        assertTrue(client.isEnabled());
        assertNotNull(client.getPassword());
        confirm = confirmRepository.findByClientAndAuthType(client,AuthType.PHONE);
        assertNull(confirm);

        assertTrue(securityService.match(password,client.getPassword()));
    }

    @Test
    public void testMaxAttempts() throws Exception {
        ReflectionTestUtils.setField(mailConfirmService,"mailSender",mailSender);
        doNothing().when(mailSender).send(anyObject());
        ReflectionTestUtils.setField(clientService,"maxConfirmAttempt",3);

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
        Confirm confirm = confirmRepository.findByClientAndAuthType(client, AuthType.PHONE);
        assertNotNull(confirm);
        assertTrue(confirm.isEnabled());
        assertNotNull(confirm.getCode());

        Integer maxAttempts = environment.getProperty("confirm.attempt.max",Integer.class, 3);

        ClientConfirmResponse clientConfirmResponse = null;
        for (int i = 0; i <= maxAttempts; i++) {
            //change password
            password = "password_changed";
            ClientConfirmRequest clientConfirmRequest = new ClientConfirmRequest();
            clientConfirmRequest.setAuthType(AuthType.PHONE);
            clientConfirmRequest.setAuthName(authName);
            clientConfirmRequest.setAuthPassword(password);
            clientConfirmRequest.setConfirmCode(confirm.getCode() + "_invalid");

            clientConfirmResponse = clientService.confirm(clientConfirmRequest);
            assertEquals(ResponseStatus.FAIL,clientConfirmResponse.getStatus());
        }
        assertEquals("Max attempts of confirm exceeded",clientConfirmResponse.getMessage());

        client = clientRepository.findByUsername(clientRegisterRequest.getAuthName());
        assertTrue(!client.isEnabled());
        confirm = confirmRepository.findByClientAndAuthType(client, AuthType.PHONE);
        assertNull(confirm);
    }

    @Test
    public void testRegisterSomeTimes() throws Exception {
        ReflectionTestUtils.setField(mailConfirmService,"mailSender",mailSender);
        doNothing().when(mailSender).send(anyObject());
        ReflectionTestUtils.setField(clientService,"maxConfirmAttempt",10);

        ClientRegisterResponse clientRegisterResponse = null;
        String authName = "+79267787787";
        String password = "password";
        int max = 5;
        for (int i = 0; i <= max; i++) {
            password = "password" + "_" + i;
            ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
            clientRegisterRequest.setAuthType(AuthType.PHONE);
            clientRegisterRequest.setAuthName(authName);
            clientRegisterRequest.setAuthPassword(password);
            clientRegisterRequest.setConfirm(true);

            clientRegisterResponse = clientService.register(clientRegisterRequest);
            assertNotNull(clientRegisterResponse);
            assertTrue(clientRegisterResponse.getStatus().equals(ResponseStatus.SUCCESS));

        }
        Client client = clientRepository.findByUsername(authName);
        assertNotNull(client);
        assertTrue(!client.isEnabled());
        assertEquals("password_" + max, password);
        assertNull(client.getPassword());

        //change password
        password = "password_changed";
        ClientConfirmRequest clientConfirmRequest = new ClientConfirmRequest();
        clientConfirmRequest.setAuthType(AuthType.PHONE);
        clientConfirmRequest.setAuthName(authName);
        clientConfirmRequest.setAuthPassword(password);
        clientConfirmRequest.setConfirmCode(clientRegisterResponse.getConfirmCode());

        ClientConfirmResponse clientConfirmResponse = clientService.confirm(clientConfirmRequest);
        assertNotNull(clientConfirmResponse);
        assertEquals(ResponseStatus.SUCCESS,clientConfirmResponse.getStatus());

        client = clientRepository.findByUsername(clientConfirmRequest.getAuthName());
        assertNotNull(client);
        assertTrue(client.isEnabled());
        assertNotNull(client.getPassword());
        Confirm confirm = confirmRepository.findByClientAndAuthType(client,AuthType.PHONE);
        assertNull(confirm);

        assertTrue(securityService.match(password,client.getPassword()));

    }

    @Test
    public void testValidEmailAndPhone() {
        ClientRegisterRequest clientRegisterRequest = new ClientRegisterRequest();
        clientRegisterRequest.setAuthName("invalid#mail.ru");
        clientRegisterRequest.setAuthType(AuthType.EMAIL);
        ClientRegisterResponse clientRegisterResponseInvalidEmail = clientService.register(clientRegisterRequest);
        assertEquals(ResponseStatus.FAIL, clientRegisterResponseInvalidEmail.getStatus());
        assertTrue(clientRegisterResponseInvalidEmail.getMessage(), clientRegisterResponseInvalidEmail.getMessage().toLowerCase().contains("invalid email"));

        clientRegisterRequest.setAuthName("891928232");
        clientRegisterRequest.setAuthType(AuthType.PHONE);
        ClientRegisterResponse clientRegisterResponseInvalidPhone = clientService.register(clientRegisterRequest);
        assertEquals(ResponseStatus.FAIL, clientRegisterResponseInvalidEmail.getStatus());
        assertTrue(clientRegisterResponseInvalidPhone.getMessage(), clientRegisterResponseInvalidPhone.getMessage().toLowerCase().contains("invalid phone"));
    }

    @Test
    @Ignore
    public void testRegisterViaPhoneReal() throws MailSenderException {
        String authName = "+79267796753";
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
        Confirm confirm = confirmRepository.findByClientAndAuthType(client, AuthType.PHONE);
        assertNotNull(confirm);
        assertTrue(confirm.isEnabled());
        assertNotNull(confirm.getCode());

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
