package ru.qrhandshake.qrpos.service.sms;

public interface SmsSender {

    boolean send(SmsObject smsObject) throws SmsSenderException;
}
