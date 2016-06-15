package ru.qrhandshake.qrpos.exception;

/**
 * Created by lameroot on 19.05.16.
 */
public class MerchantOrderNotFoundException extends Exception {

    public MerchantOrderNotFoundException() {
    }

    public MerchantOrderNotFoundException(String message) {
        super(message);
    }

    public MerchantOrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MerchantOrderNotFoundException(Throwable cause) {
        super(cause);
    }

    public MerchantOrderNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
