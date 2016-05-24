package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.integration.IntegrationOrderStatus;

/**
 * Created by lameroot on 20.05.16.
 */
public class IntegrationOrderStatusResponse {

    private String externalId;
    private IntegrationOrderStatus orderStatus;
    //todo: добавить сюда поля которые могут приходить из запроса к процессингу

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public IntegrationOrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(IntegrationOrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
