package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

@Entity
@Table(name = "statistic")
public class Statistic {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statisticSequence")
    @SequenceGenerator(name = "statisticSequence", sequenceName = "seq_statistic", allocationSize = 1)
    private Long id;
    @Column(name = "value")
    private long value;
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatisticType type;
    @ManyToOne
    @JoinColumn(name = "fk_merchant_id", nullable = false)
    private Merchant merchant;
    @ManyToOne
    @JoinColumn(name = "fk_ordertemplate_id")
    private OrderTemplate orderTemplate;
    @Column(name = "start_time", nullable = false)
    private Long startTime;
    @Column(name = "end_time", nullable = false)
    private Long endTime;

    public static enum StatisticType {
        TEMPLATE
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public StatisticType getType() {
        return type;
    }

    public void setType(StatisticType type) {
        this.type = type;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public OrderTemplate getOrderTemplate() {
        return orderTemplate;
    }

    public void setOrderTemplate(OrderTemplate orderTemplate) {
        this.orderTemplate = orderTemplate;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
