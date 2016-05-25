package ru.qrhandshake.qrpos.domain;

import ru.qrhandshake.qrpos.integration.IntegrationSupport;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lameroot on 18.05.16.
 */
@Entity
@Table(name = "merchant_order")
public class MerchantOrder {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderSequence")
    @SequenceGenerator(name = "orderSequence", sequenceName = "seq_order", allocationSize = 1)
    private Long id;
    @Column(name = "external_id")
    private String externalId;
    @Column(name = "external_order_status")
    private String externalOrderStatus;
    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();
    @Column(name = "payment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
    private Long amount;
    private String description;
    @Column(name = "device_id")
    private String deviceId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_merchant_id")
    private Merchant merchant;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_terminal_id")
    private Terminal terminal;
    //todo: future
    @Transient
    private Client client;
    @Column(name = "integration")
    @Enumerated(EnumType.STRING)
    private IntegrationSupport integrationSupport;
    @Column(name = "orderStatus")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.REGISTERED;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    public void setIntegrationSupport(IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalOrderStatus() {
        return externalOrderStatus;
    }

    public void setExternalOrderStatus(String externalOrderStatus) {
        this.externalOrderStatus = externalOrderStatus;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
