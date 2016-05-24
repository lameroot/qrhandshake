package ru.qrhandshake.qrpos.dto;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterRequest extends AuthRequest {

    @NotNull
    private Long amount;
    private String description;
    private byte[] img;
    private ClientDto client;

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

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }
}
