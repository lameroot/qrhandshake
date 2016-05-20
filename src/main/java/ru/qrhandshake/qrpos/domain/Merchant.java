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
public class Merchant implements UserDetails{

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "merchantSequence")
    @SequenceGenerator(name = "merchantSequence", sequenceName = "seq_merchant", allocationSize = 1)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String description;
    private String contact;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
    private Set<MerchantDetail> merchantDetails;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.EMPTY_SET;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Set<MerchantDetail> getMerchantDetails() {
        return merchantDetails;
    }

    public void setMerchantDetails(Set<MerchantDetail> merchantDetails) {
        this.merchantDetails = merchantDetails;
    }
}
