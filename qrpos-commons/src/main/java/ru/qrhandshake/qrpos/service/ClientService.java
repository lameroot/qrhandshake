package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.client.ClientConfirmRequest;
import ru.qrhandshake.qrpos.api.client.ClientConfirmResponse;
import ru.qrhandshake.qrpos.api.client.ClientRegisterRequest;
import ru.qrhandshake.qrpos.api.client.ClientRegisterResponse;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.repository.ClientRepository;
import ru.qrhandshake.qrpos.service.confirm.ConfirmResult;
import ru.qrhandshake.qrpos.service.confirm.ConfirmService;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Resource
    private ClientRepository clientRepository;
    @Resource
    private SecurityService securityService;
    @Autowired(required = false)
    private List<ConfirmService> confirmServices;

    @Nullable
    public Client findByUsername(String username) {
        return clientRepository.findByUsername(username);
    }

    public Client findByPhone(String phone) {
        return clientRepository.findByPhone(phone);
    }

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }


    public Client auth(ApiAuth apiAuth) {
        Client client = clientRepository.findByUsername(apiAuth.getAuthName());
        if ( null != client && client.isEnabled() && securityService.match(apiAuth.getAuthPassword(), client.getPassword())) {
            return client;
        }
        return null;
    }

    public ClientRegisterResponse register(ClientRegisterRequest clientRegisterRequest) {
        ClientRegisterResponse clientRegisterResponse = new ClientRegisterResponse();
        final AuthType authType = null != clientRegisterRequest.getAuthType() ? clientRegisterRequest.getAuthType() : AuthType.PASSWORD;

        Client client = findByUsername(clientRegisterRequest.getAuthName());
        //todo: добавить проверку почты если она есть в параметрах?

        if ( null == client ) {
            client = new Client();
            client.setClientId(UUID.randomUUID().toString());
            client.setUsername(clientRegisterRequest.getAuthName());
            switch (authType) {
                case EMAIL: {
                    client.setEmail(clientRegisterRequest.getAuthName());
                    break;
                }
                case PHONE: {
                    client.setPhone(clientRegisterRequest.getAuthName());
                    break;
                }
            }
            client.setEnabled(false);
        }
        else if ( null != client && client.isEnabled() ) {
            logger.info("Client with name: " + clientRegisterRequest.getAuthName() + " already exists");
            clientRegisterResponse.setStatus(ResponseStatus.FAIL);
            clientRegisterResponse.setMessage("Client with name: " + clientRegisterRequest.getAuthName() + " already exists");
            return clientRegisterResponse;
        }

        if ( !clientRegisterRequest.isConfirm() || null == confirmServices || confirmServices.isEmpty() ) {
            client.setPassword(securityService.encodePassword(clientRegisterRequest.getAuthPassword()));
            client.setEnabled(true);
        }
        else {
            ConfirmService confirmService = confirmServices.stream().filter(c -> authType == c.getAuthType()).findFirst().orElse(null);
            if ( null == confirmService ) {
                logger.error("Unable to find confirmService for authType: " + authType);
                clientRegisterResponse.setStatus(ResponseStatus.FAIL);
                clientRegisterResponse.setMessage("Unable to find confirmService for authType: " + authType);
                return clientRegisterResponse;
            }
            ConfirmResult confirmResult = confirmService.sendConfirmRequest(client);
            if ( !confirmResult.isStatus() ) {
                logger.error("We tried send confirm request for {} use {}, but occurred error");
                clientRegisterResponse.setStatus(ResponseStatus.FAIL);
                clientRegisterResponse.setMessage("We tried send confirm request use " + authType + " confirm service, but occurred error");
                return clientRegisterResponse;
            }
            client.setConfirmCode(confirmResult.getConfirmCode());
            clientRegisterResponse.setConfirmCode(confirmResult.getConfirmCode());
        }
        clientRepository.save(client);

        clientRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        clientRegisterResponse.setMessage("Client registered successfully");
        clientRegisterResponse.setAuth(new ApiAuth(clientRegisterRequest.getAuthName(),clientRegisterRequest.getAuthPassword()));

        return clientRegisterResponse;
    }

    public ClientConfirmResponse confirm(ClientConfirmRequest clientConfirmRequest) {
        ClientConfirmResponse clientConfirmResponse = new ClientConfirmResponse();
        if ( null == clientConfirmRequest.getAuthType() ) {
            logger.error("AuthType must not be null");
            clientConfirmResponse.setStatus(ResponseStatus.FAIL);
            clientConfirmResponse.setMessage("AuthType must not be null");
            return clientConfirmResponse;
        }
        Client client = findByUsername(clientConfirmRequest.getAuthName());
        if ( null == client ) {
            logger.error("Client with authName: {} not found", clientConfirmRequest.getAuthName());
            clientConfirmResponse.setStatus(ResponseStatus.FAIL);
            clientConfirmResponse.setMessage("Client with authName:" + clientConfirmRequest.getAuthPassword() + " not found");
            return clientConfirmResponse;
        }
        if ( StringUtils.isBlank(client.getConfirmCode()) || StringUtils.isBlank(clientConfirmRequest.getConfirmCode()) ) {
            logger.error("Either client confirmCode or confirmCode from request is null");
            clientConfirmResponse.setStatus(ResponseStatus.FAIL);
            clientConfirmResponse.setMessage("Either client confirmCode or confirmCode from request is null");
            return clientConfirmResponse;
        }
        boolean isConfirmed = client.getConfirmCode().equalsIgnoreCase(clientConfirmRequest.getConfirmCode());
        if ( !isConfirmed ) {
            logger.error("Confirm codes a client and request aren't equals");
            clientConfirmResponse.setStatus(ResponseStatus.FAIL);
            clientConfirmResponse.setMessage("Confirm codes a client and request aren't equals");
            return clientConfirmResponse;
        }
        client.setPassword(securityService.encodePassword(clientConfirmRequest.getAuthPassword()));
        client.setEnabled(true);
        client.setConfirmCode(null);

        clientRepository.save(client);
        clientConfirmResponse.setStatus(ResponseStatus.SUCCESS);
        clientConfirmResponse.setMessage("Client updated");
        return clientConfirmResponse;
    }
}
