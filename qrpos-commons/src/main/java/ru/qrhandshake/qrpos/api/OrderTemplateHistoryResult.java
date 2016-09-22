package ru.qrhandshake.qrpos.api;

import java.util.*;

/**
 * Created by lameroot on 11.08.16.
 */
public class OrderTemplateHistoryResult {

    private List<OrderTemplateHistoryData> orders = new ArrayList<>();

    public List<OrderTemplateHistoryData> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderTemplateHistoryData> orders) {
        this.orders = orders;
    }

    public static class OrderTemplateHistoryData {
        Map<String,Object> asMap = new HashMap<>();
        String humanOrderNumber;
        String deviceModel;
        String deviceMobileNumberMasked;
        Date date;
        Long id;

        public OrderTemplateHistoryData setHumanOrderNumber(String humanOrderNumber) {
            this.humanOrderNumber = humanOrderNumber;
            asMap.put("humanOrderNumber",humanOrderNumber);
            return this;
        }

        public OrderTemplateHistoryData setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
            asMap.put("deviceModel",deviceModel);
            return this;
        }

        public OrderTemplateHistoryData setDeviceMobileNumberMasked(String deviceMobileNumberMasked) {
            this.deviceMobileNumberMasked = deviceMobileNumberMasked;
            asMap.put("deviceMobileNumberMasked",deviceMobileNumberMasked);
            return this;
        }

        public OrderTemplateHistoryData setDate(Date date) {
            this.date = date;
            asMap.put("date",date);
            return this;
        }

        public OrderTemplateHistoryData setId(Long id) {
            this.id = id;
            asMap.put("id",id);
            return this;
        }

        public Map<String,Object> toMap() {
            return this.asMap;
        }
    }
}
