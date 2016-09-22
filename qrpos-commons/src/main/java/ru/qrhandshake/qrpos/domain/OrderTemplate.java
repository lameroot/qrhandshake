package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

@Entity
@Table(name = "order_template")
public class OrderTemplate {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderTemplateSequence")
    @SequenceGenerator(name = "orderTemplateSequence", sequenceName = "seq_order_template", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_terminal_id")
    private Terminal terminal;
    @Column
    private Long amount;
    @Column
    private String name;
    @Column
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
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
}
