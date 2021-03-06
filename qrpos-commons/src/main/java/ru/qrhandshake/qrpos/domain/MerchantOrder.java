package ru.qrhandshake.qrpos.domain;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "merchantOrderSequence")
    @SequenceGenerator(name = "merchantOrderSequence", sequenceName = "seq_merchant_order", allocationSize = 1)
    private Long id;
    @Column(name = "order_id", unique = true)
    private String orderId;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_client_id")
    private Client client;
    @Column(name = "integration")
    @Enumerated(EnumType.STRING)
    private IntegrationSupport integrationSupport;
    @Column(name = "orderStatus")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.REGISTERED;
    @Column(name = "session_id")
    private String sessionId;
    @Column(name = "payment_secure_type")
    @Enumerated(EnumType.STRING)
    private PaymentSecureType paymentSecureType;
    @Column(name = "payment_way")
    @Enumerated(EnumType.STRING)
    private PaymentWay paymentWay;
    @Column(name = "payment_type")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
        if ( null == this.paymentDate && null != this.orderStatus && this.orderStatus.equals(OrderStatus.PAID) ) {
            this.paymentDate = new Date();
        }
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public PaymentSecureType getPaymentSecureType() {
        return paymentSecureType;
    }

    public void setPaymentSecureType(PaymentSecureType paymentSecureType) {
        this.paymentSecureType = paymentSecureType;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public boolean canPayment() {
        return null != getOrderStatus() && OrderStatus.REGISTERED.equals(getOrderStatus());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MerchantOrder{");
        sb.append("id=").append(id);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", externalOrderStatus='").append(externalOrderStatus).append('\'');
        sb.append(", createdDate=").append(createdDate);
        sb.append(", paymentDate=").append(paymentDate);
        sb.append(", amount=").append(amount);
        sb.append(", description='").append(description).append('\'');
        sb.append(", integrationSupport=").append(integrationSupport);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", paymentSecureType=").append(paymentSecureType);
        sb.append(", paymentWay=").append(paymentWay);
        sb.append(", paymentType=").append(paymentType);
        sb.append('}');
        return sb.toString();
    }
}
