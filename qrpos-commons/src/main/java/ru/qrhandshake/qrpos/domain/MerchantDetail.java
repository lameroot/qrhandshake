package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

/**
 * Created by lameroot on 19.05.16.
 */
@Entity
@Table(name = "merchant_detail")
public class MerchantDetail {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "merchantDetailSequence")
    @SequenceGenerator(name = "merchantDetailSequence", sequenceName = "seq_merchant_detail", allocationSize = 1)
    private Long id;
    private String okato;
    private String bik;
    private String inn;
    private String kbk;
    @Column(name = "p2p_pan")
    private String p2pPan;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_merchant_id")
    private Merchant merchant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOkato() {
        return okato;
    }

    public void setOkato(String okato) {
        this.okato = okato;
    }

    public String getBik() {
        return bik;
    }

    public void setBik(String bik) {
        this.bik = bik;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getKbk() {
        return kbk;
    }

    public void setKbk(String kbk) {
        this.kbk = kbk;
    }

    public String getP2pPan() {
        return p2pPan;
    }

    public void setP2pPan(String p2pPan) {
        this.p2pPan = p2pPan;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
}
