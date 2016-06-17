package ru.qrhandshake.qrpos.integration;

/**
 * Created by lameroot on 26.05.16.
 */
public class IntegrationResponse {

    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
