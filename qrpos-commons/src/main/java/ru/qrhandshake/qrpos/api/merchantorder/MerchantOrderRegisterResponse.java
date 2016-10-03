package ru.qrhandshake.qrpos.api.merchantorder;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.qrhandshake.qrpos.api.ApiResponse;

/**
 * Created by lameroot on 18.05.16.
 */
public class MerchantOrderRegisterResponse extends ApiResponse {

    @JsonIgnore
    private Long id;
    private String orderId;
    private String paymentUrl;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
