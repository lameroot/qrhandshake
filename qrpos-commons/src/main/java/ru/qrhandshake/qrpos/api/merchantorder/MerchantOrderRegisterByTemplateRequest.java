package ru.qrhandshake.qrpos.api.merchantorder;


import ru.qrhandshake.qrpos.api.ApiAuth;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterByTemplateRequest extends ApiAuth {

    private Long templateId;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
}
