package ru.qrhandshake.qrpos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.TerminalRegisterRequest;
import ru.qrhandshake.qrpos.api.TerminalRegisterResponse;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.Set;

/**
 * Created by lameroot on 24.05.16.
 */
@Service
public class TerminalService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TerminalRepository terminalRepository;
    @Resource
    private SecurityService securityService;

    private static final int LENGTH_AUTH = 8;

    String generateUniqueAuthName() {
        String name = Util.generatePseudoUnique(LENGTH_AUTH);
        if ( null != terminalRepository.findByAuthName(name) ) return generateUniqueAuthName();
        return name;
    }

    String generateAuthPassword() {
        return Util.generatePseudoUnique(LENGTH_AUTH);
    }

    public Terminal auth(ApiAuth apiAuth) {
        Terminal terminal = terminalRepository.findByAuthName(apiAuth.getAuthName());
        if ( null != terminal && terminal.isEnabled() && securityService.match(apiAuth.getAuthPassword(), terminal.getAuthPassword()) ) {
            return terminal;
        }
        return null;
    }

    public Terminal findById(Long id) {
        return terminalRepository.findOne(id);
    }

    public Set<Terminal> findByMerchant(Merchant merchant) {
        return terminalRepository.findByMerchant(merchant);
    }

    @Transactional
    public TerminalRegisterResponse create(Merchant merchant) {
        return create(merchant,null);
    }

    @Transactional
    public TerminalRegisterResponse create(Merchant merchant, TerminalRegisterRequest terminalRegisterRequest) {
        ApiAuth terminalAuth = null;
        if ( null != terminalRegisterRequest && terminalRegisterRequest.authIsNotBlank() && null == terminalRepository.findByAuthName(terminalRegisterRequest.getAuthName())) {
            terminalAuth = terminalRegisterRequest;
        }
        else {
            logger.debug("ApiAuth either null or terminal with this name already exists. Generate authName and authPassword for new terminal.");
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
