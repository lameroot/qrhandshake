package ru.qrhandshake.qrpos.service.confirm;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Confirm;
import ru.qrhandshake.qrpos.repository.ConfirmRepository;
import ru.qrhandshake.qrpos.service.sms.SmsObject;
import ru.qrhandshake.qrpos.service.sms.SmsSender;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SmsConfirmService extends AbstractConfirmService {

    @Autowired(required = false)
    private MailConfirmService mailConfirmService;
    @Resource
    private SmsSender smsSender;

    @Value("${confirm.sms.useMail:false}")
    private boolean useMail;
    @Value("${confirm.sms.phonesViaMail:}")
    private List<String> phonesViaMail;
    @Value("${confirm.sms.text:Confirm code: %s}")
    private String confirmSmsText;

    @Override
    public AuthType getAuthType() {
        return AuthType.PHONE;
    }

    @Override
    protected void send(Client client, String confirmCode) throws Exception {
        if ( null != mailConfirmService && useMail ) {
            mailConfirmService.send(client,confirmCode);
        }
        else if ( phonesViaMail.contains(client.getPhone()) && null != mailConfirmService ) {
            mailConfirmService.send(client,confirmCode);
        }
        SmsObject smsObject = new SmsObject().setPhone(client.getPhone()).setText(String.format(confirmSmsText,confirmCode));
        smsSender.send(smsObject);
    }

}
