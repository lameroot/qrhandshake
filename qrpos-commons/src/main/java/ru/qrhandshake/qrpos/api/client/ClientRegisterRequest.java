package ru.qrhandshake.qrpos.api.client;

import ru.qrhandshake.qrpos.api.ApiAuth;

/**
 * Created by lameroot on 24.05.16.
 */
public class ClientRegisterRequest extends ApiAuth {

    private boolean confirm;

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }
}
