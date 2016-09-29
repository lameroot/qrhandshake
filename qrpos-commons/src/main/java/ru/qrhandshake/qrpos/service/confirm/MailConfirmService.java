package ru.qrhandshake.qrpos.service.confirm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.service.mail.MailObject;
import ru.qrhandshake.qrpos.service.mail.MailSender;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;

@Service
public class MailConfirmService implements ConfirmService {

    private final static Logger logger = LoggerFactory.getLogger(MailConfirmService.class);

    @Resource
    private MailSender mailSender;

    @Value("${mail.confirm.code.length:4}")
    private Integer confirmCodeLength;

    @Override
    public ConfirmResult sendConfirmRequest(Client client) {
        ConfirmResult confirmResult = new ConfirmResult();
        try {
            String confirmCode = Util.generatePseudoUnique(confirmCodeLength);
            confirmResult.setConfirmCode(confirmCode);

            MailObject mailObject = new MailObject.SimpleMailObject().body(confirmCode).subject("Paystudio.ru / Confirm code");

            mailSender.send(mailObject);
            confirmResult.setStatus(true);

        } catch (Exception e) {
            logger.error("Error confirm code",e);
        }
        return confirmResult;
    }

    @Override
    public AuthType getAuthType() {
        return AuthType.EMAIL;
    }
}
