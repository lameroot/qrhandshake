package ru.qrhandshake.qrpos.api.merchantorder;

import ru.qrhandshake.qrpos.api.ApiAuth;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 26.05.16.
 */
public class MerchantOrderReverseRequest extends ApiAuth {

    @NotNull
    private String sessionId;
    @NotNull
    private String orderId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
