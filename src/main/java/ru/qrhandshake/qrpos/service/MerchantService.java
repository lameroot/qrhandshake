package ru.qrhandshake.qrpos.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.qrhandshake.qrpos.api.*;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.dto.AuthRequest;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
import ru.qrhandshake.qrpos.repository.TerminalRepository;
import ru.qrhandshake.qrpos.repository.UserRepository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
 * Created by lameroot on 18.05.16.
 */
@Service
public class MerchantService {

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
        }
        else if ( !merchantRegisterRequest.authIsNotBlank() ) {
            merchantRegisterResponse.setStatus(ResponseStatus.FAIL);
            merchantRegisterResponse.setMessage("Auth params aren't correct");
        }
        else if ( null != userService.loadUserByUsername(merchantRegisterRequest.getAuthName()) ) {
            merchantRegisterResponse.setMessage("User with name: '" + merchantRegisterRequest.getAuthName() + "' exists");
            merchantRegisterResponse.setStatus(ResponseStatus.FAIL);
        }
        else {
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
            TerminalRegisterResponse terminalRegisterResponse = terminalService.create(merchant, new ApiAuth(merchantRegisterRequest.getAuthName(), merchantRegisterRequest.getAuthPassword()));

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
        }
        return merchantRegisterResponse;
    }


}
