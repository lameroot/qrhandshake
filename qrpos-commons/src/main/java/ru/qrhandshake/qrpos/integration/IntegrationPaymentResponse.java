package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.domain.PaymentSecureType;
import ru.qrhandshake.qrpos.domain.PaymentType;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;

/**
 * Created by lameroot on 19.05.16.
 */
public class IntegrationPaymentResponse extends IntegrationResponse {

    private String orderId;
    private String externalId;
    private ReturnUrlObject returnUrlObject;
    private OrderStatus orderStatus;
    private IntegrationOrderStatus integrationOrderStatus;
    private PaymentSecureType paymentSecureType;
    private String externalBindingId;
    private PaymentType paymentType;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ReturnUrlObject getReturnUrlObject() {
        return returnUrlObject;
    }

    public void setReturnUrlObject(ReturnUrlObject returnUrlObject) {
        this.returnUrlObject = returnUrlObject;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public IntegrationOrderStatus getIntegrationOrderStatus() {
        return integrationOrderStatus;
    }

    public void setIntegrationOrderStatus(IntegrationOrderStatus integrationOrderStatus) {
        this.integrationOrderStatus = integrationOrderStatus;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public PaymentSecureType getPaymentSecureType() {
        return paymentSecureType;
    }

    public void setPaymentSecureType(PaymentSecureType paymentSecureType) {
        this.paymentSecureType = paymentSecureType;
    }

    public String getExternalBindingId() {
        return externalBindingId;
    }

    public void setExternalBindingId(String externalBindingId) {
        this.externalBindingId = externalBindingId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }
}
