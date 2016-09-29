package ru.qrhandshake.qrpos.service.mail;

import org.junit.Test;
import ru.qrhandshake.qrpos.GeneralTest;

import javax.annotation.Resource;

/**
 * Created by lameroot on 30.09.16.
 */
public class MailSenderTest extends GeneralTest {

    @Resource
    private MailSender mailSender;

    @Test
    public void testSend() throws MailSenderException {
        MailObject mailObject = new MailObject.SimpleMailObject().subject("test").body("this is test")
                .sender("lameroot@gmail.com")
                .recipients("lameroot@mail.ru");

        mailSender.send(mailObject);
    }
}
