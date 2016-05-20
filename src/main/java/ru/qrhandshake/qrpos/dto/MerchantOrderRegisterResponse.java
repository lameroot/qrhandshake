package ru.qrhandshake.qrpos.dto;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterResponse extends MerchantResponse {

    private String paymentUrl;
    private String qrUrl;

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }
}
