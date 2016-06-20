package ru.qrhandshake.qrpos.integration;

import org.springframework.ui.Model;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.domain.PaymentWay;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationPaymentRequest extends IntegrationRequest {

    private String orderId;
    private String returnUrl;
    private String description;
    private Long amount;
    private Client client;
    private OrderStatus orderStatus;
    private Map<String, String> params = new HashMap<>();
    private PaymentWay paymentWay = PaymentWay.CARD;
    private PaymentParams paymentParams;
    private String ip;

    public IntegrationPaymentRequest(IntegrationSupport integrationSupport) {
        super(integrationSupport);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
}
