package ru.qrhandshake.qrpos.service.confirm;

import ru.qrhandshake.qrpos.domain.AuthType;
import ru.qrhandshake.qrpos.domain.Client;

/**
 * Created by lameroot on 29.09.16.
 */
public interface ConfirmService {

    ConfirmResult sendConfirmRequest(Client client);
    AuthType getAuthType();
}
