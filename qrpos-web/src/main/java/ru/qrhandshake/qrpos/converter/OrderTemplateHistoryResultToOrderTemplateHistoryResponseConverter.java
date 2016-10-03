package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryResponse;
import ru.qrhandshake.qrpos.api.ordertemplate.OrderTemplateHistoryResult;
import ru.qrhandshake.qrpos.api.ResponseStatus;

/**
 * Created by lameroot on 11.08.16.
 */
@Component
public class OrderTemplateHistoryResultToOrderTemplateHistoryResponseConverter implements Converter<OrderTemplateHistoryResult, OrderTemplateHistoryResponse> {

    @Override
    public OrderTemplateHistoryResponse convert(OrderTemplateHistoryResult orderTemplateHistoryResult) {
        OrderTemplateHistoryResponse orderTemplateHistoryResponse = new OrderTemplateHistoryResponse();
        for (OrderTemplateHistoryResult.OrderTemplateHistoryData orderTemplateHistoryData : orderTemplateHistoryResult.getOrders()) {
            orderTemplateHistoryResponse.getOrders().add(orderTemplateHistoryData.toMap());
        }
        orderTemplateHistoryResponse.setStatus(null != orderTemplateHistoryResult ? ResponseStatus.SUCCESS : ResponseStatus.FAIL);
        return orderTemplateHistoryResponse;
    }
}
