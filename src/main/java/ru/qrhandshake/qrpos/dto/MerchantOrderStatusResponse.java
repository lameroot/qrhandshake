package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 20.05.16.
 */
public class MerchantOrderStatusResponse extends MerchantResponse {

    private String orderId;
    private Long amount;
    private OrderStatus status;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
