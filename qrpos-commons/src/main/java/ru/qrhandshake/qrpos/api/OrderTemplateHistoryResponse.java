package ru.qrhandshake.qrpos.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lameroot on 11.08.16.
 */
public class OrderTemplateHistoryResponse extends ApiResponse {

    //todo: добавить также дату и номер телефона (последние цифры)
    private List<String> orderNumbers = new ArrayList<>();

    public List<String> getOrderNumbers() {
        return orderNumbers;
    }

    public void setOrderNumbers(List<String> orderNumbers) {
        this.orderNumbers = orderNumbers;
    }
}
