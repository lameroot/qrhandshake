package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.OrderStatus;

/**
 * Created by lameroot on 20.05.16.
 */
public class IntegrationOrderStatusResponse {

    private String externalId;
    private OrderStatus orderStatus;
    //todo: добавить сюда поля которые могут приходить из запроса к процессингу

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
