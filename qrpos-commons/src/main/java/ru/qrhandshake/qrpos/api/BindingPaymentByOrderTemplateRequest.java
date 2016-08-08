package ru.qrhandshake.qrpos.api;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 08.08.16.
 */
public class BindingPaymentByOrderTemplateRequest extends ApiAuth {

    @NotNull
    private String orderTemplateId;
    @NotNull
    private String sessionId;
    private String deviceId;

    public String getOrderTemplateId() {
        return orderTemplateId;
    }

    public void setOrderTemplateId(String orderTemplateId) {
        this.orderTemplateId = orderTemplateId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
