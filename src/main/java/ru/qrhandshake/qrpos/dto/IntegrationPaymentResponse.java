package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.integration.IntegrationOrderStatus;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationPaymentResponse {

    private String orderId;
    private String acsUrl;
    private String paReq;
    private String termUrl;
    private IntegrationOrderStatus orderStatus;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAcsUrl() {
        return acsUrl;
    }

    public void setAcsUrl(String acsUrl) {
        this.acsUrl = acsUrl;
    }

    public String getPaReq() {
        return paReq;
    }

    public void setPaReq(String paReq) {
        this.paReq = paReq;
    }

    public String getTermUrl() {
        return termUrl;
    }

    public void setTermUrl(String termUrl) {
        this.termUrl = termUrl;
    }

    public IntegrationOrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(IntegrationOrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
