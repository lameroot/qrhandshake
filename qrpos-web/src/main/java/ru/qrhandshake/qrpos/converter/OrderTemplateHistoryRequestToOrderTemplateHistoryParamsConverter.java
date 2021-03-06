package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryParams;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryRequest;

/**
 * Created by lameroot on 11.08.16.
 */
@Component
public class OrderTemplateHistoryRequestToOrderTemplateHistoryParamsConverter implements Converter<OrderTemplateHistoryRequest, OrderTemplateHistoryParams> {

    @Override
    public OrderTemplateHistoryParams convert(OrderTemplateHistoryRequest orderTemplateHistoryRequest) {
        OrderTemplateHistoryParams orderTemplateHistoryParams = new OrderTemplateHistoryParams();
        orderTemplateHistoryParams.setFrom(orderTemplateHistoryRequest.getFrom());
        orderTemplateHistoryParams.setOrderTemplateId(orderTemplateHistoryRequest.getOrderTemplateId());
        orderTemplateHistoryParams.setId(orderTemplateHistoryRequest.getId());
        orderTemplateHistoryParams.setStatus(orderTemplateHistoryRequest.isStatus());
        return orderTemplateHistoryParams;
    }
}
