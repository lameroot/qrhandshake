package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.TerminalRegisterRequest;
import ru.qrhandshake.qrpos.api.TerminalRegisterResponse;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.Optional;
import java.util.Random;

/**
 * Created by lameroot on 24.05.16.
 */
@Service
public class TerminalService {

    @Resource
    private TerminalRepository terminalRepository;
    @Resource
    private SecurityService securityService;

    private static final char[] _base62chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnpqrstuvwxyz".toCharArray();
    private static final int LENGTH_AUTH = 8;

    String generateUniqueAuthName() {
        Random _random = new Random();
        StringBuilder sb = new StringBuilder(LENGTH_AUTH);

        for (int i=0; i<LENGTH_AUTH; i++) {
            sb.append(_base62chars[_random.nextInt(36)]);
        }
        String name = sb.toString();
        if ( null != terminalRepository.findByAuthName(name) ) return generateUniqueAuthName();
        return name;
    }

    String generateAuthPassword() {
        Random _random = new Random();
        StringBuilder sb = new StringBuilder(LENGTH_AUTH);

        for (int i=0; i<LENGTH_AUTH; i++) {
            sb.append(_base62chars[_random.nextInt(36)]);
        }
        return sb.toString();
    }

    public Terminal auth(ApiAuth apiAuth) {
        Terminal terminal = terminalRepository.findByAuthName(apiAuth.getAuthName());
        if ( null != terminal && terminal.isEnabled() && securityService.match(apiAuth.getAuthPassword(), terminal.getAuthPassword()) ) {
            return terminal;
        }
        return null;
    }

    @Transactional
    public TerminalRegisterResponse create(Merchant merchant) {
        return create(merchant,null);
    }

    @Transactional
    public TerminalRegisterResponse create(Merchant merchant, ApiAuth apiAuth) {
        ApiAuth terminalAuth = null;
        if ( null != apiAuth && apiAuth.authIsNotBlank() && null == terminalRepository.findByAuthName(apiAuth.getAuthName())) {
            terminalAuth = apiAuth;
        }
        else {
            terminalAuth = new ApiAuth(generateUniqueAuthName(), generateAuthPassword());
        }

        Terminal terminal = new Terminal();
        terminal.setMerchant(merchant);
        terminal.setAuthName(terminalAuth.getAuthName());
        terminal.setAuthPassword(securityService.encodePassword(terminalAuth.getAuthPassword()));
        terminal.setEnabled(true);
        terminalRepository.save(terminal);

        TerminalRegisterResponse terminalRegisterResponse = new TerminalRegisterResponse();
        terminalRegisterResponse.setMerchantId(merchant.getMerchantId());
        terminalRegisterResponse.setAuth(terminalAuth);
        terminalRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        terminalRegisterResponse.setMessage("Terminal success created");
        return terminalRegisterResponse;
    }

}
