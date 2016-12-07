package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.util.Date;

//@Entity
//@Table(name = "black_client")
public class BlackClient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blackClientSequence")
    @SequenceGenerator(name = "blackClientSequence", sequenceName = "seq_black_client", allocationSize = 1)
    private Long id;
    private Long clientId;
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
