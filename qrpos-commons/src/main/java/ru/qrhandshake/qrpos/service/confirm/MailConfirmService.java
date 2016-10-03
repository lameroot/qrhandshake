package ru.qrhandshake.qrpos.service.confirm;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.service.mail.MailObject;
import ru.qrhandshake.qrpos.service.mail.MailSender;

import javax.annotation.Resource;

@Service
public class MailConfirmService extends AbstractConfirmService {

    @Resource
    private MailSender mailSender;

//    private ThreadPoolExecutor confirmThreadPoolExecutor = new ThreadPoolExecutor(3, 10, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10),
//            new ThreadFactoryBuilder().setDaemon(false)
//                    .setNameFormat("confirmMail" + "-%d")
//                    .setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread [" + t.getName() + "]", e))
//                    .build());

    @Override
    public AuthType getAuthType() {
        return AuthType.EMAIL;
    }

    @Override
    protected void send(Client client, String confirmCode) throws Exception {
        MailObject mailObject = new MailObject.SimpleMailObject().body(confirmCode).subject("Paystudio.ru / Confirm code").recipients(client.getEmail());
        mailSender.send(mailObject);//todo: сделать асинхронный вызов с повторами, если ошибка
    }
}
