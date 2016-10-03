package ru.qrhandshake.qrpos.api.ordertemplate;

import ru.qrhandshake.qrpos.api.ApiResponse;

/**
 * Created by lameroot on 08.08.16.
 */
public class OrderTemplateResponse extends ApiResponse {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
