package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

/**
 * Created by lameroot on 24.05.16.
 */
@Entity
@Table(name = "terminal")
public class Terminal {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "terminalSequence")
    @SequenceGenerator(name = "terminalSequence", sequenceName = "seq_terminal", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "fk_merchant_id", nullable = false)
    private Merchant merchant;
    @Column(name = "auth_name", nullable = false, unique = true)
    private String authName;
    @Column(name = "auth_password", nullable = false)
    private String authPassword;
    @Column(name = "enabled")
    private boolean enabled;

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

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
