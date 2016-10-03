package ru.qrhandshake.qrpos.api.client;


import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.api.merchantorder.MerchantOrderStatusResponse;

import java.util.ArrayList;
import java.util.List;

public class ClientOrderHistoryResponse extends ApiResponse {

    private List<MerchantOrderStatusResponse> orders = new ArrayList<>();

    public List<MerchantOrderStatusResponse> getOrders() {
        return orders;
    }

    public void setOrders(List<MerchantOrderStatusResponse> orders) {
        this.orders = orders;
    }
}
