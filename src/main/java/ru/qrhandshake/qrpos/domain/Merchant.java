package ru.qrhandshake.qrpos.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.qrhandshake.qrpos.integration.IntegrationSupport;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Created by lameroot on 18.05.16.
 */
@Entity
@Table(name = "merchant")
public class Merchant {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "merchantSequence")
    @SequenceGenerator(name = "merchantSequence", sequenceName = "seq_merchant", allocationSize = 1)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(name = "merchant_id", unique = true, nullable = false)
    private String merchantId;
    private String description;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
    private Set<Terminal> terminals;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
    private Set<User> users;


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

    public Set<Terminal> getTerminals() {
        return terminals;
    }

    public void setTerminals(Set<Terminal> terminals) {
        this.terminals = terminals;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}
