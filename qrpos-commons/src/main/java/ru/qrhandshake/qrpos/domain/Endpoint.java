package ru.qrhandshake.qrpos.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserPasswordEndpoint.class, name = "userPasswordEndpoint")
})
@Entity
@Table(name = "endpoint")
@Inheritance(strategy = InheritanceType.JOINED)
public class Endpoint {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endpointSequence")
    @SequenceGenerator(name = "endpointSequence", sequenceName = "seq_endpoint", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_endpoint_catalog_id", nullable = false)
    private EndpointCatalog endpointCatalog;
    @Column(name = "enabled")
    private boolean enabled = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_merchant_id", nullable = false)
    private Merchant merchant;
    @Column(name = "params")
    private String params;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public EndpointCatalog getEndpointCatalog() {
        return endpointCatalog;
    }

    public void setEndpointCatalog(EndpointCatalog endpointCatalog) {
        this.endpointCatalog = endpointCatalog;
    }
}
