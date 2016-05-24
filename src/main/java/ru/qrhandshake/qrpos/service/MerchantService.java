package ru.qrhandshake.qrpos.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.dto.MerchantDto;
import ru.qrhandshake.qrpos.dto.AuthRequest;
import ru.qrhandshake.qrpos.exception.AuthException;
import ru.qrhandshake.qrpos.repository.MerchantRepository;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by lameroot on 18.05.16.
 */
@Service
public class MerchantService implements UserDetailsService {

    @Resource
    private MerchantRepository merchantRepository;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Merchant merchant = merchantRepository.findByUsername(username);
        return merchant;
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

        return merchantRepository.save(merchant);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
