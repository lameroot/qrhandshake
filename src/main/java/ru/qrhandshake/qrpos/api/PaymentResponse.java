package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 26.05.16.
 */
public class PaymentResponse extends ApiResponse {

    private String orderId;
    private String redirectUrlOrPagePath;
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

    public String getRedirectUrlOrPagePath() {
        return redirectUrlOrPagePath;
    }

    public void setRedirectUrlOrPagePath(String redirectUrlOrPagePath) {
        this.redirectUrlOrPagePath = redirectUrlOrPagePath;
    }
}
