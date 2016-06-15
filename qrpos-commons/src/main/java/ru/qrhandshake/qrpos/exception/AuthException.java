package ru.qrhandshake.qrpos.exception;

import ru.qrhandshake.qrpos.api.ApiAuth;

/**
 * Created by lameroot on 18.05.16.
 */
public class AuthException extends Exception {

    private ApiAuth apiAuth;
    public AuthException() {
    }
    public AuthException(ApiAuth apiAuth) {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

    public AuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ApiAuth getApiAuth() {
        return apiAuth;
    }

    public void setApiAuth(ApiAuth apiAuth) {
        this.apiAuth = apiAuth;
    }
}
