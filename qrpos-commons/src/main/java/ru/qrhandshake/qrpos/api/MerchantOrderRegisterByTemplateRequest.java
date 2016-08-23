package ru.qrhandshake.qrpos.api;


/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterByTemplateRequest extends ApiAuth{

    private Long templateId;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
}
