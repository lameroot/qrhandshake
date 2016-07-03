package ru.qrhandshake.qrpos.domain;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created with IntelliJ IDEA.
 * User: Smirnov_Y
 * Date: 22.08.14
 * Time: 18:29
 * To change this template use File | Settings | File Templates.
 */
public enum EnumGrantedAuthority implements GrantedAuthority {
    ADMIN,
    REGISTERED,
    ANONYMOUS;

    @Override
    public String getAuthority() {
        return this.name();
    }

    public static EnumGrantedAuthority getByName(String name) {
        try {
            return EnumGrantedAuthority.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
