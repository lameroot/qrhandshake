package ru.qrhandshake.qrpos.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.api.TerminalRegisterRequest;
import ru.qrhandshake.qrpos.api.TerminalRegisterResponse;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;

import javax.annotation.Resource;

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

    public TerminalRegisterResponse register(TerminalRegisterRequest terminalRegisterRequest) {
        Merchant merchant = null;
        TerminalRegisterResponse terminalRegisterResponse = new TerminalRegisterResponse();
        if ( null == (merchant = merchantService.findByMerchantId(terminalRegisterRequest.getMerchantId())) ) {
            terminalRegisterResponse.setMerchantId(terminalRegisterRequest.getMerchantId());
            terminalRegisterResponse.setStatus(ResponseStatus.FAIL);
            terminalRegisterResponse.setMessage("MerchantId: " + terminalRegisterRequest.getMerchantId() + " not exists");
            return terminalRegisterResponse;
        }
        if ( StringUtils.isBlank(terminalRegisterRequest.getTerminalId()) ) {
            terminalRegisterResponse.setMerchantId(merchant.getMerchantId());
            terminalRegisterResponse.setStatus(ResponseStatus.FAIL);
            terminalRegisterResponse.setMessage("Unknown terminalId, you can send generateTerminalId='true' param to generate terminalId");
            return terminalRegisterResponse;
        }
        Terminal terminal = terminalRepository.findByTerminalId(terminalRegisterRequest.getTerminalId());
        if ( null != terminal ) {
            terminalRegisterResponse.setMerchantId(merchant.getMerchantId());
            terminalRegisterResponse.setStatus(ResponseStatus.FAIL);
            terminalRegisterResponse.setMessage("Terminal was already registered early");
            terminalRegisterResponse.setAuth(new ApiAuth(terminal.getTerminalId(), null));
            return terminalRegisterResponse;
        }
        terminal = new Terminal();
        terminal.setMerchant(merchant);
        terminal.setTerminalId(terminalRegisterRequest.getTerminalId());
        terminal.setPassword(passwordEncoder.encode(terminalRegisterRequest.getAuthPassword()));
        terminalRepository.save(terminal);

        terminalRegisterResponse.setMerchantId(merchant.getMerchantId());
        terminalRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        terminalRegisterResponse.setMessage("Terminal success registered");
        terminalRegisterResponse.setAuth(new ApiAuth(terminalRegisterRequest.getTerminalId(), terminalRegisterRequest.getAuthPassword()));
        return terminalRegisterResponse;

    }
}
