package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.OrderTemplateResponse;
import ru.qrhandshake.qrpos.api.OrderTemplateResult;
import ru.qrhandshake.qrpos.api.ResponseStatus;

/**
 * Created by lameroot on 09.08.16.
 */
@Component
public class OrderTemplateResultToOrderTemplateResponseConverter implements Converter<OrderTemplateResult,OrderTemplateResponse> {

    @Override
    public OrderTemplateResponse convert(OrderTemplateResult orderTemplateResult) {
        OrderTemplateResponse orderTemplateResponse = new OrderTemplateResponse();
        orderTemplateResponse.setId(orderTemplateResult.getId());
        orderTemplateResponse.setStatus(null != orderTemplateResult.getId() ? ResponseStatus.SUCCESS : ResponseStatus.FAIL);
        return orderTemplateResponse;
    }
}
