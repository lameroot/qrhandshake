package ru.qrhandshake.qrpos.api;

import java.util.Date;

/**
 * Created by lameroot on 11.08.16.
 */
public class OrderTemplateHistoryParams {

    private Long orderTemplateId;
    private Date from = new Date();

    public Long getOrderTemplateId() {
        return orderTemplateId;
    }

    public void setOrderTemplateId(Long orderTemplateId) {
        this.orderTemplateId = orderTemplateId;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }
}
