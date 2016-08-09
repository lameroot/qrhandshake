package ru.qrhandshake.qrpos.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.exception.AuthException;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * Created by lameroot on 08.06.16.
 */
@Service
public class AuthService {

    @Resource
    private TerminalService terminalService;
    @Resource
    private ClientService clientService;

    public Terminal terminalAuth(Principal principal, ApiAuth apiAuth) throws AuthException {
        Terminal terminal = null;
        if ( null != principal ) {
            terminal = (Terminal) ((Authentication) principal).getPrincipal();
        }
        else if ( null != apiAuth && apiAuth.authIsNotBlank() ) {
            terminal = terminalService.auth(apiAuth);
        }
        if ( null == terminal ) throw new AuthException("Terminal not auth");
        return terminal;
    }

    public Client clientAuth(Principal principal, ApiAuth apiAuth, boolean throwExceptionIfNull) throws AuthException {
        Client client = null;
        if ( null != principal ) {
            client = (Client) ((Authentication) principal).getPrincipal();
        }
        else if ( null != apiAuth && apiAuth.authIsNotBlank() ) {
            client = clientService.auth(apiAuth);
        }
        if ( null == client && throwExceptionIfNull ) throw new AuthException("Client not auth");
        return client;
    }
}
