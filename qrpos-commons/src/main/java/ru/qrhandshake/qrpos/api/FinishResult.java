package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 17.08.16.
 */
public class FinishResult extends AbstractResult {

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


    public static class Result {
        static FinishResult finishResult = new FinishResult();
        public FinishResult.Result setOrderId(String orderId) {
            finishResult.setOrderId(orderId);
            return this;
        }
        public FinishResult.Result setOrderStatus(OrderStatus orderStatus) {
            finishResult.setOrderStatus(orderStatus);
            return this;
        }
        public FinishResult build() {
            return finishResult;
        }
        public FinishResult.Result setErrorMessage(String message) {
            finishResult.setCode(0);
            finishResult.setMessage(message);
            return this;
        }
    }
}
