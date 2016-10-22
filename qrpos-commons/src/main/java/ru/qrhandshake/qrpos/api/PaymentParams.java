package ru.qrhandshake.qrpos.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.qrhandshake.qrpos.api.binding.BindingPaymentParams;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = YandexMoneyPaymentParams.class, name = "yandexMoneyPaymentParams"),
        @JsonSubTypes.Type(value = CardPaymentParams.class, name = "cardPaymentParams"),
        @JsonSubTypes.Type(value = BindingPaymentParams.class, name = "bindingPaymentParams")
})
public class PaymentParams {
    private String orderId;
    private String ip;
    private String returnUrl;
    private String paymentAccount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    @JsonProperty
    public String getPaymentAccount() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount = paymentAccount;
    }
}
