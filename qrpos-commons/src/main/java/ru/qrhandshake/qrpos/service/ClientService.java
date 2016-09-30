package ru.qrhandshake.qrpos.service;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.repository.ClientRepository;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * Created by lameroot on 25.05.16.
 */
@Service
public class ClientService {

    @Resource
    private ClientRepository clientRepository;
    @Resource
    private SecurityService securityService;

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
        Client client = null;
        if ( null != clientRegisterRequest.getAuthType() ) {
            switch (clientRegisterRequest.getAuthType()) {
                case PASSWORD: {
                    client = findByUsername(clientRegisterRequest.getAuthName());
                    break;
                }
                case EMAIL: {
                    client = findByEmail(clientRegisterRequest.getAuthName());
                    break;
                }
                case PHONE: {
                    client = findByPhone(clientRegisterRequest.getAuthName());
                    break;
                }
            }
        }
        if ( null != client ) {
            ClientRegisterResponse clientRegisterResponse = new ClientRegisterResponse();
            clientRegisterResponse.setStatus(ResponseStatus.FAIL);
            clientRegisterResponse.setMessage("Client with name: " + clientRegisterRequest.getAuthName() + " already exists");
            return clientRegisterResponse;
        }
        client = new Client();
        client.setClientId(UUID.randomUUID().toString());
        AuthType authType = clientRegisterRequest.getAuthType();
        if ( null == authType ) authType = AuthType.PASSWORD;
        switch (authType) {
            case PASSWORD: {
                client.setUsername(clientRegisterRequest.getAuthName());
                break;
            }
            case EMAIL: {
                client.setEmail(clientRegisterRequest.getAuthName());
                break;
            }
            case PHONE: {
                client.setPhone(clientRegisterRequest.getAuthName());
                break;
            }
        }
        client.setPassword(securityService.encodePassword(clientRegisterRequest.getAuthPassword()));
        clientRepository.save(client);

        ClientRegisterResponse clientRegisterResponse = new ClientRegisterResponse();
        clientRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        clientRegisterResponse.setMessage("Client registered successfully");
        clientRegisterResponse.setAuth(new ApiAuth(clientRegisterRequest.getAuthName(),clientRegisterRequest.getAuthPassword()));

        return clientRegisterResponse;
    }
}
