package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateResponse;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateResult;
import ru.qrhandshake.qrpos.api.ResponseStatus;
import ru.qrhandshake.qrpos.domain.OrderTemplate;

/**
 * Created by lameroot on 09.08.16.
 */
@Component
public class OrderTemplateToOrderTemplateResponseConverter implements Converter<OrderTemplate,OrderTemplateResponse> {

    @Override
    public OrderTemplateResponse convert(OrderTemplate orderTemplate) {
        OrderTemplateResponse orderTemplateResponse = new OrderTemplateResponse();
        orderTemplateResponse.setId(orderTemplate.getId());
        orderTemplateResponse.setAmount(orderTemplate.getAmount());
        orderTemplateResponse.setDescription(orderTemplate.getDescription());
        orderTemplateResponse.setStatus(null != orderTemplate.getId() ? ResponseStatus.SUCCESS : ResponseStatus.FAIL);
        return orderTemplateResponse;
    }
}
