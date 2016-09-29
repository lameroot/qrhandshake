package ru.qrhandshake.qrpos.api.client;

import ru.qrhandshake.qrpos.api.ApiAuth;

/**
 * Created by lameroot on 29.09.16.
 */
public class ClientConfirmRequest extends ApiAuth {

    private String confirmCode;

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }
}
