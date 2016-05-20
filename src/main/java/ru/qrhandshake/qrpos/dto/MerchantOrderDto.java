package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.MerchantOrder;
import ru.qrhandshake.qrpos.integration.IntegrationSupport;

import java.util.Date;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderDto {

    private Long id;
    private Date expiredDate;
    private Long amount;
    private String description;
    private Long fee;
    private MerchantDto merchant;
    private ClientDto client;
    private IntegrationSupport integrationSupport;
    private String currency;
    private String language;

    public MerchantOrderDto(){}
    public MerchantOrderDto(MerchantOrder merchantOrder){
        this.id = merchantOrder.getId();
        this.expiredDate = merchantOrder.getExpiredDate();
        this.amount = merchantOrder.getAmount();
        this.description = merchantOrder.getDescription();
        this.fee = merchantOrder.getFee();
        this.merchant = new MerchantDto(merchantOrder.getMerchant());
        this.client = new ClientDto(merchantOrder.getClient());
        this.integrationSupport = merchantOrder.getIntegrationSupport();
        this.currency = merchantOrder.getCurrency();
        this.language = merchantOrder.getLanguage();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
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

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public MerchantDto getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantDto merchant) {
        this.merchant = merchant;
    }

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }

    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    public void setIntegrationSupport(IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
