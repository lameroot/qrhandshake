package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;


/**
 * Created by lameroot on 26.05.16.
 */
public class PaymentResponse extends ApiResponse {

    private ReturnUrlObject returnUrlObject;
    private String orderId;
    private OrderStatus orderStatus;
    private PaymentAuthType paymentAuthType = PaymentAuthType.ANONYMOUS;
    private String bindingId;

    public ReturnUrlObject getReturnUrlObject() {
        return returnUrlObject;
    }

    public void setReturnUrlObject(ReturnUrlObject returnUrlObject) {
        this.returnUrlObject = returnUrlObject;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentAuthType getPaymentAuthType() {
        return paymentAuthType;
    }

    public void setPaymentAuthType(PaymentAuthType paymentAuthType) {
        this.paymentAuthType = paymentAuthType;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }
}
