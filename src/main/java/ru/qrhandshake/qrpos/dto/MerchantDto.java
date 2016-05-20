package ru.qrhandshake.qrpos.dto;

import ru.qrhandshake.qrpos.domain.Merchant;

import java.util.Date;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantDto {

    private Long id;
    private String name;
    private String contact;
    private String description;
    private Date createdDate;
    private String username;
    private String password;

    public MerchantDto(){}
    public MerchantDto(Merchant merchant){
        if ( null != merchant ) {
            this.id = merchant.getId();
            this.name = merchant.getName();
            this.contact = merchant.getContact();
            this.description = merchant.getDescription();
            this.createdDate = merchant.getCreatedDate();
            this.username = merchant.getUsername();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
