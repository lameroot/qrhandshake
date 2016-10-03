package ru.qrhandshake.qrpos.api.ordertemplate;

import java.util.Date;

/**
 * Created by lameroot on 11.08.16.
 */
public class OrderTemplateHistoryParams {

    private Long orderTemplateId;
    private Long id;
    private Date from = new Date();
    private boolean status = true;

    public Long getOrderTemplateId() {
        return orderTemplateId;
    }

    public void setOrderTemplateId(Long orderTemplateId) {
        this.orderTemplateId = orderTemplateId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
