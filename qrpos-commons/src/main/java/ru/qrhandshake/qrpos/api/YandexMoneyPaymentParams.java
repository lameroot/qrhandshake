package ru.qrhandshake.qrpos.api;

/**
 * Created by lameroot on 30.05.16.
 */
public class YandexMoneyPaymentParams extends PaymentParams {

    private String dstAccount;

    public String getDstAccount() {
        return dstAccount;
    }

    public void setDstAccount(String dstAccount) {
        this.dstAccount = dstAccount;
    }
}
