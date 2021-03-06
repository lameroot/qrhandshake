package ru.qrhandshake.qrpos.service.sms;

public class SmsObject {

    private String phone;
    private String text;

    public String getPhone() {
        return phone;
    }

    public SmsObject setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getText() {
        return text;
    }

    public SmsObject setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmsObject{");
        sb.append("phone='").append(phone).append('\'');
        sb.append(", text='").append("****masked****").append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String toString(boolean debug) {
        final StringBuilder sb = new StringBuilder("SmsObject{");
        sb.append("phone='").append(phone).append('\'');
        sb.append(", text='").append(debug ? text : "****masked****").append('\'');
        sb.append('}');
        return sb.toString();
    }
}
