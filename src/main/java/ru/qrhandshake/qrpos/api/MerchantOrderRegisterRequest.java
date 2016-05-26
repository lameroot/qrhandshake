package ru.qrhandshake.qrpos.api;


import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterRequest extends ApiAuth {

    @NotNull
    private Long amount;
    private String description;
    private String deviceId;
    @NotNull
    private String sessionId;

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
