package ru.qrhandshake.qrpos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.qrhandshake.qrpos.domain.EnumGrantedAuthority;
import ru.qrhandshake.qrpos.domain.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecurityUtils {
    private final static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    public static List<EnumGrantedAuthority> createAuthorityList(String... roles) {
        List<EnumGrantedAuthority> authorities = new ArrayList<>(roles.length);
        for (String role : roles) {
            try {
                authorities.add(EnumGrantedAuthority.valueOf(role));
            } catch (IllegalArgumentException e) {
                logger.error("Error while adding authority", e);
                throw new RuntimeException(e);
            }
        }
        return authorities;
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        } else {
            return null;
        }
    }

    public static boolean isCurrentUserHasRole(EnumGrantedAuthority authority) {
        return isUserHasRole(getCurrentUser(), authority);
    }

    public static boolean isUserHasRole(User user, EnumGrantedAuthority authority) {
        for (EnumGrantedAuthority enumGrantedAuthority : user.getAuthorities()) {
            if (enumGrantedAuthority.equals(authority)) return true;
        }
        return false;
    }

    public static boolean isCurrentUserAdmin() {
        User user = getCurrentUser();
        return user != null && isUserHasRole(user, EnumGrantedAuthority.ADMIN);
    }
}
