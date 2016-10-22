package ru.qrhandshake.qrpos.integration;

/**
 * Created by lameroot on 24.05.16.
 */
public interface IntegrationOrderStatus {

    String getStatus();
    boolean isPaid();

}
