package ru.qrhandshake.qrpos.exception;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 20.05.16.
 */
public class IllegalOrderStatusException extends Exception {

    private OrderStatus illegalOrderStatus;

    public IllegalOrderStatusException(OrderStatus illegalOrderStatus) {
        this.illegalOrderStatus = illegalOrderStatus;
    }

    public IllegalOrderStatusException(String message, OrderStatus illegalOrderStatus) {
        super(message);
        this.illegalOrderStatus = illegalOrderStatus;
    }

    public IllegalOrderStatusException(String message, Throwable cause, OrderStatus illegalOrderStatus) {
        super(message, cause);
        this.illegalOrderStatus = illegalOrderStatus;
    }

    public IllegalOrderStatusException(Throwable cause, OrderStatus illegalOrderStatus) {
        super(cause);
        this.illegalOrderStatus = illegalOrderStatus;
    }

    public IllegalOrderStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, OrderStatus illegalOrderStatus) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.illegalOrderStatus = illegalOrderStatus;
    }

    public OrderStatus getIllegalOrderStatus() {
        return illegalOrderStatus;
    }
}
