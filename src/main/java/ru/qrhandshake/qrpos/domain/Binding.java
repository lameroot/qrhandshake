package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lameroot on 24.05.16.
 */
@Entity
@Table(name = "BINDING")
public class Binding {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bindingSequence")
    @SequenceGenerator(name = "bindingSequence", sequenceName = "seq_binding", allocationSize = 1)
    private Long id;
    @Column(name = "binding_id", nullable = false, unique = true)
    private String bindingId;
    @Column(name = "payment_params", nullable = false)
    private String paymentParams;
    @Column(name = "external_binding_id")
    private String externalBindingId;
    @Column(name = "payment_secure_type")
    @Enumerated(EnumType.STRING)
    private PaymentSecureType paymentSecureType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_client_id", nullable = false)
    private Client client;
    @Column(name = "integration_support", nullable = false)
    @Enumerated(EnumType.STRING)
    private IntegrationSupport integrationSupport;
    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;
    @Column(name = "payment_way", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentWay paymentWay;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentParams() {
        return paymentParams;
    }

    public void setPaymentParams(String paymentParams) {
        this.paymentParams = paymentParams;
    }

    public String getExternalBindingId() {
        return externalBindingId;
    }

    public void setExternalBindingId(String externalBindingId) {
        this.externalBindingId = externalBindingId;
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

    public PaymentSecureType getPaymentSecureType() {
        return paymentSecureType;
    }

    public void setPaymentSecureType(PaymentSecureType paymentSecureType) {
        this.paymentSecureType = paymentSecureType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public boolean isCompleted() {
        return isEnabled() && null != externalBindingId;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Binding{");
        sb.append("id=").append(id);
        sb.append(", bindingId='").append(bindingId).append('\'');
        sb.append(", paymentSecureType=").append(paymentSecureType);
        sb.append(", integrationSupport=").append(integrationSupport);
        sb.append(", createdDate=").append(createdDate);
        sb.append(", enabled=").append(enabled);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", paymentWay=").append(paymentWay);
        sb.append('}');
        return sb.toString();
    }
}
