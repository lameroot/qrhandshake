package ru.qrhandshake.qrpos.api.binding;


import ru.qrhandshake.qrpos.domain.OrderTemplate;

/**
 * Created by lameroot on 09.08.16.
 */
public class BindingPaymentByOrderTemplateParams {

    private String bindingId;
    private OrderTemplate orderTemplate;
    private String sessionId;
    private String deviceId;
    private String deviceModel;
    private String deviceMobileNumber;
    private String returnUrl;


    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public OrderTemplate getOrderTemplate() {
        return orderTemplate;
    }

    public void setOrderTemplate(OrderTemplate orderTemplate) {
        this.orderTemplate = orderTemplate;
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

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
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
