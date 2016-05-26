package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 26.05.16.
 */
public class MerchantOrderReverseRequest extends ApiAuth {

    private String terminalSessionId;
    private String orderId;

    public String getTerminalSessionId() {
        return terminalSessionId;
    }

    public void setTerminalSessionId(String terminalSessionId) {
        this.terminalSessionId = terminalSessionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
