package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

/**
 * Created by lameroot on 24.05.16.
 */
@Entity
@Table(name = "binding")
public class Binding {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bindingSequence")
    @SequenceGenerator(name = "bindingSequence", sequenceName = "seq_binding", allocationSize = 1)
    private Long id;
    @Column(name = "payment_params", nullable = false)
    private String paymentParams;
    @Column(name = "external_binding_id", nullable = false)
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
}
