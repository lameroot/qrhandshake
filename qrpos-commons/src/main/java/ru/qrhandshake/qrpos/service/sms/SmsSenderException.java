package ru.qrhandshake.qrpos.service.sms;

public class SmsSenderException extends Exception {

    public SmsSenderException() {
    }

    public SmsSenderException(String message) {
        super(message);
    }

    public SmsSenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmsSenderException(Throwable cause) {
        super(cause);
    }

    public SmsSenderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
