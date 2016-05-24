package ru.qrhandshake.qrpos.api;

import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * Created by lameroot on 24.05.16.
 */
public class TerminalRegisterRequest  {

    private String merchantId;
    private String terminalId;
    private String authPassword;
    private boolean generateTerminalId;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return StringUtils.isBlank(terminalId) && generateTerminalId ? UUID.randomUUID().toString() : terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public boolean isGenerateTerminalId() {
        return generateTerminalId;
    }

    public void setGenerateTerminalId(boolean generateTerminalId) {
        this.generateTerminalId = generateTerminalId;
    }
}
