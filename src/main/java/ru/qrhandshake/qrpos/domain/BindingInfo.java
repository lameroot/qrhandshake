package ru.qrhandshake.qrpos.domain;

/**
 * Created by lameroot on 01.06.16.
 */
public class BindingInfo {

    private String clientId;
    private String bindingId;

    public BindingInfo() {
    }

    public BindingInfo(String clientId, String bindingId) {
        this.clientId = clientId;
        this.bindingId = bindingId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BindingInfo{");
        sb.append("clientId='").append(clientId).append('\'');
        sb.append(", bindingId='").append(bindingId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
