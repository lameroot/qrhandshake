package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;

@Entity
@Table(name = "endpoint_catalog")
public class EndpointCatalog {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endpointCatalogSequence")
    @SequenceGenerator(name = "endpointCatalogSequence", sequenceName = "seq_endpoint_catalog", allocationSize = 1)
    private Long id;
    @Column(name = "integration_support", nullable = false, unique = true)
    @Enumerated(value = EnumType.STRING)
    private IntegrationSupport integrationSupport;
    @Column(name = "address", nullable = false)
    private String address;
    @Column(name = "params")
    private String params;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    public void setIntegrationSupport(IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
