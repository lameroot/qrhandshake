package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lameroot on 18.05.16.
 */
//@Entity
//@Table(name = "user")
public class User {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batchSequence")
    @SequenceGenerator(name = "batchSequence", sequenceName = "seq_batch", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_merchant_id")
    private Merchant merchant;
    private String login;
    private String password;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
