package ru.qrhandshake.qrpos.api.ordertemplate;

import ru.qrhandshake.qrpos.api.ApiResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lameroot on 11.08.16.
 */
public class OrderTemplateHistoryResponse extends ApiResponse {

    private List<Map<String,Object>> orders = new ArrayList<>();

    public List<Map<String, Object>> getOrders() {
        return orders;
    }

    public void setOrders(List<Map<String, Object>> orders) {
        this.orders = orders;
    }
}
