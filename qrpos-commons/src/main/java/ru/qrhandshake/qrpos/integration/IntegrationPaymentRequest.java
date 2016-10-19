package ru.qrhandshake.qrpos.integration;

import org.springframework.ui.Model;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationPaymentRequest extends IntegrationRequest {

    private String orderId;
    private String externalId;
    private String returnUrl;
    private String description;
    private Long amount;
    private Client client;
    private OrderStatus orderStatus;
    private Map<String, String> params = new HashMap<>();
    private PaymentWay paymentWay = PaymentWay.CARD;
    private PaymentParams paymentParams;
    private String ip;
    private PaymentType paymentType;

    public IntegrationPaymentRequest(){}
    public IntegrationPaymentRequest(Endpoint endpoint) {
        super(endpoint);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    public PaymentParams getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(PaymentParams paymentParams) {
        this.paymentParams = paymentParams;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IntegrationPaymentRequest{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", returnUrl='").append(returnUrl).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", client=").append(client);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", params=").append(params);
        sb.append(", paymentWay=").append(paymentWay);
        sb.append(", paymentParams=").append(paymentParams);
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", paymentType=").append(paymentType);
        sb.append('}');
        return sb.toString();
    }
}
