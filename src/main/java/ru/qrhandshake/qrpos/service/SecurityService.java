package ru.qrhandshake.qrpos.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by lameroot on 25.05.16.
 */
@Service
public class SecurityService {

    @Resource
    private PasswordEncoder passwordEncoder;

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean match(String rawPassword, String encoded) {
        return passwordEncoder.matches(rawPassword, encoded);
    }
}
