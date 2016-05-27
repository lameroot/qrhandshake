package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.PaymentWay;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by lameroot on 19.05.16.
 */
public class PaymentRequest {

    @NotNull
    private String orderId;
    @NotNull
    private String pan;
    @NotNull
    @Size(min = 2, max = 2)
    private String month;
    @NotNull
    @Size(min = 4, max = 4)
    private String year;
    @NotNull
    private String cardHolderName;
    @NotNull
    private String cvc;
    private String returnUrl;
    private PaymentWay paymentWay = PaymentWay.CARD;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public String getExpiry() {
        return year + month;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }
}
