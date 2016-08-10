package ru.qrhandshake.qrpos.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lameroot on 10.08.16.
 */
@Entity
@Table(name = "order_template_history")
public class OrderTemplateHistory {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderTemplateHistorySequence")
    @SequenceGenerator(name = "orderTemplateHistorySequence", sequenceName = "seq_order_template_history", allocationSize = 1)
    private Long id;
    @Column(name = "fk_order_template_id")
    private Long orderTemplateId;
    @Column(name = "fk_order_id")
    private Long merchantOrderId;
    @Column(name = "human_order_number")
    private String humanOrderNumber;
    @Column(name = "fk_client_id")
    private Long clientId;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "device_model")
    private String deviceModel;
    @Column(name = "status")
    private Boolean status;
    @Column(name = "date")
    private Date date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getHumanOrderNumber() {
        return humanOrderNumber;
    }

    public void setHumanOrderNumber(String humanOrderNumber) {
        this.humanOrderNumber = humanOrderNumber;
    }

    public Long getOrderTemplateId() {
        return orderTemplateId;
    }

    public void setOrderTemplateId(Long orderTemplateId) {
        this.orderTemplateId = orderTemplateId;
    }

    public Long getMerchantOrderId() {
        return merchantOrderId;
    }

    public void setMerchantOrderId(Long merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderTemplateHistory{");
        sb.append("id=").append(id);
        sb.append(", orderTemplateId=").append(orderTemplateId);
        sb.append(", merchantOrderId=").append(merchantOrderId);
        sb.append(", humanOrderNumber='").append(humanOrderNumber).append('\'');
        sb.append(", clientId=").append(clientId);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", deviceModel='").append(deviceModel).append('\'');
        sb.append(", status=").append(status);
        sb.append(", date=").append(date);
        sb.append('}');
        return sb.toString();
    }
}
