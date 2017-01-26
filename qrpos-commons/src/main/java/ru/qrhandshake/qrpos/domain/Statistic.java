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
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    @Column(name = "ordertemplate_id")
    private Long orderTemplateId;
    @Column(name = "terminal_id")
    private Long terminalId;
    @Column(name = "start_time", nullable = false)
    private Long startTime;
    @Column(name = "end_time", nullable = false)
    private Long endTime;

    public static enum StatisticType {
        COUNT_PAID,
        AMOUNT_PAID,
        COUNT_DECLINED,
        AMOUNT_DECLINED
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

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getOrderTemplateId() {
        return orderTemplateId;
    }

    public void setOrderTemplateId(Long orderTemplateId) {
        this.orderTemplateId = orderTemplateId;
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

    public Long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }
}
