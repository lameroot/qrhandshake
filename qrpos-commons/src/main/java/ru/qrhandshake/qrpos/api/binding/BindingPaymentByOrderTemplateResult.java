package ru.qrhandshake.qrpos.api.binding;

/**
 * Created by lameroot on 09.08.16.
 */
public class BindingPaymentByOrderTemplateResult {

    private String orderId;
    private String humanOrderNumber;
    private boolean status;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getHumanOrderNumber() {
        return humanOrderNumber;
    }

    public void setHumanOrderNumber(String humanOrderNumber) {
        this.humanOrderNumber = humanOrderNumber;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
