package ru.qrhandshake.qrpos.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.qrhandshake.qrpos.api.OrderTemplateParams;
import ru.qrhandshake.qrpos.api.OrderTemplateRequest;
import ru.qrhandshake.qrpos.domain.Terminal;
import ru.qrhandshake.qrpos.service.TerminalService;

import javax.annotation.Resource;

/**
 * Created by lameroot on 09.08.16.
 */
@Component
public class OrderTemplateRequestToOrderTemplateParamsConverter implements Converter<OrderTemplateRequest, OrderTemplateParams> {

    @Override
    public OrderTemplateParams convert(OrderTemplateRequest orderTemplateRequest) {
        OrderTemplateParams orderTemplateParams = new OrderTemplateParams();
        orderTemplateParams.setDescription(orderTemplateRequest.getDescription());
        orderTemplateParams.setAmount(orderTemplateRequest.getAmount());
        orderTemplateParams.setName(orderTemplateRequest.getName());
        orderTemplateParams.setTerminalId(orderTemplateRequest.getTerminalId());

        return orderTemplateParams;
    }
}
