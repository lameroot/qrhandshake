package ru.qrhandshake.qrpos.api;


/**
 * Created by lameroot on 24.05.16.
 */
public class TerminalRegisterRequest extends ApiAuth {

    private boolean defaultTerminal;

    public boolean isDefaultTerminal() {
        return defaultTerminal;
    }

    public void setDefaultTerminal(boolean defaultTerminal) {
        this.defaultTerminal = defaultTerminal;
    }
}
