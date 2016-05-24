package ru.qrhandshake.qrpos.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.ApiAuth;
import ru.qrhandshake.qrpos.api.MerchantRegisterRequest;
import ru.qrhandshake.qrpos.api.MerchantRegisterResponse;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.dto.AuthRequest;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantRepository;
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
    private UserRepository userRepository;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserService userService;

    public Merchant findByMerchantId(String merchantId) {
        return merchantRepository.findByMerchantId(merchantId);
    }

    public Merchant loadMerchant(AuthRequest authRequest) throws AuthException {
        Merchant merchant = merchantRepository.findByUsername(authRequest.getLogin());
        if ( null == merchant || merchant.getPassword().equals(passwordEncoder.encode(authRequest.getPassword())) ) {
            throw new AuthException("Invalid username and password");
        }
        return merchant;
    }

    public Merchant create(MerchantDto merchantDto) {
        Merchant merchant = new Merchant();
        merchant.setContact(merchantDto.getContact());
        merchant.setCreatedDate(new Date());
        merchant.setDescription(merchantDto.getDescription());
        merchant.setName(merchantDto.getName());
        merchant.setUsername(merchantDto.getUsername());
        merchant.setPassword(encodePassword(merchantDto.getPassword()));

        merchantRepository.save(merchant);
        merchantDto.setId(merchant.getId());
        return merchant;
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean isExist(String merchantId) {
        return null != merchantRepository.findByMerchantId(merchantId);
    }

    public MerchantRegisterResponse register(MerchantRegisterRequest merchantRegisterRequest) {
        if ( isExist(merchantRegisterRequest.getMerchantId()) ) {
            MerchantRegisterResponse merchantRegisterResponse = new MerchantRegisterResponse();
            merchantRegisterResponse.setMessage("Merchant exists");
            merchantRegisterResponse.setStatus(ResponseStatus.FAIL);
            return merchantRegisterResponse;
        }
        else {
            Merchant merchant = new Merchant();
            merchant.setDescription(merchantRegisterRequest.getDescription());
            merchant.setName(merchantRegisterRequest.getName());
            merchant.setMerchantId(merchantRegisterRequest.getMerchantId());
            merchantRepository.save(merchant);

            String password = merchant.getMerchantId();//first password
            User user = userService.create(merchant, password, true);

            MerchantRegisterResponse merchantRegisterResponse = new MerchantRegisterResponse();
            merchantRegisterResponse.setStatus(ResponseStatus.SUCCESS);
            merchantRegisterResponse.setMessage("Merchant created");
            merchantRegisterResponse.setMerchantId(merchant.getMerchantId());
            ApiAuth apiAuth = new ApiAuth(user.getUsername(), password);
            merchantRegisterResponse.setAuth(apiAuth);

            return merchantRegisterResponse;
        }
    }


}
