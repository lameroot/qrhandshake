package ru.qrhandshake.qrpos.api;

import org.jetbrains.annotations.Nullable;

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

    public static @Nullable ResponseStatus valueOfCode(int code) {
        for (ResponseStatus responseStatus : values()) {
            if ( responseStatus.code == code ) return responseStatus;
        }
        return null;
    }
}
