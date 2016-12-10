package ru.qrhandshake.qrpos.domain;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.qrhandshake.qrpos.util.SecurityUtils;

import javax.persistence.*;
import java.util.*;

/**
 * Created by lameroot on 18.05.16.
 */
@Entity
@Table(name = "`user`")
public class User implements UserDetails {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSequence")
    @SequenceGenerator(name = "userSequence", sequenceName = "seq_user", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_merchant_id")
    private Merchant merchant;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column(name = "is_enabled")
    private boolean isEnabled;
    @Column(name = "is_expired")
    private boolean isExpired;
    @Column(name = "is_locked")
    private boolean isLocked;
    @Column
    private String roles;

    public void setAuthorities(Collection<EnumGrantedAuthority> authorities) {
        if (authorities.isEmpty()) {
            this.roles = "";
            return;
        }
        StringBuilder res = new StringBuilder();
        Set<EnumGrantedAuthority> authSet = new HashSet<>(authorities);
        for (GrantedAuthority authority : authSet) {
            res.append(authority.getAuthority()).append(",");
        }
        res.deleteCharAt(res.length() - 1);
        this.roles = res.toString();
    }

    @Override
    @Transient
    public Collection<EnumGrantedAuthority> getAuthorities() {
        if (StringUtils.isBlank(this.roles)) {
            return Collections.emptyList();
        }
        String[] roles = StringUtils.trimToEmpty(this.roles).split(",");
        return SecurityUtils.createAuthorityList(roles);
    }

    @Deprecated
    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean canCreateTerminal() {
        return !isLocked && !isExpired && isEnabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", createdDate=").append(createdDate);
        sb.append(", isEnabled=").append(isEnabled);
        sb.append(", isExpired=").append(isExpired);
        sb.append(", isLocked=").append(isLocked);
        sb.append(", roles='").append(roles).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
