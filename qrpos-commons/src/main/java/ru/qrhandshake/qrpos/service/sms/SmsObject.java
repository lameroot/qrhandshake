package ru.qrhandshake.qrpos.service.sms;

public class SmsObject {

    private String phone;
    private String text;
    private String sender;

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

    public String getSender() {
        return sender;
    }

    public SmsObject setSender(String sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SmsObject{");
        sb.append("phone='").append(phone).append('\'');
        sb.append(", text='").append("****masked****").append('\'');
        sb.append(", sender='").append(sender).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
