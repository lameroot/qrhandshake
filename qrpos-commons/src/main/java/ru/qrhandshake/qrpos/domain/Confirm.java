package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "confirm")
public class Confirm {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "confirmSequence")
    @SequenceGenerator(name = "confirmSequence", sequenceName = "seq_confirm", allocationSize = 1)
    private Long id;
    @Column
    private String code;
    @Column
    private Integer attempt;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiry;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_client_id", nullable = false)
    private Client client;
    @Column(name = "enabled")
    private boolean enabled;
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthType authType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }
}
