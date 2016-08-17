package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 17.08.16.
 */
public abstract class AbstractResult {

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
