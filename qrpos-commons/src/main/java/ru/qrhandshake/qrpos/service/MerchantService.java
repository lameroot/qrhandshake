package ru.qrhandshake.qrpos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterRequest;
import ru.qrhandshake.qrpos.api.merchant.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * Created by lameroot on 18.05.16.
 */
@Service
public class MerchantService {

    @Value("${merchant.root.id:-1}")
    private Long rootMerchantId;

    @Resource
    private MerchantRepository merchantRepository;
    @Resource
    private UserService userService;
    @Resource
    private TerminalService terminalService;

    public boolean isExist(String name) {
        return null != merchantRepository.findByName(name);
    }

    public MerchantRegisterResponse register(MerchantRegisterRequest merchantRegisterRequest) {
        MerchantRegisterResponse merchantRegisterResponse = new MerchantRegisterResponse();
        if ( isExist(merchantRegisterRequest.getName()) ) {
            merchantRegisterResponse.setMessage("Merchant with name: '" + merchantRegisterRequest.getName() + "' exists");
            merchantRegisterResponse.setStatus(ResponseStatus.FAIL);
            return merchantRegisterResponse;
        }
        if ( !merchantRegisterRequest.authIsNotBlank() ) {
            merchantRegisterRequest.setAuthName(merchantRegisterRequest.getName() + "_operator");
            merchantRegisterRequest.setAuthPassword(Util.generatePseudoUnique(8));
        }
        if ( null != userService.loadUserByUsername(merchantRegisterRequest.getAuthName()) ) {
            merchantRegisterResponse.setMessage("User with name: '" + merchantRegisterRequest.getAuthName() + "' exists");
            merchantRegisterResponse.setStatus(ResponseStatus.FAIL);
            return merchantRegisterResponse;
        }

        Merchant merchant = new Merchant();
        merchant.setDescription(merchantRegisterRequest.getDescription());
        merchant.setName(merchantRegisterRequest.getName());
        merchantRepository.save(merchant);

        ApiAuth userAuth = new ApiAuth(merchantRegisterRequest.getAuthName(), merchantRegisterRequest.getAuthPassword());
        User user = userService.create(merchant, userAuth);
        merchantRegisterResponse.setUserAuth(userAuth);
        //create default terminal
        TerminalRegisterRequest terminalRegisterRequest = new TerminalRegisterRequest();
        terminalRegisterRequest.setAuthName(merchantRegisterRequest.getAuthName());
        terminalRegisterRequest.setAuthPassword(merchantRegisterRequest.getAuthPassword());
        terminalRegisterRequest.setDefaultTerminal(true);
        TerminalRegisterResponse terminalRegisterResponse = terminalService.create(merchant, terminalRegisterRequest);

        ApiAuth apiAuth = new ApiAuth(merchantRegisterRequest.getAuthName(), merchantRegisterRequest.getAuthPassword());
        merchantRegisterResponse.setAuth(apiAuth);
        merchantRegisterResponse.setMerchantId(merchant.getMerchantId());
        merchantRegisterResponse.setStatus(ResponseStatus.SUCCESS);
        merchantRegisterResponse.setTerminalAuth(terminalRegisterResponse.getAuth());

        if ( ResponseStatus.SUCCESS.equals(terminalRegisterResponse.getStatus()) ) {
            merchantRegisterResponse.setMessage("Merchant created");
        }
        else {
            merchantRegisterResponse.setMessage("Merchant crated, but terminal not: " + terminalRegisterResponse.getMessage());
        }

        return merchantRegisterResponse;
    }


    public Merchant findRootMerchant() throws AuthException {
        return Optional.of(merchantRepository.findOne(rootMerchantId)).orElseThrow(() -> new AuthException("Not found parent merchant"));
    }
}
