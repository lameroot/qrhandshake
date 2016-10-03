package ru.qrhandshake.qrpos.service.confirm;

/**
 * Created by lameroot on 29.09.16.
 */
public class ConfirmResult {

    private String confirmCode;
    private boolean status;

    public ConfirmResult() {
    }

    public ConfirmResult(String confirmCode, boolean status) {
        this.confirmCode = confirmCode;
        this.status = status;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
