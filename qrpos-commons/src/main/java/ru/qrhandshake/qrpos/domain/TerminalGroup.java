package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "terminal_group")
public class TerminalGroup implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "terminalGroupSequence")
    @SequenceGenerator(name = "terminalGroupSequence", sequenceName = "seq_terminal_group", allocationSize = 1)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "terminalGroup")
    private Set<Terminal> terminals = new HashSet<>();
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_merchant_id", nullable = false)
    private Merchant merchant;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "terminalGroup")
    private Set<TerminalGroupParam> terminalGroupParams = new HashSet<>();

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

    public Set<Terminal> getTerminals() {
        return terminals;
    }

    public void setTerminals(Set<Terminal> terminals) {
        this.terminals = terminals;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Set<TerminalGroupParam> getTerminalGroupParams() {
        return terminalGroupParams;
    }

    public void setTerminalGroupParams(Set<TerminalGroupParam> terminalGroupParams) {
        this.terminalGroupParams = terminalGroupParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TerminalGroup)) return false;

        TerminalGroup that = (TerminalGroup) o;

        if (!id.equals(that.id)) return false;
        if (!merchant.equals(that.merchant)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + merchant.hashCode();
        return result;
    }
}
