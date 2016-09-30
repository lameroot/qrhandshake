package ru.qrhandshake.qrpos.service.confirm;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MailConfirmService implements ConfirmService {

    private final static Logger logger = LoggerFactory.getLogger(MailConfirmService.class);

    @Resource
    private MailSender mailSender;

    @Value("${mail.confirm.code.length:4}")
    private Integer confirmCodeLength;
//    private ThreadPoolExecutor confirmThreadPoolExecutor = new ThreadPoolExecutor(3, 10, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10),
//            new ThreadFactoryBuilder().setDaemon(false)
//                    .setNameFormat("confirmMail" + "-%d")
//                    .setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread [" + t.getName() + "]", e))
//                    .build());

    @Override
    public ConfirmResult sendConfirmRequest(Client client) {
        ConfirmResult confirmResult = new ConfirmResult();
        try {
            String confirmCode = Util.generatePseudoUniqueNumber(confirmCodeLength);
            confirmResult.setConfirmCode(confirmCode);

            MailObject mailObject = new MailObject.SimpleMailObject().body(confirmCode).subject("Paystudio.ru / Confirm code").recipients(client.getEmail());

            mailSender.send(mailObject);//todo: сделать асинхронный вызов с повторами, если ошибка
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
