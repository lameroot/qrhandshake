package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 24.05.16.
 */
public enum ResponseStatus {
    FAIL(0),
    SUCCESS(1);

    private final int code;

    ResponseStatus(int code) {
        this.code = code;
    }
}
