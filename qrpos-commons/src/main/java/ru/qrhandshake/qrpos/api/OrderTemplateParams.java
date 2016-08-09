package ru.qrhandshake.qrpos.api;

import ru.qrhandshake.qrpos.domain.Terminal;

/**
 * Created by lameroot on 09.08.16.
 */
public class OrderTemplateParams {

    private Terminal terminal;
    private String name;
    private String description;
    private Long amount;

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
