package ru.qrhandshake.qrpos.service;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.PaymentParams;
import ru.qrhandshake.qrpos.api.PaymentRequest;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;
import ru.qrhandshake.qrpos.domain.Merchant;

/**
 * Created by lameroot on 27.05.16.
 */
@Service
public class IntegrationSupportService {

    public IntegrationSupport checkIntegrationSupport(Merchant merchant, PaymentParams paymentParams) {
        if ( null != merchant && null != merchant.getIntegrationSupport() ) return merchant.getIntegrationSupport();
//      Если эта проверка только для каточного платежа, то нужен только номер карты

        return IntegrationSupport.RBS_SBRF;//todo: hardcode only sber
        //тут может быть проверка на тип карты и взависимости от этого выбор, через кого проводить операцию
    }
}
