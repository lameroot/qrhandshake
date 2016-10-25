package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lameroot on 18.05.16.
 */
@Entity
@Table(name = "merchant")
public class Merchant {

    @Id
    @Column(updatable = false, name="id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "merchantSequence")
    @SequenceGenerator(name = "merchantSequence", sequenceName = "seq_merchant", allocationSize = 1)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date();
    @Column(name = "integration_support", nullable = true)
    @Enumerated(EnumType.STRING)
    private IntegrationSupport integrationSupport;
    @Column(name = "create_binding")
    private boolean createBinding = true;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public IntegrationSupport getIntegrationSupport() {
        return integrationSupport;
    }

    public void setIntegrationSupport(IntegrationSupport integrationSupport) {
        this.integrationSupport = integrationSupport;
    }

    public boolean isCreateBinding() {
        return createBinding;
    }

    public void setCreateBinding(boolean createBinding) {
        this.createBinding = createBinding;
    }

    @Transient
    public String getMerchantId() {
        return null != id ? String.valueOf(id) : null;
    }

}
