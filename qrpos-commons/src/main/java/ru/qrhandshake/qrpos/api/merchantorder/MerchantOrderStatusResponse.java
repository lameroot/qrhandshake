package ru.qrhandshake.qrpos.api.merchantorder;

import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.domain.OrderStatus;

import java.util.Date;

/**
 * Created by lameroot on 20.05.16.
 */
public class MerchantOrderStatusResponse extends ApiResponse {

    private String orderId;
    private Long amount;
    private OrderStatus orderStatus;
    private Date createdDate;
    private Date paymentDate;

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

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
}
