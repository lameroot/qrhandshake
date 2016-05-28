package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 28.05.16.
 */
public class IntegrationFinishResponse extends IntegrationResponse {

    private String orderId;
    private OrderStatus orderStatus;

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
}
