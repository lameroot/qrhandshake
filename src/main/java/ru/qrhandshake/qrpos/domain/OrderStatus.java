package ru.qrhandshake.qrpos.domain;

/**
 * Created by lameroot on 20.05.16.
 */
public enum  OrderStatus {
    CREATED,
    DEPOSITED,
    APPROVED,
    DECLINED,
    REVERSED,
    REFUNDED,
    REDIRECT_TO_ACS;
}
