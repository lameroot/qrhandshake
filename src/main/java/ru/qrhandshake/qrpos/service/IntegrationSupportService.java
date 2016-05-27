package ru.qrhandshake.qrpos.service;

import org.springframework.stereotype.Service;
import ru.qrhandshake.qrpos.api.PaymentRequest;
import ru.qrhandshake.qrpos.domain.IntegrationSupport;

/**
 * Created by lameroot on 27.05.16.
 */
@Service
public class IntegrationSupportService {

    public IntegrationSupport checkIntegrationSupport(PaymentRequest paymentRequest) {
        switch (paymentRequest.getPaymentWay()) {
            case CARD: {
                return IntegrationSupport.RBS_SBRF;//todo: hardcode only sber
            }
            case YANDEX_WALLET: {
                return IntegrationSupport.YANDEX_WALLET;
            }
            case QIWI_WALLET: {
                return IntegrationSupport.QIWI_WALLET;
            }
            case GOOGLE_WALLET: {
                return IntegrationSupport.GOOGLE_WALLET;
            }
        }
        return null;
        //тут может быть проверка на тип карты и взависимости от этого выбор, через кого проводить операцию
    }
}
