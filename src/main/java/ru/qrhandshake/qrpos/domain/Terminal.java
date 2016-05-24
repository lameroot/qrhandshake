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
    @JoinColumn(name = "fk_merchant_id")
    private Merchant merchant;

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
}
