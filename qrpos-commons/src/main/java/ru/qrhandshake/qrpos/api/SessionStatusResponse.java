package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 21.06.16.
 */
public class SessionStatusResponse extends ApiResponse {

    private String orderId;
    private OrderStatus orderStatus;
    private Long amount;
    private String description;
    private boolean canPayment;

    public SessionStatusResponse() {
    }
    public SessionStatusResponse(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }


    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public boolean isCanPayment() {
        return canPayment;
    }

    public void setCanPayment(boolean canPayment) {
        this.canPayment = canPayment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
