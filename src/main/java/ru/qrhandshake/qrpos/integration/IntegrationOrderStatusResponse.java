package ru.qrhandshake.qrpos.integration;

import ru.qrhandshake.qrpos.domain.BindingInfo;
import ru.qrhandshake.qrpos.domain.OrderStatus;
import ru.qrhandshake.qrpos.integration.IntegrationOrderStatus;

/**
 * Created by lameroot on 20.05.16.
 */
public class IntegrationOrderStatusResponse extends IntegrationResponse {

    private String orderId;
    private String externalId;
    private IntegrationOrderStatus integrationOrderStatus;
    private OrderStatus orderStatus;
    private BindingInfo bindingInfo;
    //todo: добавить сюда поля которые могут приходить из запроса к процессингу


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public IntegrationOrderStatus getIntegrationOrderStatus() {
        return integrationOrderStatus;
    }

    public void setIntegrationOrderStatus(IntegrationOrderStatus integrationOrderStatus) {
        this.integrationOrderStatus = integrationOrderStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BindingInfo getBindingInfo() {
        return bindingInfo;
    }

    public void setBindingInfo(BindingInfo bindingInfo) {
        this.bindingInfo = bindingInfo;
    }
}
