package ru.qrhandshake.qrpos.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.Merchant;
import ru.qrhandshake.qrpos.domain.User;
import ru.qrhandshake.qrpos.repository.UserRepository;

import javax.annotation.Resource;

/**
 * Created by lameroot on 24.05.16.
 */
@Service
public class UserService implements UserDetailsService {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userRepository.findByUsername(s);
    }

    public User create(Merchant merchant, String username, String password) {
        return create(merchant, username, password, true, false, false);
    }
    public User create(Merchant merchant, String username, String password,
                       boolean isEnabled, boolean isExpired, boolean isLocked) {
        User user = new User();
        user.setMerchant(merchant);
        user.setUsername(username);
        user.setPassword(encodePassword(password));
        user.setExpired(isExpired);
        user.setEnabled(isEnabled);
        user.setLocked(isLocked);
        return userRepository.save(user);
    }

    public boolean isPasswordValid(UserDetails userDetails, String password) {
        return userDetails.getPassword().equals(encodePassword(password));
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
