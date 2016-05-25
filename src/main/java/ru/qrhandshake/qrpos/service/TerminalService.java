package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

/**
 * Created by lameroot on 24.05.16.
 */
@Service
public class TerminalService {

    @Resource
    private TerminalRepository terminalRepository;
    @Resource
    private MerchantService merchantService;
    @Resource
    private PasswordEncoder passwordEncoder;


    public Terminal auth(ApiAuth apiAuth) {
        Terminal terminal = terminalRepository.findByAuthName(apiAuth.getAuthName());
        if ( null != terminal && terminal.isEnabled() && terminal.getAuthPassword().equals(passwordEncoder.encode(apiAuth.getAuthPassword())) ) {
            return terminal;
        }
        return null;
    }

    public TerminalRegisterResponse create(User user, TerminalRegisterRequest terminalRegisterRequest) {
        if ( !user.canCreateTerminal() ) {
            TerminalRegisterResponse terminalRegisterResponse = new TerminalRegisterResponse();
            terminalRegisterResponse.setAuth(new ApiAuth(terminalRegisterRequest.getAuthName(),terminalRegisterRequest.getAuthPassword()));
            terminalRegisterResponse.setStatus(ResponseStatus.FAIL);
            terminalRegisterResponse.setMessage("User: " + user.getUsername() + " can't create terminal");
            return terminalRegisterResponse;
        }
        Merchant merchant = user.getMerchant();
        Terminal terminal = new Terminal();
        terminal.setMerchant(merchant);
        terminal.setAuthName(terminalRegisterRequest.getAuthName());
        terminal.setAuthPassword(passwordEncoder.encode(terminalRegisterRequest.getAuthPassword()));
        terminal.setEnabled(true);
        terminalRepository.save(terminal);

        TerminalRegisterResponse terminalRegisterResponse = new TerminalRegisterResponse();
        terminalRegisterResponse.setMerchantId(merchant.getMerchantId());
        terminalRegisterResponse.setAuth(new ApiAuth(terminalRegisterRequest.getAuthName(),terminalRegisterRequest.getAuthPassword()));
        terminalRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        terminalRegisterResponse.setMessage("Terminal success created");
        return terminalRegisterResponse;

    }

}
