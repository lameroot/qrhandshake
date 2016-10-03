package ru.qrhandshake.qrpos.service.confirm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;
import ru.qrhandshake.qrpos.domain.Confirm;
import ru.qrhandshake.qrpos.repository.ConfirmRepository;
import ru.qrhandshake.qrpos.util.Util;

import javax.annotation.Resource;
import java.util.Calendar;

public abstract class AbstractConfirmService implements ConfirmService {

    @Value("${confirm.code.length:4}")
    private Integer confirmCodeLength;
    @Value("${confirm.expiry.hours:24}")
    private Integer maxExpiry;
    private final static Logger logger = LoggerFactory.getLogger(AbstractConfirmService.class);

    @Resource
    private ConfirmRepository confirmRepository;

    @Override
    public ConfirmResult sendConfirmRequest(Client client) {
        Confirm confirm = findConfirmByClientAndAuthType(client, getAuthType());
        if ( null == confirm ) {
            confirm = new Confirm();
            confirm.setClient(client);
            confirm.setEnabled(true);
            confirm.setAttempt(0);
            confirm.setAuthType(getAuthType());
            Calendar expiryDate = Calendar.getInstance();
            expiryDate.add(Calendar.HOUR,maxExpiry);
            confirm.setExpiry(expiryDate.getTime());
        }
        else {
            confirm.setAttempt(confirm.getAttempt() + 1);
        }
        ConfirmResult confirmResult = new ConfirmResult();
        String confirmCode = Util.generatePseudoUniqueNumber(confirmCodeLength);
        confirmResult.setConfirmCode(confirmCode);
        confirm.setCode(confirmCode);
        try {
            send(client, confirmCode);
            confirmRepository.save(confirm);
            confirmResult.setStatus(true);
        } catch (Throwable e) {
            logger.error("Error send confirm code",e);
        }

        return confirmResult;
    }

    protected abstract void send(Client client, String confirmCode) throws Exception;

    protected Confirm findConfirmByClientAndAuthType(Client client, AuthType authType) {
        return confirmRepository.findByClientAndAuthType(client, authType);
    }
}
