package ru.qrhandshake.qrpos.service.confirm;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Confirm;
import ru.qrhandshake.qrpos.repository.ConfirmRepository;

import javax.annotation.Resource;

@Service
public class SmsConfirmService extends AbstractConfirmService {

    @Autowired(required = false)
    private MailConfirmService mailConfirmService;
    @Resource
    private ConfirmRepository confirmRepository;

    @Value("${confirm.sms.useMail:false}")
    private boolean useMail;

    @Override
    public AuthType getAuthType() {
        return AuthType.PHONE;
    }

    @Override
    protected void send(Client client, String confirmCode) throws Exception {
        if ( null != mailConfirmService && useMail ) {
            mailConfirmService.send(client,confirmCode);
        }
        else {
            throw new IllegalArgumentException("Not working now, use dummySmsConfirmService");
        }
    }

}
