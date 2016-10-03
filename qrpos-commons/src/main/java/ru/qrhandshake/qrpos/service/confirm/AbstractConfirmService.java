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

public abstract class AbstractConfirmService implements ConfirmService {

    @Value("${confirm.code.length:4}")
    private Integer confirmCodeLength;
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
        }
        else {
            confirm.setAttempt(confirm.getAttempt() + 1);
        }
        ConfirmResult confirmResult = new ConfirmResult();
        String confirmCode = Util.generatePseudoUniqueNumber(confirmCodeLength);
        confirmResult.setConfirmCode(confirmCode);

        try {
            send(client, confirmCode);
        } catch (Exception e) {
            logger.error("Error send confirm code",e);
        }

        return confirmResult;
    }

    protected abstract void send(Client client, String confirmCode) throws Exception;

    protected Confirm findConfirmByClientAndAuthType(Client client, AuthType authType) {
        return confirmRepository.findByClientAndAuthType(client, authType);
    }
}
