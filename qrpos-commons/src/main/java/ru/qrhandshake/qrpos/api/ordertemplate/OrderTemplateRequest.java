package ru.qrhandshake.qrpos.api.ordertemplate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 08.08.16.
 */
public class OrderTemplateRequest {

    @NotNull
    private String name;
    @NotNull
    private Long terminalId;
    private String description;
    @NotNull
    @Min(100)
    private Long amount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
