package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.dto.ReturnUrlObject;

/**
 * Created by lameroot on 17.08.16.
 */
public class CardBindingCreateResponse extends ApiResponse {

    private ReturnUrlObject returnUrlObject;
    private String orderId;
    private OrderStatus orderStatus;

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
}
