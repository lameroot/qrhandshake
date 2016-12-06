package ru.qrhandshake.qrpos.api.ordertemplate;

import ru.qrhandshake.qrpos.api.ApiResponse;

/**
 * Created by lameroot on 08.08.16.
 */
public class OrderTemplateResponse extends ApiResponse {

    private Long id;
    private Long amount;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
