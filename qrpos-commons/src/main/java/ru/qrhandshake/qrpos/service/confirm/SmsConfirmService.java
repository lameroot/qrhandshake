package ru.qrhandshake.qrpos.service.confirm;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;

import javax.annotation.Resource;

@Service
public class SmsConfirmService implements ConfirmService {

    @Autowired(required = false)
    private MailConfirmService mailConfirmService;

    @Value("${confirm.sms.useMail:false}")
    private boolean useMail;

    @Override
    public ConfirmResult sendConfirmRequest(Client client) {
        if ( null != mailConfirmService && useMail ) return mailConfirmService.sendConfirmRequest(client);
        throw new IllegalArgumentException("Not working now, use dummySmsConfirmService");
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.PHONE;
    }
}
