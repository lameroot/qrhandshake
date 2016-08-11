package ru.qrhandshake.qrpos.api;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 08.08.16.
 */
public class BindingPaymentByOrderTemplateRequest extends ApiAuth {

    @NotNull
    private String bindingId;
    @NotNull
    private Long orderTemplateId;
    @NotNull
    private String sessionId;
    private String deviceId;
    private String deviceModel;
    private String deviceMobileNumber;

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public Long getOrderTemplateId() {
        return orderTemplateId;
    }

    public void setOrderTemplateId(Long orderTemplateId) {
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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceMobileNumber() {
        return deviceMobileNumber;
    }

    public void setDeviceMobileNumber(String deviceMobileNumber) {
        this.deviceMobileNumber = deviceMobileNumber;
    }
}
