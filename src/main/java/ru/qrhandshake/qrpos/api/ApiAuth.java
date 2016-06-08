package ru.qrhandshake.qrpos.api;

import org.apache.commons.lang.StringUtils;


/**
 * Created by lameroot on 24.05.16.
 */
public class ApiAuth {

    private String authName;
    private String authPassword;
    private AuthType authType;

    public ApiAuth() {
    }

    public ApiAuth(String authName, String authPassword) {
        this.authName = authName;
        this.authPassword = authPassword;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public boolean authIsNotBlank() {
        return StringUtils.isNotBlank(authName) && StringUtils.isNotBlank(authPassword);
    }
}
