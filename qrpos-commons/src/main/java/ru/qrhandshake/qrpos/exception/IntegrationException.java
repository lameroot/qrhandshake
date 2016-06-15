package ru.qrhandshake.qrpos.exception;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationException extends Exception {

    public IntegrationException() {
    }

    public IntegrationException(String message) {
        super(message);
    }

    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrationException(Throwable cause) {
        super(cause);
    }

    public IntegrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
