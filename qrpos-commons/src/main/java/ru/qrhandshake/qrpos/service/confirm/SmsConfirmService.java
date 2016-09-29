package ru.qrhandshake.qrpos.service.confirm;


import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;

public class SmsConfirmService implements ConfirmService {

    @Override
    public ConfirmResult sendConfirmRequest(Client client) {
        throw new IllegalArgumentException("Not working now, use dummySmsConfirmService");
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.PHONE;
    }
}
